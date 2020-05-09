package com.example.galleryapplication.view.fragment

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.galleryapplication.R
import com.example.galleryapplication.model.getImageUri
import com.example.galleryapplication.model.hide
import com.example.galleryapplication.model.show
import com.example.galleryapplication.model.showToast
import com.example.galleryapplication.view.activity.GalleryActivity
import com.example.galleryapplication.viewmodel.SignUpViewModel
import kotlinx.android.synthetic.main.fragment_sign_up.*
import kotlinx.android.synthetic.main.fragment_sign_up.view.*


class SignUpFragment : Fragment(), View.OnClickListener {

    private val GALLERY = 1
    private val CAMERA_REQUEST = 188
    private var contentUri: Uri ?= null
    private val CAMERA_PERMISSION_REQUEST = 100

    private val viewModel: SignUpViewModel by lazy {
        ViewModelProvider(this).get(SignUpViewModel::class.java)
    }


    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        val output: View = inflater.inflate(R.layout.fragment_sign_up, container, false)
        output.sign_up.setOnClickListener(this)
        output.frame.setOnClickListener(this)
        setObservers()
        return output
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onClick(v: View?) {
        if (v == sign_up)
            saveUserDetails()
        else if (v == frame)
            requestProfileImage()
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

    fun choosePhotoFromGallery() {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(galleryIntent, GALLERY)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun takePhotoFromCamera() {
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
                    context!!.showToast("All permission granted")
                    val cameraIntent = Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE)
                    startActivityForResult(cameraIntent, CAMERA_REQUEST)
                }
            }
        } }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
            val bitmap: Bitmap = data?.extras?.get("data") as Bitmap
            userProfileImage.setImageBitmap(bitmap)
            //calling method to obtain uri from bitmap
            contentUri = getImageUri(context!!, bitmap)

        } else if (requestCode == GALLERY && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                contentUri = data.data!!
                val bitmap: Bitmap =
                    MediaStore.Images.Media.getBitmap(context?.contentResolver, contentUri)
                userProfileImage.setImageBitmap(bitmap)
            }
        }
    }

    private fun saveUserDetails() {
        val name = signup_name.text.toString()
        val email = signup_email.text.toString()
        val password = signup_password.text.toString()
        viewModel.signUp(name, email, password, contentUri).observe(viewLifecycleOwner, Observer {
            it.onSuccess {
                progressbar.hide()
            }
            it.onFailure {
                progressbar.show()
            }

        })
    }



    private fun setObservers() {
        viewModel.getEmailError().observe(viewLifecycleOwner, Observer {
            signup_email.error = it
        })
        viewModel.getPasswordError().observe(viewLifecycleOwner, Observer {
            signup_password.error = it
        })
        viewModel.getNameError().observe(viewLifecycleOwner, Observer {
            signup_name.error = it
        })
        viewModel.getError().observe(viewLifecycleOwner, Observer {
            context!!.showToast(it)
        })
        viewModel.getSignUpStatus().observe(viewLifecycleOwner, Observer {
            when (it) {
                SignUpViewModel.SignupStatus.SHOW_PROGRESS -> progressbar.show()
                SignUpViewModel.SignupStatus.HIDE_PROGRESS -> progressbar.hide()
                SignUpViewModel.SignupStatus.GO_TO_HOMEPAGE -> {
                    startActivity(Intent(context, GalleryActivity::class.java))
                    activity!!.finish()
                }
            }
        })

    }
}




