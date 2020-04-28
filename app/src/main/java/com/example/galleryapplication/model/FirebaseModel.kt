package com.example.galleryapplication.model

import android.net.Uri
import android.util.Log
import android.widget.Toast
import com.example.galleryapplication.R
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference


class FirebaseModel {
    private val auth = FirebaseAuth.getInstance()
    private val TAG = "Firebase Model"
    private var db = FirebaseFirestore.getInstance()
    private lateinit var userProfileImageRef: StorageReference
    private lateinit var profileImageUrl: String
    private lateinit var currentUserId: String


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

    fun saveUserDataToFireStore(name: String, email: String) {
        val userHashMap = HashMap<String, String>()
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

    fun passwordReset(email: String) {
        auth.sendPasswordResetEmail(email).addOnSuccessListener {
            /*Toast.makeText(
                context,
                "We have sent you instructions to reset your password!",
                Toast.LENGTH_SHORT
            ).show()
*/

        }
            .addOnFailureListener {
               // Toast.makeText(context, "Failed to send reset email!", Toast.LENGTH_SHORT).show()


            }
    }

}