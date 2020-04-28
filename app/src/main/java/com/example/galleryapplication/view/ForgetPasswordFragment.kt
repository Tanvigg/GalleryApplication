package com.example.galleryapplication.view

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.galleryapplication.R
import com.example.galleryapplication.viewmodel.FirebaseViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.fragment_forget_password.*
import kotlinx.android.synthetic.main.fragment_forget_password.view.*
import kotlinx.android.synthetic.main.fragment_login.*
import kotlinx.android.synthetic.main.fragment_sign_up.*

/**
 * A simple [Fragment] subclass.
 */
class ForgetPasswordFragment : Fragment() {
    private val viewModel: FirebaseViewModel by lazy {
        ViewModelProvider(this).get(FirebaseViewModel::class.java)
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
        viewModel.getEmailError().observe(this, Observer {
            user_email.error = it
        })

        viewModel.getLoginState().observe(this, Observer {
            when (it) {
                FirebaseViewModel.LoginState.SHOW_PROGRESS -> progressbar_forgetPass.show()
                FirebaseViewModel.LoginState.HIDE_PROGRESS -> progressbar_forgetPass.hide()
            }
        })

    }
}
