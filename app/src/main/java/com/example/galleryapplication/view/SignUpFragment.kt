package com.example.galleryapplication.view

import android.Manifest
import android.app.Activity.RESULT_OK
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.galleryapplication.GalleryActivity
import com.example.galleryapplication.R
import com.example.galleryapplication.viewmodel.FirebaseViewModel
import kotlinx.android.synthetic.main.fragment_sign_up.*
import kotlinx.android.synthetic.main.fragment_sign_up.view.*
import java.io.ByteArrayOutputStream


class SignUpFragment : Fragment() {

    private val CAMERA_REQUEST = 188
    private val GALLERY = 1
    private val CAMERA_PERMISSION_REQUEST = 100
    private lateinit var contentUri: Uri
    private lateinit var viewModel: FirebaseViewModel


    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate the layout for this fragment
        setHasOptionsMenu(true)
        val output: View = inflater.inflate(R.layout.fragment_sign_up, container, false)
        output.sign_up.setOnClickListener {
            saveUserDetails()
        }

        output.frame.setOnClickListener {
            requestProfileImage()
        }

        return output
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun requestProfileImage() {
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
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(galleryIntent, GALLERY)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun takePhotoFromCamera() {
        if (context?.checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                arrayOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ), CAMERA_PERMISSION_REQUEST
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

        if (requestCode == CAMERA_PERMISSION_REQUEST) {
            if (grantResults.size > 0) {
                val cameraPermission: Boolean = grantResults[0] == PackageManager.PERMISSION_GRANTED
                val readExternalStorage: Boolean =
                    grantResults[1] == PackageManager.PERMISSION_GRANTED
                val writeExternalStorage: Boolean =
                    grantResults[2] == PackageManager.PERMISSION_GRANTED

                if (cameraPermission && readExternalStorage && writeExternalStorage) {
                    Toast.makeText(context, "All permission granted", Toast.LENGTH_SHORT).show()
                    val cameraIntent = Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE)
                    startActivityForResult(cameraIntent, CAMERA_REQUEST)
                }
            }
        }


    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK) {
            val bitmap: Bitmap = data?.extras?.get("data") as Bitmap
            userProfileImage.setImageBitmap(bitmap)

            //calling method to obtain uri from bitmap
            contentUri = getImageUri(context!!, bitmap)

        } else if (requestCode == GALLERY && resultCode == RESULT_OK) {
            if (data != null) {
                contentUri = data.data!!

                val bitmap: Bitmap = MediaStore.Images.Media.getBitmap(context?.contentResolver, contentUri)
                userProfileImage.setImageBitmap(bitmap)
            }
        }
    }



    private fun validateEmail(): Boolean {
        val value = signup_email.text.toString()
        val pattern: String =
            "^[a-zA-Z0-9_!#\$%&’*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+\$"
        if (value.isEmpty()) {
            signup_email.setError("Please Enter Email ")
            return false
        } else if (!value.matches(pattern.toRegex())) {
            signup_email.error = "Invalid email Address"
            return false
        } else {
            signup_email.error = null
            return true
        }
    }

    private fun validatePassword(): Boolean {
        val value = signup_password.text.toString()
        val pattern: String = "^(?=.*\\d).{6,16}\$"
        //(?=.*d)         : This matches the presence of at least one digit i.e. 0-9.
        //{6,16}          : This limits the length of password from minimum 6 letters to maximum 16 letters.
        if (value.isEmpty()) {
            signup_password.error = "Please Enter Password"
            return false
        } else if (!value.matches(pattern.toRegex())) {
            signup_password.error = "Invalid Password"
            return false
        } else {
            signup_password.error = null
            return true

        }
    }

    private fun validateName(): Boolean {
        val value = signup_name.text.toString()
        if (value.isEmpty()) {
            signup_name.error = "Please Enter Name"
            return false
        } else {
            signup_name.error = null
            return true
        }
    }

    private fun saveUserDetails() {
        if (!validateName() || !validateEmail() || !validatePassword()) {
            return
        }
        val name = signup_name.text.toString()
        val email = signup_email.text.toString()
        val password = signup_password.text.toString()

        progressbar.visibility = View.VISIBLE
        viewModel = ViewModelProvider(this).get(FirebaseViewModel::class.java)
        if(viewModel.signUp(name,email,password,contentUri)){
            startActivity(Intent(context, GalleryActivity::class.java))
        }else{
            Toast.makeText(context,"No Internet Connection", Toast.LENGTH_SHORT).show()
        }
        progressbar.visibility = View.GONE
    }


    fun getImageUri(context: Context, inImage: Bitmap): Uri {
        val outImage: Bitmap = Bitmap.createScaledBitmap(inImage, 2000, 2000, true)
        val path: String = MediaStore.Images.Media.insertImage(context.contentResolver, outImage, "Title", null)
        return Uri.parse(path)
    }


}





