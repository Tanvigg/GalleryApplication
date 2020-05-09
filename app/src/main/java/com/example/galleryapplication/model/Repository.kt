package com.example.galleryapplication.model

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.AuthCredential

class Repository {
    private var firebaseModel = FirebaseModel()

    fun login(email: String, password: String) = firebaseModel.login(email, password)

    fun loginWithGoogle(authCredential: AuthCredential) =
        firebaseModel.loginWithGoogle(authCredential)

    fun uploadUserToFirebase(photoUrl: Uri?, displayName: String?, email: String?) = firebaseModel.uploadUserToFirebase(photoUrl,displayName,email)

    fun signUp(name: String, email: String, password: String, userImage: Uri?): LiveData<Result<Boolean>>{
        val result : MutableLiveData<Result<Boolean>> = MutableLiveData()
        firebaseModel.signUp(name, email, password, userImage).observeForever {
            it.onSuccess {
                result.value = Result.success(it)
            }
            it.onFailure {
                result.value = Result.failure(it)
            }
        }

        return result
    }


    fun passwordReset(email: String) =  firebaseModel.passwordReset(email)

    fun fetchUserDetails() = firebaseModel.fetchUserDetails()

    fun logout() = firebaseModel.logout()

    fun updateUserprofile(selectedPhotoUri: Uri?): LiveData<Result<Boolean>> {
        val result: MutableLiveData<Result<Boolean>> = MutableLiveData()
        firebaseModel.updateUserProfile(selectedPhotoUri!!).observeForever {
            it.onSuccess {
                result.value = Result.success(it)
            }
            it.onFailure {
                result.value = Result.failure(it)
            }
        }
        return result
    }

    fun addCategory(categoryName:String, selectedPhotoUri: Uri):LiveData<Result<Boolean>>{
        val result : MutableLiveData<Result<Boolean>> = MutableLiveData()
        firebaseModel.addCategory(categoryName,selectedPhotoUri).observeForever{
            it.onSuccess{
                result.value = Result.success(it)
            }
            it.onFailure{
                result.value  = Result.failure(it)
            }
        }
        return result
        }



    fun fetchCategories() = firebaseModel.fetchCategories()

    fun addPhotos(selectedPhotoUri: Uri,timeInMilis:String,date:String,categoryName: String):LiveData<Result<Boolean>>{
        val result : MutableLiveData<Result<Boolean>> = MutableLiveData()
        firebaseModel.addPhotos(selectedPhotoUri,timeInMilis,date,categoryName).observeForever{
            it.onSuccess{
                result.value = Result.success(it)
            }
            it.onFailure{
                result.value  = Result.failure(it)
            }
        }
        return result
    }

    fun fetchPhotos(categoryName: String) = firebaseModel.fetchPhotos(categoryName)

    fun deleteImage(image : String,categoryName: String,timeInMilis: String) = firebaseModel.deleteImage(image,categoryName,timeInMilis)

    fun fetchTimeLine() = firebaseModel.fetchTimeLine()


}