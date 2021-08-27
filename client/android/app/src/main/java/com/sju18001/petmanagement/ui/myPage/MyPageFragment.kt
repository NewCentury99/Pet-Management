package com.sju18001.petmanagement.ui.myPage

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.sju18001.petmanagement.R
import com.sju18001.petmanagement.controller.Util
import com.sju18001.petmanagement.databinding.FragmentMyPageBinding
import com.sju18001.petmanagement.restapi.RetrofitBuilder
import com.sju18001.petmanagement.restapi.SessionManager
import com.sju18001.petmanagement.restapi.dao.Account
import com.sju18001.petmanagement.restapi.dto.FetchAccountPhotoReqDto
import com.sju18001.petmanagement.restapi.dto.FetchAccountResDto
import okhttp3.MediaType
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MyPageFragment : Fragment() {
    private var _binding: FragmentMyPageBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    // variable for ViewModel
    val myPageViewModel: MyPageViewModel by activityViewModels()

    // variables for storing API call(for cancel)
    private var fetchAccountApiCall: Call<FetchAccountResDto>? = null
    private var fetchAccountPhotoApiCall: Call<ResponseBody>? = null

    // Account DAO
    private lateinit var accountData: Account

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMyPageBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onStart() {
        super.onStart()

        binding.accountLookup.setOnClickListener {
            val accountLookupIntent = Intent(context, MyPageActivity::class.java)
            accountLookupIntent.putExtra("fragmentType", "account_edit")
            accountLookupIntent.putExtra("id", accountData.id)
            accountLookupIntent.putExtra("username", accountData.username)
            accountLookupIntent.putExtra("email", accountData.email)
            accountLookupIntent.putExtra("phone", accountData.phone)
            accountLookupIntent.putExtra("marketing", accountData.marketing)
            accountLookupIntent.putExtra("nickname", accountData.nickname)
            accountLookupIntent.putExtra("userMessage", accountData.userMessage)

            if(accountData.photoUrl != null) {
                accountLookupIntent.putExtra("photoByteArray", myPageViewModel.accountPhotoProfileByteArray)
            }

            startActivity(accountLookupIntent)
            requireActivity().overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left)
        }

        binding.preferencesLookup.setOnClickListener {
            val preferencesLookupIntent = Intent(context, MyPageActivity::class.java)
            preferencesLookupIntent.putExtra("fragmentType", "preferences")
            startActivity(preferencesLookupIntent)
            requireActivity().overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left)
        }

        binding.notificationLookup.setOnClickListener {
            val notificationPreferencesIntent = Intent(context, MyPageActivity::class.java)
            notificationPreferencesIntent.putExtra("fragmentType", "notification_preferences")
            startActivity(notificationPreferencesIntent)
            requireActivity().overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left)
        }

        binding.themeLookup.setOnClickListener {
            val themePreferencesIntent = Intent(context, MyPageActivity::class.java)
            themePreferencesIntent.putExtra("fragmentType", "theme_preferences")
            startActivity(themePreferencesIntent)
            requireActivity().overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left)
        }

        binding.termsAndPoliciesLookup.setOnClickListener {
            val termsAndPoliciesIntent = Intent(context, MyPageActivity::class.java)
            termsAndPoliciesIntent.putExtra("fragmentType", "terms_and_policies")
            startActivity(termsAndPoliciesIntent)
            requireActivity().overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left)
        }
        binding.licenseLookup.setOnClickListener {
            val licenseIntent = Intent(context, MyPageActivity::class.java)
            licenseIntent.putExtra("fragmentType", "license")
            startActivity(licenseIntent)
            requireActivity().overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left)
        }
    }

    override fun onResume() {
        super.onResume()

        fetchAccountProfileData()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null

        // stop api call when fragment is destroyed
        fetchAccountApiCall?.cancel()
        fetchAccountPhotoApiCall?.cancel()
    }

    // fetch account profile
    private fun fetchAccountProfileData() {
        // create empty body
        accountData = SessionManager.fetchLoggedInAccount(requireContext())!!
        myPageViewModel.accountNicknameProfileValue = accountData.nickname!!

        // set views after API response
        setViewsWithAccountProfileData()
    }

    // fetch account photo
    private fun fetchAccountPhotoAndSetView() {
        // create DTO
        val fetchAccountPhotoReqDto = FetchAccountPhotoReqDto(null)

        fetchAccountPhotoApiCall = RetrofitBuilder.getServerApiWithToken(SessionManager.fetchUserToken(requireContext())!!)
            .fetchAccountPhotoReq(fetchAccountPhotoReqDto)
        fetchAccountPhotoApiCall!!.enqueue(object: Callback<ResponseBody> {
                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if(response.isSuccessful) {
                        // save in ViewModel by byte array
                        myPageViewModel.accountPhotoProfileByteArray = response.body()!!.byteStream().readBytes()
                        binding.accountPhoto.setImageBitmap(BitmapFactory.decodeByteArray(myPageViewModel.accountPhotoProfileByteArray, 0, myPageViewModel.accountPhotoProfileByteArray!!.size))
                    }
                    else {
                        // get error message + show(Toast)
                        val errorMessage = Util.getMessageFromErrorBody(response.errorBody()!!)

                        // if null: set to default image
                        // Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                        binding.accountPhoto.setImageDrawable(requireActivity().getDrawable(R.drawable.ic_baseline_account_circle_36))

                        // log error message
                        Log.d("error", errorMessage)
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    // show(Toast)/log error message
                    Toast.makeText(context, t.message.toString(), Toast.LENGTH_LONG).show()
                    Log.d("error", t.message.toString())
                }
            })
    }

    // set views for account profile
    private fun setViewsWithAccountProfileData() {
        fetchAccountPhotoAndSetView()
        binding.nicknameText.text = myPageViewModel.accountNicknameProfileValue
    }
}