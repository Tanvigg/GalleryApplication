package com.example.galleryapplication.view

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.galleryapplication.R
import com.example.galleryapplication.viewmodel.FirebaseViewModel
import com.google.android.gms.auth.api.signin.*
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.fragment_login.*
import kotlinx.android.synthetic.main.fragment_login.progressbar
import kotlinx.android.synthetic.main.fragment_login.view.*

const val RC_SIGN_IN = 123
class LoginFragment : Fragment(), View.OnClickListener {
    private var mAuth: FirebaseAuth? = null
    var TAG = LoginFragment::class.java.name
    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private val forgetPasswordFragment = ForgetPasswordFragment()
    private val viewModel: FirebaseViewModel by lazy {
        ViewModelProvider(this).get(FirebaseViewModel::class.java) }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val output: View = inflater.inflate(R.layout.fragment_login, container, false)
        mAuth = FirebaseAuth.getInstance()

        output.button_signIn.setOnClickListener(this)
        output.forget.setOnClickListener(this)
        output.login_Google.setOnClickListener(this)

        val gso: GoogleSignInOptions =
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)).requestEmail().build()
        mGoogleSignInClient = GoogleSignIn.getClient(context!!, gso)
        setObservers()
        return output
    }

    override fun onClick(v: View?) {
        when (v) {
            button_signIn -> {
                val email = login_email.text.toString()
                val password = login_password.text.toString()
                viewModel.login(email, password)

            }
            forget -> {
                activity!!.supportFragmentManager.beginTransaction()
                    .replace(R.id.container, forgetPasswordFragment, "ForgetFragment")
                    .addToBackStack(null)
                    .commit()
            }
            login_Google -> signIn()
        }
    }

    override fun onStart() {
        super.onStart()
        if (mAuth != null && mAuth!!.currentUser != null) {
            startActivity(Intent(context, GalleryActivity::class.java))
            activity!!.finish()
        }
    }

    private fun signIn() {
        val intent: Intent = mGoogleSignInClient.signInIntent
        startActivityForResult(intent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        viewModel.onActivityResult(requestCode, resultCode, data)
    }

    private fun setObservers() {
        viewModel.getEmailError().observe(viewLifecycleOwner, Observer {
            login_email.error = it
        })
        viewModel.getPasswordError().observe(viewLifecycleOwner, Observer {
            login_password.error = it
        })
        viewModel.getLoginState().observe(viewLifecycleOwner, Observer {
            when (it) {
                FirebaseViewModel.LoginState.SHOW_PROGRESS -> progressbar.show()
                FirebaseViewModel.LoginState.HIDE_PROGRESS -> progressbar.hide()
                FirebaseViewModel.LoginState.GO_TO_HOMEPAGE -> {
                    startActivity(Intent(context, GalleryActivity::class.java))
                    activity!!.finish()
                }
            }
        })
    }
}
