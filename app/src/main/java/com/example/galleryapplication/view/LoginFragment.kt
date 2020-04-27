package com.example.galleryapplication.view

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.galleryapplication.ForgetPasswordFragment
import com.example.galleryapplication.GalleryActivity
import com.example.galleryapplication.R
import com.example.galleryapplication.viewmodel.FirebaseViewModel
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.*
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.android.synthetic.main.fragment_login.*
import kotlinx.android.synthetic.main.fragment_login.progressbar
import kotlinx.android.synthetic.main.fragment_login.view.*

/**
 * A simple [Fragment] subclass.
 */
class LoginFragment : Fragment() {
    private lateinit var mAuth: FirebaseAuth
    private val TAG: String = "LoginFragment"
    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private val RC_SIGN_IN: Int = 123
    private val forgetPasswordFragment = ForgetPasswordFragment()
    private lateinit var viewModel: FirebaseViewModel


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val output: View = inflater.inflate(R.layout.fragment_login, container, false)

        output.button_signIn.setOnClickListener {
            val email = login_email.text.toString()
            val password = login_password.text.toString()

            if (TextUtils.isEmpty(email)) {
                login_email.setError("Email can't be blank")

            } else if (TextUtils.isEmpty(password)) {
                login_password.setError("Please enter Password")

            } else {
                progressbar.visibility = View.VISIBLE
                //authenticate user
                viewModel = ViewModelProvider(this).get(FirebaseViewModel::class.java)
                viewModel.login(email, password)
                    .addOnSuccessListener {
                        Toast.makeText(context, "Welcome!", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(context, GalleryActivity::class.java))
                        activity!!.finish()
                    }

                    .addOnFailureListener {
                        Log.d(TAG, "signInWithEmail:Failed")
                        Toast.makeText(context, "Failed", Toast.LENGTH_SHORT).show()
                    }
            }
        }



        output.forget.setOnClickListener {
            activity!!.supportFragmentManager.beginTransaction()
                .replace(R.id.container, forgetPasswordFragment, "ForgetFragment")
                .addToBackStack(null)
                .commit()

        }

        output.login_Google.setOnClickListener {
            signIn()

        }
        val gso: GoogleSignInOptions =
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)).requestEmail().build()
        mGoogleSignInClient = GoogleSignIn.getClient(context!!, gso)
        return output

    }

    override fun onStart() {
        super.onStart()
        if (mAuth.currentUser != null) {
            startActivity(Intent(context, GalleryActivity::class.java))
            activity!!.finish()
        }
    }


    private fun signIn() {
        val intent: Intent = mGoogleSignInClient!!.signInIntent
        startActivityForResult(intent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val result: GoogleSignInResult? =
                Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result!!.isSuccess) {
                val account: GoogleSignInAccount? = result.signInAccount
                firebaseAuthWithGoogle(account)
            } else {
                Log.e(TAG, "Google sign in failed")

            }
        }
    }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount?) {
        val authCredential: AuthCredential =
            GoogleAuthProvider.getCredential(account!!.idToken, null)
        viewModel = ViewModelProvider(this).get(FirebaseViewModel::class.java)
        viewModel.loginWithGoogle(authCredential)
            .addOnCompleteListener { task ->

                if (task.isSuccessful) {
                    val intent = Intent(context, GalleryActivity::class.java)
                    intent.putExtra("emailId", account.email)
                    intent.putExtra("profilePic", account.photoUrl)
                    startActivity(intent)

                } else {
                    Toast.makeText(context, "AUTHENTICATION ERROR", Toast.LENGTH_SHORT)
                        .show()

                }
            }
    }
}
