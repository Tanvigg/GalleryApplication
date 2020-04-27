package com.example.galleryapplication.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import com.example.galleryapplication.model.Repository
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.AuthResult

class FirebaseViewModel : ViewModel() {
    private val repository = Repository()


    fun login(email: String, password: String): Task<AuthResult> {
        return repository.login(email, password)
    }

    fun loginWithGoogle(authCredential: AuthCredential): Task<AuthResult> {
        return repository.loginWithGoogle(authCredential)
    }

    fun signUp(name: String, email: String, password: String, userImage: Uri):Boolean{
        return repository.signUp(name, email, password, userImage)
    }




}