package com.example.galleryapplication

import android.Manifest
import android.app.Activity
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Environment.getExternalStorageDirectory
import android.provider.MediaStore
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import com.google.firebase.firestore.local.QueryResult
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.fragment_category.*
import kotlinx.android.synthetic.main.fragment_category.view.*
import kotlinx.android.synthetic.main.fragment_login.*
import kotlinx.android.synthetic.main.fragment_photos.*
import kotlinx.android.synthetic.main.fragment_photos.view.*
import kotlinx.android.synthetic.main.fragment_sign_up.*
import kotlinx.android.synthetic.main.fragment_sign_up.progressbar
import java.io.*
import java.net.URI
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

/**
 * A simple [Fragment] subclass.
 */
class PhotosFragment : Fragment() {
    private lateinit var mAuth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var currentUserId: String
    private lateinit var userPhotosReference: StorageReference
    private val TAG: String = "PhotoFragment"


    private val CAMERA_REQUEST = 188
    private val GALLERY = 1
    private val MY_CAMERA_PERMISSION_REQUEST = 100
    var downloadUrl: String? = null
    private lateinit var contentUri: Uri
    private lateinit var image: Image
    private lateinit var categoryName: String
    private lateinit var photosList: ArrayList<Image>
    private lateinit var imageAdapter: ImagesAdapter

    private lateinit var calender: Calendar
    private lateinit var dp: OnDataPass
    lateinit var photoListener: ListenerRegistration


    private lateinit var imageUri: Uri


    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        Log.d(TAG, "onCreateView called")
        setHasOptionsMenu(true)
        val output: View = inflater.inflate(R.layout.fragment_photos, container, false)


        mAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        currentUserId = mAuth.currentUser!!.uid
        userPhotosReference = FirebaseStorage.getInstance().reference.child("Images in Category")

        photosList = arrayListOf()



        categoryName = arguments!!.getString("CategoryName").toString()
        Log.d("name", categoryName)


        val layoutManager = GridLayoutManager(context, 4, GridLayoutManager.VERTICAL, false)
        output.photos_recyclerView.layoutManager = layoutManager
        output.photos_recyclerView.setHasFixedSize(true)
        output.photos_recyclerView.itemAnimator = DefaultItemAnimator()

        output.progressbar_photos.visibility = View.VISIBLE



        if(photosList.size>0){
            photosList.clear()
        }

        photoListener = db.collection("users").document(currentUserId).collection("categories")
            .document(categoryName).collection("images").orderBy("time", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w("TAG", "listen:error", e)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    output.progressbar_photos.visibility = View.GONE

                    val documentChangeList: List<DocumentChange> = snapshot.documentChanges
                    for (documentChange: DocumentChange in documentChangeList) {
                        documentChange.document.data
                        val fetchedImages = Image(
                            documentChange.document.get("image").toString(),
                            documentChange.document.get("time").toString(),
                            documentChange.document.get("date").toString()
                        )
                        when (documentChange.type) {
                            DocumentChange.Type.ADDED -> imageAdapter.setPhotoData(fetchedImages)
                            DocumentChange.Type.REMOVED -> imageAdapter.removePhotoData(fetchedImages)
                        }
                    }
                }else{
                    Toast.makeText(context,"No data in snapshot",Toast.LENGTH_SHORT).show()
                }
            }
        imageAdapter = ImagesAdapter(context!!, photosList, object : PhotoClickListener {
            override fun onPhotoClick(time: String, date: String, image: String) {
                Log.d("date", date)
                dp.sendCurrentTime(time, date, image, categoryName)
            }

        })
        output.photos_recyclerView.adapter = imageAdapter

        if (photosList.isNotEmpty()) {
            photoListener.remove()
        }



        return output
    }


    private fun choosePhotoFromGallery() {
        val galleryIntent =
            Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
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

            val values = ContentValues()
            values.put(MediaStore.Images.Media.TITLE, "New Picture")
            values.put(MediaStore.Images.Media.DESCRIPTION, "From your Camera")
            imageUri =
                context!!.contentResolver.insert(
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
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {

            val bitmap: Bitmap =
                MediaStore.Images.Media.getBitmap(context!!.contentResolver, imageUri)

            //calling method to obtain uri from bitmap

            contentUri = getImageUri(context!!, bitmap)
            Log.d("image", contentUri.toString())


        } else if (requestCode == GALLERY && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                contentUri = data.data!!

                val bitmap: Bitmap =
                    MediaStore.Images.Media.getBitmap(context?.contentResolver, contentUri)

                Toast.makeText(context, "Image Saved", Toast.LENGTH_SHORT).show()
            }
        }

        try {
            val filePath: StorageReference =
                userPhotosReference.child("image" + contentUri.lastPathSegment)

            Log.d("image", contentUri.toString())
            filePath.putFile(contentUri).addOnCompleteListener { task ->

                if (task.isSuccessful) {
                    filePath.downloadUrl.addOnCompleteListener(OnCompleteListener { task ->
                        downloadUrl = task.result.toString()
                        storeImagesInCategory()
                    })
                } else {
                    val message: String = task.exception.toString()
                    Toast.makeText(context, "Error: $message", Toast.LENGTH_SHORT)
                        .show()
                }

            }


        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(context, "Failed!", Toast.LENGTH_SHORT).show()
        }
    }


    fun storeImagesInCategory() {
        calender = Calendar.getInstance()
        val currentTimeInMilis = calender.timeInMillis.toString()
        val formatter = SimpleDateFormat("MMM dd,yyyy")
        val date = formatter.format(Date())
        val  isFavourite  = false


        Log.d("date", date)
        image = Image(downloadUrl!!, currentTimeInMilis, date)

        db.collection("users").document(currentUserId).collection("categories")
            .document(categoryName).collection("images").document(currentTimeInMilis).set(image)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(
                        context,
                        "Category saved in database Successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(context, "Failed", Toast.LENGTH_SHORT).show()
                }
            }


    }


    fun getImageUri(context: Context, inImage: Bitmap): Uri {
        val outImage: Bitmap = Bitmap.createScaledBitmap(inImage, 2000, 2000, true)
        val path: String =
            MediaStore.Images.Media.insertImage(context.contentResolver, outImage, "Title", null)
        return Uri.parse(path)
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
            }

            btnCamera.setOnClickListener { v ->
                takePhotoFromCamera()
            }
            dialog.show()

        }
        return super.onOptionsItemSelected(item)
    }



}
