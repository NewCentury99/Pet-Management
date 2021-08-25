package com.sju18001.petmanagement.ui.community.comment

import android.app.Activity
import android.content.Context
import android.os.Build
import android.text.Spannable
import android.text.SpannableString
import android.text.style.LeadingMarginSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.marginLeft
import androidx.recyclerview.widget.RecyclerView
import com.sju18001.petmanagement.R
import com.sju18001.petmanagement.controller.Util
import com.sju18001.petmanagement.restapi.dao.Comment
import com.sju18001.petmanagement.ui.community.CommunityPostListAdapter
import de.hdodenhof.circleimageview.CircleImageView
import java.time.LocalDateTime

interface CommunityCommentListAdapterInterface{
    fun getActivity(): Activity
    fun onClickReply(id: Long, nickname: String)
    fun onLongClickComment(authorId: Long, commentId: Long, commentContents: String)
    fun setAccountPhoto(id: Long, holder: CommunityCommentListAdapter.ViewHolder)
    fun setAccountDefaultPhoto(holder: CommunityCommentListAdapter.ViewHolder)
    fun fetchReplyComment(pageIndex: Int, topCommentId: Long, parentCommentId: Long, position: Int)
}

class CommunityCommentListAdapter(
    private var dataSet: ArrayList<Comment>,
    private var pageIndices: ArrayList<Int>,
    private var topCommentIdList: ArrayList<Long>
    ) : RecyclerView.Adapter<CommunityCommentListAdapter.ViewHolder>()  {
    lateinit var communityCommentListAdapterInterface: CommunityCommentListAdapterInterface

    class ViewHolder(view: View): RecyclerView.ViewHolder(view){
        val communityCommentLayout: ConstraintLayout = view.findViewById(R.id.layout_community_comment)
        val profileImage: ImageView = view.findViewById(R.id.image_profile)
        val nicknameTextView: TextView = view.findViewById(R.id.text_nickname)
        val contentsTextView: TextView = view.findViewById(R.id.text_contents)
        val timestampTextView: TextView = view.findViewById(R.id.text_timestamp)
        val replyTextView: TextView = view.findViewById(R.id.text_reply)
        val loadReplyTextView: TextView = view.findViewById(R.id.text_load_reply)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.comment_item, parent, false)

        return ViewHolder(view)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // 데이터 동기화
        updateDataSetToViewHolder(holder, dataSet[position], position)
        
        // 댓글 내용에 indent 추가
        setSpanToContent(holder.nicknameTextView, holder.contentsTextView)
        
        // 리스너 추가
        setListenerOnView(holder, position)
    }

    override fun getItemCount(): Int = dataSet.size

    @RequiresApi(Build.VERSION_CODES.O)
    private fun updateDataSetToViewHolder(holder: CommunityCommentListAdapter.ViewHolder, data: Comment, position: Int){
        holder.nicknameTextView.text = data.author.nickname
        holder.contentsTextView.text = data.contents
        holder.timestampTextView.text = getTimestampForDisplay(data.timestamp)

        // Set photo
        if(!data.author.photoUrl.isNullOrEmpty()){
            communityCommentListAdapterInterface.setAccountPhoto(data.author.id, holder)
        }else{
            communityCommentListAdapterInterface.setAccountDefaultPhoto(holder)
        }

        // 답글 불러오기 버튼 세팅
        // topCommentIdList가 -1로 초기화되므로, -1이면 해당 댓글의 답글이 없다는 의미임
        if(topCommentIdList[position] == (-1).toLong()){
            holder.loadReplyTextView.visibility = View.GONE
        }else{
            holder.loadReplyTextView.visibility = View.VISIBLE
        }

        // Comment, Reply를 구분하여 이에 따라 뷰 세팅
        setViewDependingOnCommentOrReply(holder, position)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getTimestampForDisplay(timestamp: String): String{
        val secondDiff: Long = Util.getSecondDifferenceInLocalDateTime(LocalDateTime.parse(timestamp))
        val minuteDiff: Long = secondDiff / 60
        val hourDiff: Long = minuteDiff / 60
        val dateDiff: Long = hourDiff / 24
        val monthDiff: Long = dateDiff / 30
        val yearDiff: Long = monthDiff / 12

        return when {
            yearDiff > 0 -> "${yearDiff}년"
            monthDiff > 0 -> "${monthDiff}달"
            dateDiff > 0 -> "${dateDiff}일"
            hourDiff > 0 -> "${hourDiff}시간"
            minuteDiff > 0 -> "${minuteDiff}분"
            else -> "${secondDiff}초"
        }
    }

    private fun setSpanToContent(nicknameTextView: TextView, contentTextView: TextView){
        contentTextView.post{
            // contents의 첫줄에 닉네임만큼의 indent를 주기 위함
            val spannable = SpannableString(contentTextView.text)
            val span = LeadingMarginSpan.Standard(nicknameTextView.width + 10, 0)
            spannable.setSpan(span, 0, spannable.count(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

            contentTextView.text = spannable
        }
    }

    private fun setViewDependingOnCommentOrReply(holder: ViewHolder, position: Int){
        val isReply = dataSet[position].parentCommentId != null
        
        // 답글일 시 Margin 추가
        val layoutParams = holder.communityCommentLayout.layoutParams as ViewGroup.MarginLayoutParams
        layoutParams.leftMargin = if(isReply) 96 else 0
        holder.communityCommentLayout.layoutParams = layoutParams

        // 답글일 시 답글 달기 제거
        holder.replyTextView.visibility = if(isReply) View.GONE else View.VISIBLE
    }

    private fun setListenerOnView(holder: ViewHolder, position: Int){
        holder.nicknameTextView.setOnClickListener {
            // TODO: 프로필로 이동
        }

        holder.replyTextView.setOnClickListener {
            dataSet[position].author.nickname?.let {
                communityCommentListAdapterInterface.onClickReply(dataSet[position].id, it)
            }
        }

        holder.communityCommentLayout.setOnLongClickListener { _ ->
            communityCommentListAdapterInterface.onLongClickComment(dataSet[position].author.id, dataSet[position].id, dataSet[position].contents)
            true
        }

        holder.loadReplyTextView.setOnClickListener {
            communityCommentListAdapterInterface.fetchReplyComment(pageIndices[position], topCommentIdList[position], dataSet[position].id, position)
            pageIndices[position] += 1
            notifyItemChanged(position)
        }
    }

    fun addItem(item: Comment){
        dataSet.add(item)

        // 기본값으로 추가
        pageIndices.add(0)
        topCommentIdList.add(-1)
    }

    fun addItemOnPosition(item: Comment, position: Int){
        dataSet.add(position, item)

        // 기본값으로 추가
        pageIndices.add(position, 0)
        topCommentIdList.add(position, -1)
    }

    fun setTopCommentIdList(id: Long, position: Int){
        topCommentIdList[position] = id
    }

    fun resetDataSet(){
        dataSet = arrayListOf()
        pageIndices = arrayListOf()
        topCommentIdList = arrayListOf()
    }
}