package com.example.galleryapplication

import android.Manifest
import android.app.Activity.RESULT_OK
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.fragment_sign_up.*
import kotlinx.android.synthetic.main.fragment_sign_up.view.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*


class SignUpFragment : Fragment() {
    private var mAuth: FirebaseAuth? = null
    private var userProfileImageRef: StorageReference? = null
    private var currentUserId: String? = null
    private var db: FirebaseFirestore? = null

    private val TAG: String = "SignUpActivity"

    private var email: String? = null
    private var password: String? = null
    private var name: String? = null

    private val CAMERA_REQUEST = 188
    private val GALLERY = 1
    private val MY_CAMERA_PERMISSION_REQUEST = 100
    private val IMAGE_DIRECTORY = "/YourDirectName"
    val userHashMap: HashMap<String, String> = HashMap<String, String>()
    var downloadUrl: String? = null


    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate the layout for this fragment

        setHasOptionsMenu(true)
        val output: View = inflater.inflate(R.layout.fragment_sign_up, container, false)


        mAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        userProfileImageRef = FirebaseStorage.getInstance().reference.child("Profile Images")

        output.sign_up.setOnClickListener {
            saveUserDetails()
        }

        output.frame.setOnClickListener {
            RequestProfileImage()
        }

        return output
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun RequestProfileImage() {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Select Action")
        val pictureDialogItems =
            arrayOf<String>("Select photo from Gallery", "Capture photo from Camera")
        builder.setItems(pictureDialogItems, DialogInterface.OnClickListener { dialog, which ->
            when (which) {
                0 -> choosePhotoFromGallery()
                1 -> takePhotoFromCamera()
            }
        })
        builder.show()
    }


    private fun choosePhotoFromGallery() {
        val galleryIntent = Intent(
            Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        )
        startActivityForResult(galleryIntent, GALLERY)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun takePhotoFromCamera() {
        if (context?.checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                arrayOf(Manifest.permission.CAMERA),
                MY_CAMERA_PERMISSION_REQUEST
            )

        } else {
            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(cameraIntent, CAMERA_REQUEST)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == MY_CAMERA_PERMISSION_REQUEST) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(context, "camera permission granted", Toast.LENGTH_SHORT).show()
                val cameraIntent = Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE)
                startActivityForResult(cameraIntent, CAMERA_REQUEST)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK) {
            val photo: Bitmap = data?.extras?.get("data") as Bitmap
            userProfileImage.setImageBitmap(photo)

            downloadUrl = photo.toString()

        } else if (requestCode == GALLERY && resultCode == RESULT_OK) {
            if (data != null) {

                val contentUri: Uri? = data.data

                try {
                    val bitmap: Bitmap =
                        MediaStore.Images.Media.getBitmap(context?.contentResolver, contentUri)

                    var path: String = saveImage(bitmap)
                    Toast.makeText(context, "Image Saved", Toast.LENGTH_SHORT).show()


                    userProfileImage.setImageBitmap(bitmap)
                    val filePath: StorageReference =
                        userProfileImageRef!!.child(currentUserId.toString() + ".jpg")

                    filePath.putFile(contentUri!!).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(
                                context,
                                "Profile picture updated successfully...", Toast.LENGTH_SHORT
                            ).show()
                            filePath.downloadUrl.addOnCompleteListener(OnCompleteListener { task ->
                                downloadUrl = task.result.toString()


                            })
                        } else {
                            var message: String = task.exception.toString()
                            Toast.makeText(context, "Error: $message", Toast.LENGTH_SHORT)
                                .show()
                        }


                    }


                } catch (e: IOException) {
                    e.printStackTrace()
                    Toast.makeText(context, "Failed!", Toast.LENGTH_SHORT).show()

                }
            }
        }
    }


    private fun saveImage(bitmap: Bitmap): String {
        val bytes = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, bytes)
        val wallpaperDirectory =
            File(Environment.getExternalStorageDirectory().absolutePath.toString() + IMAGE_DIRECTORY)
        if (!wallpaperDirectory.exists()) {
            wallpaperDirectory.mkdir()

        }
        try {
            val f =
                File(wallpaperDirectory, Calendar.getInstance().timeInMillis.toString() + ".jpg")
            f.createNewFile()
            val fo = FileOutputStream(f)
            fo.write(bytes.toByteArray())
            MediaScannerConnection.scanFile(context, arrayOf(f.path), arrayOf("image/jpeg"), null)
            fo.close()
            Log.d("TAG", "File Saved::---&gt;" + f.absolutePath)
            return f.absolutePath
        } catch (e1: IOException) {
            e1.printStackTrace()

        }
        return ""

    }


    private fun validateEmail(): Boolean {
        var value = signup_email?.text.toString()
        val pattern: String =
            "^[a-zA-Z0-9_!#\$%&’*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+\$"
        if (value.isEmpty()) {
            signup_email.setError("Field can't be Empty")
            return false
        } else if (!value.matches(pattern.toRegex())) {
            signup_email.setError("Pattern doesn't match")
            return false
        } else {
            signup_email.setError(null)
            return true
        }
    }

    private fun validatePassword(): Boolean {
        val value = signup_password?.text.toString()
        val pattern: String = "^(?=.*\\d).{6,16}\$"
        //(?=.*d)         : This matches the presence of at least one digit i.e. 0-9.
        //{6,16}          : This limits the length of password from minimum 6 letters to maximum 16 letters.
        if (value.isEmpty()) {
            signup_password.setError("Field can't be Empty")
            return false
        } else if (!value.matches(pattern.toRegex())) {
            signup_password.setError("Pattern doesn't match")
            return false
        } else {
            signup_password.setError(null)
            return true

        }
    }

    private fun validateName(): Boolean {
        val value = signup_name?.text.toString()
        if (value.isEmpty()) {
            signup_name.setError("Field can't be Empty")
            return false
        } else {
            signup_name.setError(null)
            return true
        }
    }

    private fun saveUserDetails() {
        if (!validateName() || !validateEmail() || !validatePassword()) {
            return
        }
        name = signup_name?.text.toString()
        email = signup_email?.text.toString()
        password = signup_password?.text.toString()

        mAuth!!.createUserWithEmailAndPassword(email!!, password!!)
            .addOnCompleteListener { task ->

                if (task.isSuccessful) {
                    currentUserId = mAuth!!.currentUser!!.uid

                    val documentReference: DocumentReference? =
                        db?.collection("users")?.document(currentUserId.toString())

                    userHashMap.put("Name", name!!)
                    userHashMap.put("Email", email!!)
                    userHashMap.put("Image", downloadUrl!!)
                    documentReference?.set(userHashMap)?.addOnCompleteListener { task ->
                        if (task.isSuccessful) Toast.makeText(
                            context,
                            "data Inserted Successfully!",
                            Toast.LENGTH_SHORT
                        ).show()
                        else {
                            Toast.makeText(context, "data Insertion Failed!", Toast.LENGTH_SHORT)
                                .show()

                        }
                    }

                    Log.d(TAG, "createuserwithemailpassword:Successful")
                    var user: FirebaseUser? = mAuth!!.currentUser
                    startActivity(Intent(context, GalleryActivity::class.java))

                } else {
                    Log.d(TAG, "createuserwithemailpassword:Failed", task.exception)
                    Toast.makeText(context, "Authentication Failed", Toast.LENGTH_SHORT).show()
                }
            }
    }


}




