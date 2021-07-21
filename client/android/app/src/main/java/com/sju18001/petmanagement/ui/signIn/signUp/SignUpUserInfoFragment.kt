package com.sju18001.petmanagement.ui.signIn.signUp

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import com.sju18001.petmanagement.R
import com.sju18001.petmanagement.databinding.FragmentSignUpUserInfoBinding
import com.sju18001.petmanagement.ui.signIn.SignInViewModel
import java.util.regex.Pattern

class SignUpUserInfoFragment : Fragment() {

    // pattern regex for EditTexts
    private val patternPhone: Pattern = Pattern.compile("^01(?:0|1|[6-9])(?:\\d{3}|\\d{4})\\d{4}$")
    //private val patternEmailCode: Pattern = Pattern.compile("^[0-9]{6}$")

    // variables for view binding
    private var _binding: FragmentSignUpUserInfoBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // view binding
        _binding = FragmentSignUpUserInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()

        // variable for ViewModel
        val signInViewModel: SignInViewModel by activityViewModels()

        // for state restore(ViewModel)
        restoreState(signInViewModel)

        // for phone text change listener
        binding.phoneEditText.addTextChangedListener(object: TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if(patternPhone.matcher(s).matches()) {
                    signInViewModel.signUpPhoneValid = true
                    binding.phoneMessage.visibility = View.GONE
                }
                else {
                    signInViewModel.signUpPhoneValid = false
                    binding.phoneMessage.visibility = View.VISIBLE
                }
                signInViewModel.signUpPhoneEditText = s.toString()
                signInViewModel.signUpPhoneIsOverlap = false
                binding.phoneMessageOverlap.visibility = View.GONE
                checkIsValid(signInViewModel)
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
        })

        // for email text change listener
        binding.emailEditText.addTextChangedListener(object: TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if(Patterns.EMAIL_ADDRESS.matcher(s).matches()) {
                    signInViewModel.signUpEmailValid = true
                    binding.emailMessage.visibility = View.GONE
                }
                else {
                    signInViewModel.signUpEmailValid = false
                    binding.emailMessage.visibility = View.VISIBLE
                }
                signInViewModel.signUpEmailEditText = s.toString()
                signInViewModel.signUpEmailIsOverlap = false
                binding.emailMessageOverlap.visibility = View.GONE
                checkIsValid(signInViewModel)
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun checkIsValid(signInViewModel: SignInViewModel) {
        if(signInViewModel.signUpPhoneValid && signInViewModel.signUpEmailValid) {
            (parentFragment as SignUpFragment).enableNextButton()
        }
        else{
            (parentFragment as SignUpFragment).disableNextButton()
        }
    }

    public fun showMessage(signInViewModel: SignInViewModel) {
        if(signInViewModel.signUpPhoneIsOverlap) {
            binding.phoneMessageOverlap.visibility = View.VISIBLE
        }
        if(signInViewModel.signUpEmailIsOverlap) {
            binding.emailMessageOverlap.visibility = View.VISIBLE
        }
    }

    private fun restoreState(signInViewModel: SignInViewModel) {
        binding.phoneEditText.setText(signInViewModel.signUpPhoneEditText)
        binding.emailEditText.setText(signInViewModel.signUpEmailEditText)

        if(!signInViewModel.signUpPhoneValid && signInViewModel.signUpPhoneEditText != "") {
            binding.phoneMessage.visibility = View.VISIBLE
        }
        if(!signInViewModel.signUpEmailValid && signInViewModel.signUpEmailEditText != "") {
            binding.emailMessage.visibility = View.VISIBLE
        }
        if(signInViewModel.signUpPhoneIsOverlap) {
            binding.phoneMessageOverlap.visibility = View.VISIBLE
        }
        if(signInViewModel.signUpEmailIsOverlap) {
            binding.emailMessageOverlap.visibility = View.VISIBLE
        }

        (parentFragment as SignUpFragment).showPreviousButton()
        checkIsValid(signInViewModel)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}