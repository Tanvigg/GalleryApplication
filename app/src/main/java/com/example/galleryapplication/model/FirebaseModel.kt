package com.example.galleryapplication.model

import android.net.Uri
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.example.galleryapplication.R
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


class FirebaseModel {
    private val auth = FirebaseAuth.getInstance()
    private val TAG = "Firebase Model"
    private var db = FirebaseFirestore.getInstance()
    private lateinit var userProfileImageRef: StorageReference
    private lateinit var userCategoryImagReference: StorageReference
    private lateinit var userPhotosReference: StorageReference
    private lateinit var photosReference: StorageReference


    private var profileImageUrl: String? = null
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

    fun uploadUserToFirebase(photoUrl: Uri?, displayName: String?, email: String?) {
        currentUserId = auth.uid.toString()
        Log.d("id", currentUserId)
        userProfileImageRef = FirebaseStorage.getInstance().reference.child("Profile Images")
        profileImageUrl = photoUrl.toString()
        Log.d("d1", profileImageUrl)
        saveUserDataToFireStore(displayName!!, email!!, profileImageUrl!!)
    }


    fun signUp(name: String, email: String, password: String, userImage: Uri?):Boolean {
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                currentUserId = auth.currentUser!!.uid
                uploadUser(userImage, name, email)
            }
            else{
                Log.e("Error", "" + task.exception)
            }
        }
        return true
    }



    fun uploadUser(userImage: Uri?, name: String, email: String) {
        var userImage1 = userImage
        if (userImage == null) {
            userImage1 =
                Uri.parse("android.resource://com.example.galleryapplication/" + R.drawable.profile_image)
        }
        Log.d("id", currentUserId)
        userProfileImageRef = FirebaseStorage.getInstance().reference.child("Profile Images")

        val filePath: StorageReference =
            userProfileImageRef.child("image" + userImage1!!.lastPathSegment)
        filePath.putFile(userImage1).addOnSuccessListener {
            filePath.downloadUrl.addOnSuccessListener {
                profileImageUrl = it.toString()
                saveUserDataToFireStore(name, email, profileImageUrl!!)
            }
                .addOnFailureListener {
                    Log.e("Unable to upload", "$it")
                }
        }
    }


    private fun saveUserDataToFireStore(name: String, email: String, profileImageUrl: String) {
        userHashMap.put("Name", name)
        userHashMap.put("Email", email)
        userHashMap.put("ProfileImage", profileImageUrl)
        Log.d("d1", userHashMap.toString())
        db.collection("users").document(currentUserId).set(userHashMap).addOnSuccessListener {
            Log.e("FIREBASE MODEL", "successful")
        }
            .addOnFailureListener {
                Log.e("Error", "$it")
            }

    }

    fun passwordReset(email: String): Task<Void> {
        val fAuth: Task<Void> = auth.sendPasswordResetEmail(email)
        return fAuth
    }

    fun fetchUserDetails(): Task<DocumentSnapshot> {
        currentUserId = auth.uid.toString()
        return db.collection("users").document(currentUserId).get()

    }

    fun updateUserProfile(selectedPhotoUri: Uri): MutableLiveData<Result<Boolean>> {
        val result = MutableLiveData<Result<Boolean>>()
        currentUserId = auth.uid.toString()
        userProfileImageRef = FirebaseStorage.getInstance().reference.child("Profile Images")
        val filePath: StorageReference =
            userProfileImageRef.child("image" + selectedPhotoUri.lastPathSegment)
        filePath.putFile(selectedPhotoUri).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                filePath.downloadUrl.addOnCompleteListener { task ->
                    profileImageUrl = task.result.toString()
                    db.collection("users").document(currentUserId)
                        .update("ProfileImage", profileImageUrl).addOnSuccessListener {
                            result.value = Result.success(true)

                        }
                        .addOnFailureListener {
                            result.value = Result.failure(it)
                        }
                }
            } else {
                Log.d(TAG, "profile image upload Failed")

            }
        }
        return result
    }


    fun logout() {
        auth.signOut()
    }

    fun addCategory(categoryName: String, selectedPhotoUri: Uri): MutableLiveData<Result<Boolean>> {
        val result = MutableLiveData<Result<Boolean>>()
        userCategoryImagReference = FirebaseStorage.getInstance().reference.child("Category Images")
        val filePath: StorageReference =
            userCategoryImagReference.child("image" + selectedPhotoUri.lastPathSegment)
        filePath.putFile(selectedPhotoUri).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                filePath.downloadUrl.addOnCompleteListener { task ->
                    categoryImageUrl = task.result.toString()
                    Log.d("image", categoryImageUrl)
                    saveCategoryToFireStore(categoryName).addSnapshotListener { snapshot, e ->
                        if (e != null) {
                            Log.w(TAG, "Listen failed.", e)
                            return@addSnapshotListener
                        }
                        if (snapshot != null) {
                            result.value = Result.success(true)

                        } else {
                            result.value = e?.let { Result.failure(it) }

                        }
                    }
                }
            } else {
                Log.d(TAG, "image upload Failed")
            }
        }
        return result
    }


    private fun saveCategoryToFireStore(categoryName: String): DocumentReference {
        currentUserId = auth.uid.toString()
        category = Category(
            categoryName,
            categoryImageUrl
        )
        val documentReference: DocumentReference = db.collection("users").document(currentUserId)
        documentReference.collection("category").document(categoryName)
            .set(category)
        return documentReference
    }

    fun fetchCategories(): CollectionReference {
        currentUserId = auth.uid.toString()
        val collectionReference =
            db.collection("users").document(currentUserId).collection("category")
        return collectionReference
    }

    fun addPhotos(
        selectedPhotoUri: Uri,
        timeInMilis: String,
        date: String,
        categoryName: String
    ): MutableLiveData<Result<Boolean>> {
        val result = MutableLiveData<Result<Boolean>>()
        currentUserId = auth.uid.toString()
        userPhotosReference =
            FirebaseStorage.getInstance().getReference("CategoryImages/$currentUserId")
        val filePath: StorageReference =
            userPhotosReference.child("image" + selectedPhotoUri.lastPathSegment)
        filePath.putFile(selectedPhotoUri).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                filePath.downloadUrl.addOnCompleteListener { task ->
                    imageUrl = task.result.toString()
                    Log.d("image", imageUrl)
                    saveImagesToFireStore(
                        timeInMilis,
                        date,
                        categoryName
                    ).addSnapshotListener { snapshot, e ->
                        if (e != null) {
                            Log.w(TAG, "Listen failed.", e)
                            return@addSnapshotListener
                        }

                        if (snapshot != null) {
                            result.value = Result.success(true)

                        } else {
                            result.value = e?.let { Result.failure(it) }

                        }
                    }
                }
            } else {
                Log.d(TAG, "image upload Failed")
            }
        }
        return result
    }

    private fun saveImagesToFireStore(
        timeInMilis: String,
        date: String,
        categoryName: String
    ): DocumentReference {
        currentUserId = auth.uid.toString()
        photos = Photos(
            imageUrl,
            timeInMilis,
            date
        )
        val documentReference: DocumentReference = db.collection("users").document(currentUserId)
        documentReference.collection("category").document(categoryName)
            .collection("images")
            .document(timeInMilis).set(photos)
        return documentReference
    }

    fun fetchPhotos(categoryName: String): CollectionReference {
        currentUserId = auth.uid.toString()
        var collectionReference =
            db.collection("users").document(currentUserId).collection("category")
                .document(categoryName)
                .collection("images")
        return collectionReference
    }

    fun deleteImage(image: String, categoryName: String, timeInMilis: String) {
        currentUserId = auth.uid.toString()

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
            .document(timeInMilis).delete().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "Deleted")
                } else {
                    Log.d(TAG, "Failed")
                }

            }
    }

    fun fetchTimeLine(): StorageReference {
        currentUserId = auth.uid.toString()
        photosReference =
            FirebaseStorage.getInstance().getReference("CategoryImages/$currentUserId")
        return photosReference
    }
}