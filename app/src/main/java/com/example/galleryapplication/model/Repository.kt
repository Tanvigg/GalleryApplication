package com.example.galleryapplication.model

import android.net.Uri
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.auth.AuthCredential

class Repository {
    private var firebaseModel = FirebaseModel()

    fun login(email: String, password: String) = firebaseModel.login(email, password)

    fun loginWithGoogle(authCredential: AuthCredential) =
        firebaseModel.loginWithGoogle(authCredential)

    fun signUp(name: String, email: String, password: String, userImage: Uri) =
        firebaseModel.signUp(name, email, password, userImage)
}