package com.sju18001.petmanagement.ui.login.recovery

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.sju18001.petmanagement.R
import com.sju18001.petmanagement.controller.Util
import com.sju18001.petmanagement.databinding.FragmentRecoverUsernameBinding
import com.sju18001.petmanagement.restapi.RetrofitBuilder
import com.sju18001.petmanagement.restapi.dto.RecoverUsernameReqDto
import com.sju18001.petmanagement.restapi.dto.RecoverUsernameResDto
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RecoverUsernameFragment : Fragment() {
    private var _binding: FragmentRecoverUsernameBinding? = null
    private val binding get() = _binding!!

    private val INPUT_LENGTH = 1
    private val EMAIL = 0
    private val isValidInput: HashMap<Int, Boolean> = HashMap()

    private var isViewDestroyed: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRecoverUsernameBinding.inflate(inflater, container, false)

        if(savedInstanceState?.getBoolean("is_result_shown") == true){
            val username = savedInstanceState.getString("result_username")
            setViewForResult(username)
        }

        return binding.root
    }

    override fun onStart() {
        super.onStart()

        // initialize valid input map
        for(i in 0 until INPUT_LENGTH) { isValidInput[i] = false }
        checkEmailValidation(binding.emailEditText.text)
        checkIsValid()
        setMessageGone()

        // 아이디 찾기 버튼 클릭
        binding.recoverUsernameButton.setOnClickListener{
            activity?.let { Util.hideKeyboard(it) }

            recoverUsername(binding.emailEditText.text.toString())
        }

        // 레이아웃 클릭
        binding.recoverUsernameLayout.setOnClickListener{
            activity?.let { Util.hideKeyboard(it) }
        }

        // 이메일 입력란 입력
        binding.emailEditText.addTextChangedListener(object: TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                checkEmailValidation(s)
                checkIsValid()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        if(binding.resultLayout.visibility == View.VISIBLE){
            outState.putBoolean("is_result_shown", true)
            outState.putString("result_username", binding.resultUsername.text.toString())
        }else{
            outState.putBoolean("is_result_shown", false)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null

        isViewDestroyed = true
    }


    // * 유효성 검사
    private fun checkEmailValidation(s: CharSequence?){
        if(Patterns.EMAIL_ADDRESS.matcher(s).matches()) {
            isValidInput[EMAIL] = true
            binding.emailMessage.visibility = View.GONE
        }
        else {
            isValidInput[EMAIL] = false
            binding.emailMessage.visibility = View.VISIBLE
        }
    }

    private fun checkIsValid() {
        for(i in 0 until INPUT_LENGTH) {
            if(!isValidInput[i]!!) {
                // if not valid -> disable button + return
                binding.recoverUsernameButton.isEnabled = false
                return
            }
        }

        // if all is valid -> enable button
        binding.recoverUsernameButton.isEnabled = true
    }

    private fun setMessageGone() {
        binding.emailMessage.visibility = View.GONE
    }


    // 아이디 찾기
    private fun recoverUsername(email: String){
        val recoverUsernameReqDto = RecoverUsernameReqDto(email)
        val call = RetrofitBuilder.getServerApi().recoverUsernameReq(recoverUsernameReqDto)

        // 버튼 로딩 상태
        setButtonLoading(true)

        call.enqueue(object: Callback<RecoverUsernameResDto> {
            override fun onResponse(
                call: Call<RecoverUsernameResDto>,
                response: Response<RecoverUsernameResDto>
            ) {
                if(!isViewDestroyed){
                    // 버튼 로딩 상태 해제
                    setButtonLoading(false)

                    if(response.isSuccessful){
                        response.body()?.let{
                            setViewForResult(it.username)
                        }
                    }else{
                        Toast.makeText(context, Util.getMessageFromErrorBody(response.errorBody()!!), Toast.LENGTH_LONG).show()
                    }
                }
            }

            override fun onFailure(call: Call<RecoverUsernameResDto>, t: Throwable) {
                if(!isViewDestroyed){
                    // 버튼 로딩 상태 해제
                    setButtonLoading(false)

                    Toast.makeText(context, "요청에 실패하였습니다.", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun setButtonLoading(isLoading: Boolean){
        if(isLoading){
            binding.recoverUsernameButton.apply {
                text = ""
                isEnabled = false
            }
            binding.recoverUsernameProgressBar.visibility = View.VISIBLE
        }else{
            binding.recoverUsernameButton.apply {
                text = context?.getText(R.string.recover_username)
                isEnabled = true
            }
            binding.recoverUsernameProgressBar.visibility = View.GONE
        }
    }

    // 아이디 찾기 결과
    private fun setViewForResult(username: String?){
        binding.resultUsername.text = username
        binding.recoverUsernameLayout.visibility = View.GONE
        binding.resultLayout.visibility = View.VISIBLE
    }
}