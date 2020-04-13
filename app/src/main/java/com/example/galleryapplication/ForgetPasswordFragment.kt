package com.example.galleryapplication

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.fragment_forget_password.*
import kotlinx.android.synthetic.main.fragment_forget_password.view.*

/**
 * A simple [Fragment] subclass.
 */
class ForgetPasswordFragment : Fragment() {
    private var mAuth: FirebaseAuth? = null
    private var email: String? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val output: View = inflater.inflate(R.layout.fragment_forget_password, container, false)
        mAuth = FirebaseAuth.getInstance()


        output.btn_reset_password.setOnClickListener(View.OnClickListener {
            email = output.user_email.text.toString()
            if (TextUtils.isEmpty(email)) {
                Toast.makeText(context, "Enter your registered email id", Toast.LENGTH_SHORT).show()
            }
            output.progressbar.setVisibility(View.VISIBLE)
            mAuth!!.sendPasswordResetEmail(email!!).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(
                        context,
                        "We have sent you instructions to reset your password!",
                        Toast.LENGTH_SHORT
                    ).show()

                } else {
                    Toast.makeText(
                        context, "Failed to send reset email!", Toast.LENGTH_SHORT).show()
                }
                output.progressbar.setVisibility(View.GONE)

            }

        })
        return output
    }

}
