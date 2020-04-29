package com.example.galleryapplication.model

import android.net.Uri
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.AuthResult
import com.google.firebase.firestore.DocumentReference

class Repository {
    private var firebaseModel = FirebaseModel()

    fun login(email: String, password: String) = firebaseModel.login(email, password)

    fun loginWithGoogle(authCredential: AuthCredential) =
        firebaseModel.loginWithGoogle(authCredential)

    fun signUp(name: String, email: String, password: String, userImage: Uri) =
        firebaseModel.signUp(name, email, password, userImage)

    fun passwordReset(email: String) =  firebaseModel.passwordReset(email)

    fun fetchUserDetails() = firebaseModel.fetchUserDetails()

    fun logout() = firebaseModel.logout()

    fun updateUserprofile(selectedPhotoUri : Uri?)  = firebaseModel.updateUserProfile(
        selectedPhotoUri!!
    )

    fun addCategory(categoryName :String, selectedPhotoUri: Uri) = firebaseModel.addCategory(categoryName,selectedPhotoUri)

    fun fetchCategories() = firebaseModel.fetchCategories()

    fun addPhotos(selectedPhotoUri: Uri,timeInMilis:String,date:String,categoryName: String) = firebaseModel.addPhotos(selectedPhotoUri,timeInMilis,date,categoryName)

    fun fetchPhotos(categoryName: String) = firebaseModel.fetchPhotos(categoryName)

    fun deleteImage(image : String,categoryName: String,timeInMilis: String) = firebaseModel.deleteImage(image,categoryName,timeInMilis)

    fun fetchTimeLine() = firebaseModel.fetchTimeLine()

}