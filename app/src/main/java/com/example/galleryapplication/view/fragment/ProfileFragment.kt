package com.example.galleryapplication.view.fragment

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.galleryapplication.R
import com.example.galleryapplication.model.LoadingDialog
import com.example.galleryapplication.model.getImageUri
import com.example.galleryapplication.model.showToast
import com.example.galleryapplication.view.activity.MainActivity
import com.example.galleryapplication.viewmodel.ProfileViewModel
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_profile.*
import kotlinx.android.synthetic.main.fragment_profile.view.*

/**
 * A simple [Fragment] subclass.
 */
class ProfileFragment : Fragment(), View.OnClickListener {
    private val GALLERY = 1
    private lateinit var contentUri: Uri
    private val CAMERA_REQUEST = 188
    private val MY_CAMERA_PERMISSION_REQUEST = 100
    private var loadingDialog:LoadingDialog ?= null
    private lateinit var sharedPreferences: SharedPreferences

    private val viewModel: ProfileViewModel by lazy {
        ViewModelProvider(this).get(ProfileViewModel::class.java)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val output: View = inflater.inflate(R.layout.fragment_profile, container, false)
        loadingDialog = LoadingDialog(activity!!)
        setObservers()
        fetchUserDetails()
        output.button_signOut.setOnClickListener(this)
        output.change_profile_image.setOnClickListener(this)

        sharedPreferences = context!!.getSharedPreferences("mySharedPreference", Context.MODE_PRIVATE)
        val flag = sharedPreferences.getInt("isGoogleSignUp", -1)
        Log.d("flag", flag.toString())
        if (flag == 1)
            output.change_profile_image.visibility = View.GONE
        else
            output.change_profile_image.visibility = View.VISIBLE

        return output
    }

    private fun setObservers(){
        viewModel.getProfileStatus().observe(viewLifecycleOwner, Observer {
            when(it){
                ProfileViewModel.ProfileStatus.SHOW_PROGRESS -> showProgress()
                ProfileViewModel.ProfileStatus.HIDE_PROGRESS -> hideProgress()
                ProfileViewModel.ProfileStatus.SHOW_PROGRESS_ON_UPDATE -> showProgressOnUpdate()
                ProfileViewModel.ProfileStatus.HIDE_PROGRESS_ON_UPDATE -> hideProfressOnUpdate()
            }
        })

        viewModel.getError().observe(viewLifecycleOwner, Observer {
            context!!.showToast(it)
        })
    }

    private fun showProgress(){
        ballSpinFadeLoader.visibility = View.VISIBLE
        loadingDialog!!.startLoadingDialog("Setting Profile","please wait,while we are getting your details...")
    }
    private fun hideProgress(){
        loadingDialog!!.dismissDialog()
        ballSpinFadeLoader.visibility = View.GONE
    }

    private fun showProgressOnUpdate() {
        ballSpinFadeLoader.visibility = View.VISIBLE
    }

    private fun hideProfressOnUpdate() {
        ballSpinFadeLoader.visibility = View.GONE
        context!!.showToast("Profile Picture updates successfully")
    }



    private fun fetchUserDetails() {
        loadingDialog!!.startLoadingDialog("Setting Profile","please wait,while we are getting your details...")
        viewModel.fetchUserDetails().addOnSuccessListener { document ->
            Log.d("doc",document.toString())
            if (document != null) {
                userName.setText(document.get("Name").toString())
                userName.isEnabled = false
                userEmail.setText(document.get("Email").toString())
                userEmail.isEnabled = false
                Picasso.get().load(document.get("ProfileImage").toString()).into(userProfileImage)
                loadingDialog!!.dismissDialog()
            }else{
                Log.e("NO DOCUMENT", "No such document")
            }
        }
            .addOnFailureListener {
                Log.e("Failed", "get failed with ", it)
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


        } else if (v == change_profile_image) {
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
                context!!.showToast("Image Saved")
                userProfileImage.setImageBitmap(bitmap)
            }
        }
    }
}
