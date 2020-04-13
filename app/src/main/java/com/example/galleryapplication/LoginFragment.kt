package com.example.galleryapplication

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.*
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.android.synthetic.main.fragment_login.*
import kotlinx.android.synthetic.main.fragment_login.view.*

/**
 * A simple [Fragment] subclass.
 */
class LoginFragment : Fragment() {
    private lateinit var mAuth: FirebaseAuth
    private lateinit var listener: FirebaseAuth.AuthStateListener
    private lateinit var email: String
    private lateinit var password: String
    private val TAG: String = "LoginFragment"
    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private val RC_SIGN_IN: Int = 123
    private val forgetPasswordFragment = ForgetPasswordFragment()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val output: View = inflater.inflate(R.layout.fragment_login, container, false)

        mAuth = FirebaseAuth.getInstance()

        if (mAuth.currentUser != null) {
            startActivity(Intent(context, GalleryActivity::class.java))
        }

        output.button_signIn.setOnClickListener {
            email = login_email.text.toString()
            password = login_password.text.toString()
            if (TextUtils.isEmpty(email))
                Toast.makeText(context, "Please enter Email id", Toast.LENGTH_SHORT).show()
            else if (TextUtils.isEmpty(password))
                Toast.makeText(context, "Please enter Password", Toast.LENGTH_SHORT).show()

            //authenticate user
            mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d(TAG, "signInWithEmail:success")
                        startActivity(Intent(context, GalleryActivity::class.java))
                    } else {
                        Log.d(TAG, "signInWithEmail:Failed")
                        Toast.makeText(context, "Failed", Toast.LENGTH_SHORT).show()
                    }
                }
        }

        output.forget.setOnClickListener {

             val transaction = fragmentManager!!.beginTransaction()
            transaction.replace(R.id.container,forgetPasswordFragment,"ForgetFragment").commit()
            transaction.addToBackStack(null)

        }

        output.login_Google.setOnClickListener {
            signIn()

        }
        val gso: GoogleSignInOptions =
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)).requestEmail().build()
        mGoogleSignInClient = GoogleSignIn.getClient(context!!, gso)

        listener = FirebaseAuth.AuthStateListener { firebaseAuth: FirebaseAuth ->
            val user: FirebaseUser? = firebaseAuth.currentUser
            if (user != null)
                Log.d(TAG, "onAuthStateChanged:signed_in:" + user.uid)
            else {
                Log.d(TAG, "onAuthStateChanged:signed_out")

            }

        }

        return output
    }


    override fun onStart() {
        super.onStart()
        mAuth.addAuthStateListener { listener }
    }

    override fun onStop() {
        super.onStop()
        mAuth.removeAuthStateListener { listener }
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
        Log.d(TAG, "firebaseAuthWithGoogle:" + account!!.id)
        Log.d("firebaseAuthWithGoogle:", account.photoUrl.toString())

        val authCredential: AuthCredential = GoogleAuthProvider.getCredential(account.idToken, null)
        mAuth.signInWithCredential(authCredential).addOnCompleteListener { task ->

            if (task.isSuccessful) {
                var intent = Intent(context, GalleryActivity::class.java)
                intent.putExtra("emailId", account.email)
                intent.putExtra("profilePic", account.photoUrl)
                startActivity(intent)

            } else {
                Log.e(TAG, "signInCredential : Failure", task.exception)
                Toast.makeText(context, "AUTHENTICATION ERROR", Toast.LENGTH_SHORT)
                    .show()
            }


        }
    }


}
