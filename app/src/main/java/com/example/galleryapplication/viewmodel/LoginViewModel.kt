package com.example.galleryapplication.viewmodel

import android.app.Application
import android.content.Intent
import android.net.Uri
import android.text.TextUtils
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.galleryapplication.model.Repository
import com.example.galleryapplication.view.*
import com.example.galleryapplication.view.Fragment.RC_SIGN_IN
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInResult
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.GoogleAuthProvider

class LoginViewModel(val context: Application) : AndroidViewModel(context) {
    private val repository = Repository()
    private var emailError = MutableLiveData<String>()
    private var passwordError = MutableLiveData<String>()
    private var loginState = MutableLiveData<LoginState>()
    lateinit var account: GoogleSignInAccount

    fun getEmailError(): LiveData<String> {
        return emailError
    }

    fun getPasswordError(): LiveData<String> {
        return passwordError
    }

    fun getLoginState(): LiveData<LoginState> {
        return loginState
    }


    fun login(email: String, password: String) {
        if (TextUtils.isEmpty(email)) {
            emailError.value = "Email can't be blank"
        } else if (TextUtils.isEmpty(password)) {
            passwordError.value = "Please enter Password"

        } else {
            loginState.value = LoginState.SHOW_PROGRESS
            //authenticate user
            repository.login(email, password).addOnSuccessListener {
                context.showToast("Welcome")
                loginState.value = LoginState.GO_TO_HOMEPAGE
            }

                .addOnFailureListener {
                    //Log.d(TAG, "signInWithEmail:Failed")
                    context.showToast("Failed")
                    loginState.value = LoginState.HIDE_PROGRESS
                }
        }

    }


    enum class LoginState {
        SHOW_PROGRESS,
        HIDE_PROGRESS,
        GO_TO_HOMEPAGE,

    }


    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == RC_SIGN_IN) {
            val result: GoogleSignInResult? =
                Auth.GoogleSignInApi.getSignInResultFromIntent(data)
            if (result!!.isSuccess) {
                account = result.signInAccount!!
                firebaseAuthWithGoogle(account)
            } else {
                context.showToast("Google Sign in Failed")

            }
        }

    }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount?) {
        val authCredential: AuthCredential =
            GoogleAuthProvider.getCredential(account!!.idToken, null)
        repository.loginWithGoogle(authCredential).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                uploadUserToFirebase(account.photoUrl,account.displayName,account.email)
                loginState.value = LoginState.GO_TO_HOMEPAGE
            } else {
                context.showToast("Authentication Error")
            }
        }
    }

    private fun uploadUserToFirebase(photoUrl: Uri?, displayName: String?, email: String?) {
        repository.uploadUserToFirebase(photoUrl,displayName,email)


    }
}