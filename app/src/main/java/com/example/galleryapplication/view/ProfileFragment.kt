package com.example.galleryapplication.view

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.opengl.Visibility
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
import com.example.galleryapplication.R
import com.example.galleryapplication.viewmodel.FirebaseViewModel
import com.squareup.picasso.Picasso
import com.wang.avi.indicator.BallSpinFadeLoaderIndicator
import kotlinx.android.synthetic.main.fragment_profile.*
import kotlinx.android.synthetic.main.fragment_profile.view.*

/**
 * A simple [Fragment] subclass.
 */
class ProfileFragment : Fragment(), View.OnClickListener {
    private lateinit var loadingBar: ProgressDialog
    private val GALLERY = 1
    private lateinit var contentUri: Uri
    private val CAMERA_REQUEST = 188
    private val MY_CAMERA_PERMISSION_REQUEST = 100
    private val viewModel: FirebaseViewModel by lazy {
        ViewModelProvider(this).get(FirebaseViewModel::class.java)
    }


    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val output: View = inflater.inflate(R.layout.fragment_profile, container, false)
        fetchUserDetails()
        output.button_signOut.setOnClickListener(this)
        output.image_frame.setOnClickListener(this)

        return output
    }

    private fun fetchUserDetails() {
        loadingBar = ProgressDialog(context, R.style.MyAlertDialogStyle)
        loadingBar.setTitle("Setting Profile ")
        loadingBar.setMessage("please wait, while we are getting your Details...")
        loadingBar.setCanceledOnTouchOutside(false)
        loadingBar.show()
        viewModel.fetchUserDetails().addOnSuccessListener { document ->
            if (document != null) {
                userName.setText(document.get("Name").toString())
                userName.isEnabled = false
                userEmail.setText(document.get("Email").toString())
                userEmail.isEnabled = false
                Picasso.get().load(document.get("ProfileImage").toString()).into(userProfileImage)
                loadingBar.dismiss()
                ballSpinFadeLoader.visibility = View.GONE
            }else{
                Log.e("NO DOCUMENT", "No such document")
            }
        }
            .addOnFailureListener {
                context!!.showToast("Unable to fetch user details, please try again later.")
            }


    }


    @RequiresApi(Build.VERSION_CODES.M)
    override fun onClick(v: View?) {
        if (v == button_signOut) {
            val builder = AlertDialog.Builder(context)
            builder.setMessage("Do You want to Logout of the app?")
            builder.setCancelable(true)
            builder.setPositiveButton(
                "YES"
            ) { dialog, which ->
                viewModel.logout()
                val intent = Intent(context, MainActivity::class.java)
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                startActivity(intent)
            }
            builder.setNegativeButton(
                "NO"
            ) { dialog, which -> dialog!!.cancel() }
            builder.create().show()


        } else if (v == image_frame) {
            RequestProfileImage()
        }

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
                ), MY_CAMERA_PERMISSION_REQUEST
            )
        } else {
            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(cameraIntent, CAMERA_REQUEST)
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
            val bitmap: Bitmap = data?.extras?.get("data") as Bitmap
            userProfileImage.setImageBitmap(bitmap)
            contentUri = getImageUri(context!!, bitmap)
            viewModel.updateUserProfile(contentUri)
        } else if (requestCode == GALLERY && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                contentUri = data.data!!
                viewModel.updateUserProfile(contentUri)
                val bitmap: Bitmap =
                    MediaStore.Images.Media.getBitmap(context?.contentResolver, contentUri)
                Toast.makeText(context, "Image Saved", Toast.LENGTH_SHORT).show()
                userProfileImage.setImageBitmap(bitmap)
            }
        }
    }

    fun getImageUri(context: Context, inImage: Bitmap): Uri {
        val outImage: Bitmap = Bitmap.createScaledBitmap(inImage, 2000, 2000, true)
        val path: String =
            MediaStore.Images.Media.insertImage(context.contentResolver, outImage, "Title", null)
        return Uri.parse(path)
    }




}
