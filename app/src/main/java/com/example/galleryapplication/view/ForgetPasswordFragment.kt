package com.example.galleryapplication.view

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.galleryapplication.R
import com.example.galleryapplication.viewmodel.FirebaseViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.fragment_forget_password.view.*

/**
 * A simple [Fragment] subclass.
 */
class ForgetPasswordFragment : Fragment() {
    private lateinit var viewModel: FirebaseViewModel



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val output: View = inflater.inflate(R.layout.fragment_forget_password, container, false)

        output.btn_reset_password.setOnClickListener(View.OnClickListener {
            val email = output.user_email.text.toString()

            if (TextUtils.isEmpty(email)) {
                Toast.makeText(context, "Enter your registered email id", Toast.LENGTH_SHORT).show()
            }

            output.progressbar.visibility = View.VISIBLE
            viewModel = ViewModelProvider(this).get(FirebaseViewModel::class.java)
            viewModel.passwordReset(email)
            output.progressbar.setVisibility(View.GONE)
        })
        return output
    }

}
