package com.example.galleryapplication.view.fragment

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.galleryapplication.R
import com.example.galleryapplication.model.hide
import com.example.galleryapplication.model.show
import com.example.galleryapplication.view.activity.GalleryActivity
import com.example.galleryapplication.viewmodel.LoginViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.fragment_login.*
import kotlinx.android.synthetic.main.fragment_login.view.*

const val RC_SIGN_IN = 123
class LoginFragment : Fragment(), View.OnClickListener {
    private var mAuth: FirebaseAuth? = null
    var TAG = LoginFragment::class.java.name
    private var isGoogleSignUp = 0
    private var sharedPreferences: SharedPreferences? = null

    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private val forgetPasswordFragment =
        ForgetPasswordFragment()
    private val viewModel: LoginViewModel by lazy {
        ViewModelProvider(this).get(LoginViewModel::class.java) }


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
            login_Google -> {
                isGoogleSignUp = 1
                sharedPreferences =
                    context!!.getSharedPreferences("mySharedPreference", Context.MODE_PRIVATE)
                val editor = sharedPreferences!!.edit()
                editor.putInt("isGoogleSignUp", isGoogleSignUp)
                editor.apply()
                signIn()

            }
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
        startActivityForResult(intent,
            RC_SIGN_IN
        )
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
                LoginViewModel.LoginState.SHOW_PROGRESS -> progressbar.show()
                LoginViewModel.LoginState.HIDE_PROGRESS -> progressbar.hide()
                LoginViewModel.LoginState.GO_TO_HOMEPAGE -> {
                    startActivity(Intent(context, GalleryActivity::class.java))
                    activity!!.finish()
                }
            }
        })
    }
}
