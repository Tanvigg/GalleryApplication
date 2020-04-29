package com.example.galleryapplication.model

import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.constraintlayout.solver.widgets.Snapshot
import com.example.galleryapplication.R
import com.example.galleryapplication.view.Category
import com.example.galleryapplication.view.Photos
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import org.w3c.dom.Document
import java.util.*
import kotlin.collections.HashMap


class FirebaseModel {
    private val auth = FirebaseAuth.getInstance()
    private val TAG = "Firebase Model"
    private var db = FirebaseFirestore.getInstance()
    private lateinit var userProfileImageRef: StorageReference
    private lateinit var userCategoryImagReference: StorageReference
    private lateinit var userPhotosReference: StorageReference
    private lateinit var photosReference: StorageReference


    private lateinit var profileImageUrl: String
    private lateinit var categoryImageUrl: String
    private lateinit var imageUrl: String
    private lateinit var currentUserId: String

    private var userHashMap: HashMap<String, String> = HashMap()
    private lateinit var category: Category
    private lateinit var photos: Photos


    fun login(email: String, password: String): Task<AuthResult> {
        val fAuth: Task<AuthResult> = auth.signInWithEmailAndPassword(email, password)
        return fAuth
    }

    fun loginWithGoogle(authCredential: AuthCredential): Task<AuthResult> {
        val fAuth: Task<AuthResult> = auth.signInWithCredential(authCredential)
        return fAuth
    }

    fun signUp(name: String, email: String, password: String, userImage: Uri): Boolean {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    uploadUser(userImage, name, email)
                }
            }
        return true
    }

    fun uploadUser(userImage: Uri, name: String, email: String) {
        currentUserId = auth.currentUser!!.uid

        userProfileImageRef = FirebaseStorage.getInstance().reference.child("Profile Images")

        val filePath: StorageReference =
            userProfileImageRef.child("image" + userImage.lastPathSegment)
        filePath.putFile(userImage).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                filePath.downloadUrl.addOnCompleteListener(OnCompleteListener { task ->
                    profileImageUrl = task.result.toString()
                    saveUserDataToFireStore(name, email)
                })
            } else {
                Log.d(TAG, "profile image upload Failed")

            }
        }
    }

    private fun saveUserDataToFireStore(name: String, email: String) {
        userHashMap.put("Name", name)
        userHashMap.put("Email", email)
        if (profileImageUrl != null) {
            userHashMap.put("ProfileImage", profileImageUrl)
        } else {
            val uri: Uri =
                Uri.parse("android.resource://com.example.galleryapplication/" + R.drawable.profile_image.toString())
            userHashMap.put("ProfileImage", uri.toString())

        }
        db.collection("users").document(currentUserId).set(userHashMap)
    }

    fun passwordReset(email: String): Task<Void> {
        val fAuth: Task<Void> = auth.sendPasswordResetEmail(email)
        return fAuth
    }

    fun fetchUserDetails(): Task<DocumentSnapshot> {
        currentUserId = auth.currentUser!!.uid
        val documentReference: Task<DocumentSnapshot> =
            db.collection("users").document(currentUserId).get()
        return documentReference
    }


    fun updateUserProfile(selectedPhotoUri: Uri) {
        userProfileImageRef = FirebaseStorage.getInstance().reference.child("Profile Images")
        val filePath: StorageReference =
            userProfileImageRef.child("image" + selectedPhotoUri.lastPathSegment)
        filePath.putFile(selectedPhotoUri).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                filePath.downloadUrl.addOnCompleteListener { task ->
                    profileImageUrl = task.result.toString()
                    updateDatabase(profileImageUrl)
                }
            } else {
                Log.d(TAG, "profile image upload Failed")

            }
        }
    }


    private fun updateDatabase(newImage: String) {
        currentUserId = auth.currentUser!!.uid
        db.collection("users").document(currentUserId)
            .update("ProfileImage", newImage).addOnSuccessListener {
                Log.i("updated successfully", "$it")

            }
            .addOnFailureListener {
                Log.i("Failed", "$it")

            }
    }


    fun logout() {
        auth.signOut()
    }

    fun addCategory(categoryName: String, selectedPhotoUri: Uri) {
        userCategoryImagReference = FirebaseStorage.getInstance().reference.child("Category Images")
        val filePath: StorageReference =
            userCategoryImagReference.child("image" + selectedPhotoUri.lastPathSegment)
        filePath.putFile(selectedPhotoUri).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                filePath.downloadUrl.addOnCompleteListener { task ->
                    categoryImageUrl = task.result.toString()
                    Log.d("image", categoryImageUrl)
                    saveCategoryToFireStore(categoryName)
                }
            } else {
                Log.d(TAG, "image upload Failed")
            }
        }

    }


    private fun saveCategoryToFireStore(categoryName: String) {
        currentUserId = auth.currentUser!!.uid
        if (categoryImageUrl != null)
            category = Category(categoryName, categoryImageUrl)
        else
            category = Category(
                categoryName,
                Uri.parse("android.resource://com.example.galleryapplication/" + R.drawable.profile_image.toString()).toString()
            )

        db.collection("users").document(currentUserId).collection("category").document(categoryName)
            .set(category)
    }

    fun fetchCategories(): CollectionReference {
        currentUserId = auth.currentUser!!.uid
        val collectionReference =
            db.collection("users").document(currentUserId).collection("category")
        return collectionReference
    }

    fun addPhotos(selectedPhotoUri: Uri, timeInMilis: String, date: String, categoryName: String) {
        currentUserId = auth.currentUser!!.uid
        userPhotosReference = FirebaseStorage.getInstance().getReference("CategoryImages/$currentUserId")
        val filePath: StorageReference = userPhotosReference.child("image" + selectedPhotoUri.lastPathSegment)
        filePath.putFile(selectedPhotoUri).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                filePath.downloadUrl.addOnCompleteListener { task ->
                    imageUrl = task.result.toString()
                    Log.d("image", imageUrl)
                    saveImagesToFireStore(timeInMilis, date, categoryName)
                }
            } else {
                Log.d(TAG, "image upload Failed")
            }
        }
    }

    private fun saveImagesToFireStore(timeInMilis: String, date: String, categoryName: String) {
        currentUserId = auth.currentUser!!.uid
        photos = Photos(imageUrl, timeInMilis, date)
        db.collection("users").document(currentUserId).collection("category").document(categoryName)
            .collection("images")
            .document(timeInMilis).set(photos)
    }

    fun fetchPhotos(categoryName: String): CollectionReference {
        currentUserId = auth.currentUser!!.uid
        var collectionReference =
            db.collection("users").document(currentUserId).collection("category")
                .document(categoryName)
                .collection("images")
        return collectionReference
    }

    fun deleteImage(image: String, categoryName: String, timeInMilis: String) {
        currentUserId = auth.currentUser!!.uid

        //delete image from storage
        val storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(image)
        storageReference.delete().addOnSuccessListener {
            Log.d(TAG, "deleted")
        }
            .addOnFailureListener {
                Log.d(TAG, "failed")
            }

        //delete image from firestrore
        db.collection("users").document(currentUserId).collection("category").document(categoryName)
            .collection("images")
            .document(timeInMilis).delete().addOnCompleteListener {task->
                if(task.isSuccessful){
                    Log.d(TAG,"Deleted")
                }
                else{
                    Log.d(TAG,"Failed")
                }

            }
    }

    fun fetchTimeLine(): StorageReference {
        currentUserId = auth.currentUser!!.uid
        photosReference = FirebaseStorage.getInstance().getReference("CategoryImages/$currentUserId")
        return photosReference
    }
}