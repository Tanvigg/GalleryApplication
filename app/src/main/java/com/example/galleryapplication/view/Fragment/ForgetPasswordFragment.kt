package com.example.galleryapplication.view.Fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.galleryapplication.R
import com.example.galleryapplication.view.hide
import com.example.galleryapplication.view.show
import com.example.galleryapplication.viewmodel.LoginViewModel
import com.example.galleryapplication.viewmodel.ForgetPasswordViewModel
import kotlinx.android.synthetic.main.fragment_forget_password.*
import kotlinx.android.synthetic.main.fragment_forget_password.view.*

/**
 * A simple [Fragment] subclass.
 */
class ForgetPasswordFragment : Fragment() {
    private val viewModel: ForgetPasswordViewModel by lazy {
        ViewModelProvider(this).get(ForgetPasswordViewModel::class.java)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val output: View = inflater.inflate(R.layout.fragment_forget_password, container, false)
        output.btn_reset_password.setOnClickListener(View.OnClickListener {
            val email = output.user_email.text.toString()
            viewModel.passwordReset(email)
        })
        setObservers()
        return output
    }

    private fun setObservers() {
        viewModel.getEmailError().observe(viewLifecycleOwner, Observer {
            user_email.error = it
        })

        viewModel.getLoginState().observe(viewLifecycleOwner, Observer {
            when (it) {
                LoginViewModel.LoginState.SHOW_PROGRESS -> progressbar_forgetPass.show()
                LoginViewModel.LoginState.HIDE_PROGRESS -> progressbar_forgetPass.hide()
            }
        })

    }
}
