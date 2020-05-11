package com.example.galleryapplication.view.fragment

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.*
import android.widget.ImageButton
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import com.example.galleryapplication.R
import com.example.galleryapplication.model.getImageUri
import com.example.galleryapplication.model.showToast
import com.example.galleryapplication.view.Interface.PhotoClickListener
import com.example.galleryapplication.view.adapter.PhotosAdapter
import com.example.galleryapplication.viewmodel.PhotosViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.android.synthetic.main.fragment_photos.*
import kotlinx.android.synthetic.main.fragment_photos.view.*
import java.text.SimpleDateFormat
import java.util.*


/**
 * A simple [Fragment] subclass.
 */
class PhotosFragment : Fragment() {
    private val TAG: String = "PhotoFragment"
    private val CAMERA_REQUEST = 188
    private val GALLERY = 1
    private val MY_CAMERA_PERMISSION_REQUEST = 100
    private lateinit var contentUri: Uri
    private lateinit var categoryName: String
    private lateinit var photoAdapter: PhotosAdapter
    private lateinit var calender: Calendar
    private lateinit var dp: OnDataPass
    private lateinit var imageUri: Uri
    private val viewModel: PhotosViewModel by lazy {
        ViewModelProvider(this).get(PhotosViewModel::class.java)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        Log.d(TAG, "onCreateView called")
        setHasOptionsMenu(true)
        val output: View = inflater.inflate(R.layout.fragment_photos, container, false)

        categoryName = arguments!!.getString("CategoryName").toString()
        setObserver()


        viewModel.fetchPhotos(categoryName).observe(viewLifecycleOwner,Observer{photos ->
            photos.let {
                photoAdapter =
                    PhotosAdapter(
                        context!!,
                        object :
                            PhotoClickListener {
                            override fun onPhotoClick(time: String, date: String, image: String) {
                                Log.d("date", date)
                                dp.sendCurrentTime(time, date, image, categoryName)
                            }

                        })
                photoAdapter.setPhotoData(it)
                output.photos_recyclerView.layoutManager = GridLayoutManager(context,4,GridLayoutManager.VERTICAL,false)
                output.photos_recyclerView.itemAnimator = DefaultItemAnimator()
                output.photos_recyclerView.adapter = photoAdapter
            }
        })
            return output
    }


    private fun setObserver(){
        viewModel.getPhotosStatus().observe(viewLifecycleOwner,Observer{
            when(it){
                PhotosViewModel.PhotoStatus.SHOW_PROGRESS -> showProgress()
                PhotosViewModel.PhotoStatus.HIDE_PROGRESS -> hideProgesss()
            }
        })
        viewModel.getError().observe(viewLifecycleOwner, Observer {
            context!!.showToast(it)
        })
    }

    private fun showProgress(){
        progressbar_photos.visibility = View.VISIBLE

    }
    private fun  hideProgesss(){
        progressbar_photos.visibility = View.GONE

    }


    @RequiresApi(Build.VERSION_CODES.M)
    private fun choosePhotoFromGallery() {
        if (context?.checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                arrayOf(
                    android.Manifest.permission.READ_EXTERNAL_STORAGE
                ), 5
            )
        } else {
            val galleryIntent =
                Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(galleryIntent, GALLERY)
        }
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

            val values = ContentValues()
            values.put(MediaStore.Images.Media.TITLE, "New Picture")
            values.put(MediaStore.Images.Media.DESCRIPTION, "From your Camera")
            imageUri = context!!.contentResolver.insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    values
                )!!
            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
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
        else if (requestCode == 5) {
            val galleryPermission: Boolean = grantResults[0] == PackageManager.PERMISSION_GRANTED
            if (galleryPermission) {
                context!!.showToast("Gallery Permission granted")
                val galleryIntent =
                    Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(galleryIntent, GALLERY)

            }
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
            val bitmap: Bitmap =
                MediaStore.Images.Media.getBitmap(context!!.contentResolver, imageUri)
            contentUri = getImageUri(context!!, bitmap)
            storeImagesInCategory()

        } else if (requestCode == GALLERY && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                contentUri = data.data!!
                Toast.makeText(context, "Image Saved", Toast.LENGTH_SHORT).show()
                storeImagesInCategory()

            }
        }
    }


    fun storeImagesInCategory() {
        calender = Calendar.getInstance()
        val currentTimeInMilis = calender.timeInMillis.toString()
        val formatter = SimpleDateFormat("MMM dd,yyyy")
        val date = formatter.format(Date())
        viewModel.addPhotos(contentUri,currentTimeInMilis,date,categoryName)
    }


    interface OnDataPass {
        fun sendCurrentTime(
            currentTime: String,
            currentDate: String,
            image: String,
            categoryName: String
        )
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        dp = context as OnDataPass
        Log.d(TAG, "onActivityCreated called")

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_on_category, menu)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if (item.itemId == R.id.add) {
            val dialogView: View = layoutInflater.inflate(R.layout.bottom_sheet, null)
            val dialog = BottomSheetDialog(context!!)
            dialog.setContentView(dialogView)

            val btnGallery: ImageButton = dialogView.findViewById(R.id.gallery)
            val btnCamera: ImageButton = dialogView.findViewById(R.id.cam)

            btnGallery.setOnClickListener { v ->
                choosePhotoFromGallery()
                dialog.hide()
            }

            btnCamera.setOnClickListener { v ->
                takePhotoFromCamera()
                dialog.hide()
            }
            dialog.show()

        }
        return super.onOptionsItemSelected(item)
    }


}
