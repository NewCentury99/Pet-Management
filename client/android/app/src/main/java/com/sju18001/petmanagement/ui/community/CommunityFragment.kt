package com.sju18001.petmanagement.ui.community

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import android.widget.MediaController
import android.widget.Toast
import android.widget.VideoView
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.sju18001.petmanagement.R
import com.sju18001.petmanagement.controller.Util
import com.sju18001.petmanagement.databinding.FragmentCommunityBinding
import com.sju18001.petmanagement.restapi.RetrofitBuilder
import com.sju18001.petmanagement.restapi.SessionManager
import com.sju18001.petmanagement.restapi.dto.*
import com.sju18001.petmanagement.ui.community.comment.CommunityCommentActivity
import com.sju18001.petmanagement.ui.community.createUpdatePost.CreateUpdatePostActivity
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import kotlin.math.ceil


class CommunityFragment : Fragment() {
    val communityViewModel: CommunityViewModel by activityViewModels()

    private var _binding: FragmentCommunityBinding? = null
    private val binding get() = _binding!!

    // session manager for user token
    private lateinit var sessionManager: SessionManager

    // 리싸이클러뷰
    private lateinit var adapter: CommunityPostListAdapter

    private var isViewDestroyed: Boolean = false
    
    // 글 새로고침
    private var topPostId: Long? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCommunityBinding.inflate(inflater, container, false)

        // get session manager
        sessionManager = context?.let { SessionManager(it) }!!

        // SwipeRefreshLayout
        binding.layoutSwipeRefresh.setOnRefreshListener {
            resetPostData()
            updateAdapterDataSetByFetchPost(FetchPostReqDto(null, null, null, null))
        }

        // for create post FAB
        binding.createPostFab.setOnClickListener {
            startActivity(Intent(context, CreateUpdatePostActivity::class.java))
            requireActivity().overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left)
        }

        return binding.root
    }

    override fun onStart() {
        super.onStart()

        // for create post FAB
        binding.createPostFab.setOnClickListener {
            val createUpdatePostActivityIntent = Intent(context, CreateUpdatePostActivity::class.java)
            createUpdatePostActivityIntent.putExtra("fragmentType", "create_post")
            startActivity(createUpdatePostActivityIntent)
            requireActivity().overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left)
        }

        // 어뎁터 초기화
        initializeAdapter()

        // 초기 post 추가
        updateAdapterDataSetByFetchPost(FetchPostReqDto(null, null, null, null))
    }

    override fun onStop() {
        super.onStop()

        // 스크롤 인덱스 저장
        val layoutManager = (binding.recyclerViewPost.layoutManager as LinearLayoutManager)
        val firstIndex = layoutManager.findFirstVisibleItemPosition()
        communityViewModel.lastScrolledIndex = firstIndex
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null

        isViewDestroyed = true

        // Delete saved files
        val dir = File(requireContext().getExternalFilesDir(null).toString() + "/pet_management")
        dir.deleteRecursively()
    }

    private fun initializeAdapter(){
        // 빈 배열로 초기화
        adapter = CommunityPostListAdapter(arrayListOf())

        // 인터페이스 구현
        adapter.communityPostListAdapterInterface = object: CommunityPostListAdapterInterface {
            override fun startCommunityCommentActivity(postId: Long) {
                val communityCommentActivityIntent = Intent(context, CommunityCommentActivity::class.java)
                communityCommentActivityIntent.putExtra("postId", postId)

                startActivity(communityCommentActivityIntent)
                requireActivity().overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left)
            }

            override fun startCreateUpdatePostActivity(postId: Long) {
                val createUpdatePostActivityIntent =
                    Intent(context, CreateUpdatePostActivity::class.java)
                createUpdatePostActivityIntent.putExtra("fragmentType", "update_post")
                createUpdatePostActivityIntent.putExtra("postId", postId)
                startActivity(createUpdatePostActivityIntent)
                requireActivity().overridePendingTransition(
                    R.anim.enter_from_right,
                    R.anim.exit_to_left
                )
            }

            override fun setAccountPhoto(id: Long, holder: CommunityPostListAdapter.ViewHolder){
                val call = RetrofitBuilder.getServerApiWithToken(sessionManager.fetchUserToken()!!)
                    .fetchAccountPhotoReq(FetchAccountPhotoReqDto(id))
                call.enqueue(object: Callback<ResponseBody> {
                    override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                        if(isViewDestroyed){
                            return
                        }

                        if(response.isSuccessful) {
                            // convert photo to byte array + get bitmap
                            val photoByteArray = response.body()!!.byteStream().readBytes()
                            val photoBitmap = BitmapFactory.decodeByteArray(photoByteArray, 0, photoByteArray.size)

                            // set account photo + save photo value
                            holder.petPhotoImage.setImageBitmap(photoBitmap)
                        }
                        else {
                            // get error message
                            val errorMessage = Util.getMessageFromErrorBody(response.errorBody()!!)

                            // Toast + Log
                            Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                            Log.d("error", errorMessage)
                        }
                    }

                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                        // log error message
                        Log.d("error", t.message.toString())
                    }
                })
            }

            override fun setAccountDefaultPhoto(holder: CommunityPostListAdapter.ViewHolder) {
                holder.petPhotoImage.setImageDrawable(requireContext().getDrawable(R.drawable.ic_baseline_account_circle_24))
            }

            override fun setPostMedia(
                holder: CommunityPostListAdapter.PostMediaItemCollectionAdapter.ViewPagerHolder,
                id: Long,
                index: Int,
                url: String
            ) {
                val body = FetchPostMediaReqDto(id, index)
                val call = RetrofitBuilder.getServerApiWithToken(sessionManager.fetchUserToken()!!).fetchPostMediaReq(body)
                call!!.enqueue(object: Callback<ResponseBody> {
                    @RequiresApi(Build.VERSION_CODES.R)
                    override fun onResponse(
                        call: Call<ResponseBody>,
                        response: Response<ResponseBody>
                    ) {
                        if(isViewDestroyed){
                            return
                        }

                        if(response.isSuccessful){
                            // 영상
                            if(Util.isUrlVideo(url)){
                                // TODO: Replace it into ServerUtil.createCopyAndReturnRealPathServer()
                                // Save file
                                val dir = File(requireContext().getExternalFilesDir(null).toString() + "/pet_management")
                                if(! dir.exists()){
                                    dir.mkdir()
                                }

                                val file = if(url.endsWith(".mp4")){
                                    File.createTempFile("post_media", ".mp4", dir)
                                }else{
                                    File.createTempFile("post_media", ".webm", dir)
                                }
                                val os = FileOutputStream(file)
                                os.write(response.body()!!.byteStream().readBytes())
                                os.close()
                                val uri = Uri.fromFile(file)

                                // View
                                val postMediaVideo = holder.postMediaVideo
                                postMediaVideo.visibility = View.VISIBLE

                                // 영상의 비율을 유지한 채로, 영상의 사이즈를 가로로 꽉 채웁니다.
                                val retriever = MediaMetadataRetriever()
                                retriever.setDataSource(requireContext(), uri)

                                val videoWidth = Integer.parseInt(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH))
                                val videoHeight = Integer.parseInt(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT))

                                val screenWidth = Util.getScreenWidthInPixel(requireActivity())
                                val ratio: Float = screenWidth.toFloat() / videoWidth.toFloat()

                                postMediaVideo.layoutParams.height = (videoHeight.toFloat() * ratio).toInt()

                                // 반복 재생
                                postMediaVideo.setOnCompletionListener {
                                    postMediaVideo.start()
                                }

                                // 재생
                                postMediaVideo.setVideoURI(uri)
                                postMediaVideo.requestFocus()
                                postMediaVideo.start()
                            }
                            // 이미지
                            else{
                                // Convert photo to byte array + get bitmap
                                val photoByteArray = response.body()!!.byteStream().readBytes()
                                val photoBitmap = BitmapFactory.decodeByteArray(photoByteArray, 0, photoByteArray.size)

                                // Set post image
                                val postMediaImage = holder.postMediaImage
                                postMediaImage.visibility = View.VISIBLE
                                postMediaImage.setImageBitmap(photoBitmap)

                                // 영상의 사이즈를 가로로 꽉 채우되, 비율을 유지합니다.
                                val screenWidth = Util.getScreenWidthInPixel(requireActivity())
                                val ratio: Float = screenWidth.toFloat() / photoBitmap.width.toFloat()
                                postMediaImage.layoutParams.height = (photoBitmap.height.toFloat() * ratio).toInt()
                            }
                        }else{
                            // get error message
                            val errorMessage = Util.getMessageFromErrorBody(response.errorBody()!!)

                            // Toast + Log
                            Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                        }
                    }

                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                        if(isViewDestroyed){
                            return
                        }

                        Toast.makeText(context, t.message, Toast.LENGTH_LONG).show()
                    }
                })
            }
        }

        binding.recyclerViewPost?.let{
            it.adapter = adapter
            it.layoutManager = LinearLayoutManager(activity)
            
            // 스크롤하여, 최하단에 위치할 시 post 추가 로드
            it.addOnScrollListener(object: RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    val itemCount = adapter.itemCount
                    if(!recyclerView.canScrollVertically(1) && itemCount != 0 && itemCount % 10 == 0){
                        updateAdapterDataSetByFetchPost(FetchPostReqDto(
                            ceil(itemCount.toDouble() / 10).toInt(), topPostId, null, null
                        ))
                    }
                }
            })
        }
    }

    private fun updateAdapterDataSetByFetchPost(body: FetchPostReqDto){
        val call = RetrofitBuilder.getServerApiWithToken(sessionManager.fetchUserToken()!!)
            .fetchPostReq(body)
        call!!.enqueue(object: Callback<FetchPostResDto> {
            override fun onResponse(
                call: Call<FetchPostResDto>,
                response: Response<FetchPostResDto>
            ) {
                if(isViewDestroyed){
                    return
                }

                if(response.isSuccessful){
                    response.body()!!.postList?.let {
                        if(it.isNotEmpty()){
                            it.map { item ->
                                adapter.addItem(item)
                            }

                            if(topPostId == null){
                                topPostId = it.first().id
                            }

                            // 데이터셋 변경 알림
                            binding.recyclerViewPost.post{
                                adapter.notifyDataSetChanged()

                                // 스크롤 로드
                                if(communityViewModel.lastScrolledIndex != -1){
                                    binding.recyclerViewPost.scrollToPosition(communityViewModel.lastScrolledIndex)
                                    communityViewModel.lastScrolledIndex = -1
                                }
                            }
                        }
                    }
                }else{
                    Toast.makeText(context, Util.getMessageFromErrorBody(response.errorBody()!!), Toast.LENGTH_LONG).show()
                }

                // 새로고침 아이콘 제거
                binding.layoutSwipeRefresh.isRefreshing = false
            }

            override fun onFailure(call: Call<FetchPostResDto>, t: Throwable) {
                if(isViewDestroyed){
                    return
                }

                Toast.makeText(context, t.message.toString(), Toast.LENGTH_LONG).show()

                // 새로고침 아이콘 제거
                binding.layoutSwipeRefresh.isRefreshing = false
            }
        })
    }

    private fun resetPostData(){
        adapter.resetDataSet()

        // 데이터셋 변경 알림
        binding.recyclerViewPost.post{
            adapter.notifyDataSetChanged()
        }
    }

    fun startAllVideos(){
        val layoutManager = (binding.recyclerViewPost.layoutManager as LinearLayoutManager)
        val firstIndex = layoutManager.findFirstVisibleItemPosition()
        val lastIndex = layoutManager.findLastVisibleItemPosition()

        for(i in firstIndex..lastIndex){
            val videoPostMedia = layoutManager.findViewByPosition(i)?.findViewById<VideoView>(R.id.video_post_media)
            videoPostMedia?.start()
        }
    }

    fun pauseAllVideos(){
        val layoutManager = (binding.recyclerViewPost.layoutManager as LinearLayoutManager)
        val firstIndex = layoutManager.findFirstVisibleItemPosition()
        val lastIndex = layoutManager.findLastVisibleItemPosition()

        for(i in firstIndex..lastIndex){
            val videoPostMedia = layoutManager.findViewByPosition(i)?.findViewById<VideoView>(R.id.video_post_media)
            videoPostMedia?.pause()
        }
    }
}