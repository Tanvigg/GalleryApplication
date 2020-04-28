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
import android.graphics.BitmapFactory
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
import com.example.galleryapplication.R
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_profile.*
import kotlinx.android.synthetic.main.fragment_profile.userProfileImage
import kotlinx.android.synthetic.main.fragment_profile.view.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*

/**
 * A simple [Fragment] subclass.
 */
class ProfileFragment : Fragment() {
    private lateinit var mAuth: FirebaseAuth
    private lateinit var currentUserId: String
    private lateinit var db: FirebaseFirestore
    private lateinit var loadingBar: ProgressDialog
    private val GALLERY = 1
    private val IMAGE_DIRECTORY = "/YourDirectName"

    private lateinit var contentUri: Uri
    private val CAMERA_REQUEST = 188
    private val MY_CAMERA_PERMISSION_REQUEST = 100
    private lateinit var userProfileImageRef: StorageReference
    var downloadUrl: String? = null
    val userHashMap: HashMap<String, String> = HashMap<String, String>()
    private val TAG: String = "ProfileFragment"


    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val output: View = inflater.inflate(R.layout.fragment_profile, container, false)


        mAuth = FirebaseAuth.getInstance()
        currentUserId = mAuth.currentUser!!.uid
        db = FirebaseFirestore.getInstance()
        userProfileImageRef = FirebaseStorage.getInstance().reference.child("Profile Images")
        loadingBar = ProgressDialog(context,
            R.style.MyAlertDialogStyle
        )



        //in order to fetch user details stored in firebase cloud database
        fetchUserData()




        output.image_frame.setOnClickListener {
            RequestProfileImage()
        }

        output.button_signOut.setOnClickListener {
            val builder = AlertDialog.Builder(context)
            builder.setMessage("Do You want to Logout of the app?")
            builder.setCancelable(true)
            builder.setPositiveButton("YES", object : DialogInterface.OnClickListener {

                override fun onClick(dialog: DialogInterface?, which: Int) {
                    mAuth.signOut()

                    val intent = Intent(context, MainActivity::class.java)
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    startActivity(intent)
                }
            })

            builder.setNegativeButton("NO", object : DialogInterface.OnClickListener {

                override fun onClick(dialog: DialogInterface?, which: Int) {
                    dialog!!.cancel()

                }
            })
            builder.create().show()

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

            //calling method to obtain uri from bitmap

            contentUri = getImageUri(context!!, bitmap)
            Log.d("image", contentUri.toString())

        } else if (requestCode == GALLERY && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                contentUri = data.data!!

                val bitmap: Bitmap =
                    MediaStore.Images.Media.getBitmap(context?.contentResolver, contentUri)

                var path: String = saveImage(bitmap)
                Toast.makeText(context, "Image Saved", Toast.LENGTH_SHORT).show()
                userProfileImage.setImageBitmap(bitmap)
            }
        }

        try {
            val filePath: StorageReference =
                userProfileImageRef.child("image" + contentUri.lastPathSegment)

            Log.d("image", contentUri.toString())

            //storing new image url to storage

            filePath.putFile(contentUri).addOnCompleteListener { task ->

                if (task.isSuccessful) {
                    filePath.downloadUrl.addOnCompleteListener(OnCompleteListener { task ->
                        downloadUrl = task.result.toString()


                        //updating in firestore

                        val documentReference: DocumentReference? =
                            db.collection("users").document(currentUserId.toString())

                        if (downloadUrl == null) {
                            val uri: Uri =
                                Uri.parse("android.resource://com.example.galleryapplication/" + R.drawable.profile_image.toString())
                            Log.d(TAG, uri.toString())
                            userHashMap.put("Image", uri.toString())
                        } else {
                            userHashMap.put("Image", downloadUrl!!)
                        }

                        documentReference!!.update(userHashMap as Map<String, Any>)
                            .addOnCompleteListener {
                                if (task.isSuccessful) {
                                    Toast.makeText(
                                        context,
                                        "Profile Picture updated Successfully!",
                                        Toast.LENGTH_SHORT
                                    ).show()


                                } else {
                                    Toast.makeText(context, "Filed to Update!", Toast.LENGTH_SHORT)
                                        .show()

                                }

                            }
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

    private fun fetchUserData() {
        loadingBar.setTitle("Setting Profile ")
        loadingBar.setMessage("please wait, while we are getting your Details...")
        loadingBar.setCanceledOnTouchOutside(false)
        loadingBar.show()
        // ballspinfadeloader()
        val documentReference: DocumentReference? =
            db.collection("users").document(currentUserId.toString())
        documentReference!!.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val doc: DocumentSnapshot = task.result!!
                if (doc.exists()) {
                    Log.d("Document", doc.getData().toString())
                    userName.setText(doc.getData()!!.get("Name").toString())
                    userName.isEnabled = false
                    userEmail.setText(doc.getData()!!.get("Email").toString())
                    userEmail.isEnabled = false
                    // ballspinfadeloadergone()
                    Picasso.get().load(doc.data!!.get("ProfileImage").toString()).into(userProfileImage)

                } else {
                    Log.d("Document", "NO DATA")
                }
                loadingBar.dismiss()


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
                File(
                    wallpaperDirectory,
                    Calendar.getInstance().timeInMillis.toString() + ".jpg"
                )
            f.createNewFile()
            val fo = FileOutputStream(f)
            fo.write(bytes.toByteArray())
            MediaScannerConnection.scanFile(
                context,
                arrayOf(f.path),
                arrayOf("image/jpeg"),
                null
            )
            fo.close()
            Log.d("TAG", "File Saved::---&gt;" + f.absolutePath)
            return f.absolutePath
        } catch (e1: IOException) {
            e1.printStackTrace()

        }
        return ""

    }


    fun getImageUri(context: Context, inImage: Bitmap): Uri {
        Log.d("image", inImage.toString())

        val bytes = ByteArrayOutputStream()

        inImage.compress(Bitmap.CompressFormat.JPEG, 80, bytes)
        val byteArray = bytes.toByteArray()

        Log.d("image", byteArray.size.toString())

        val compressedBitmap: Bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)

        Log.d("image", compressedBitmap.toString())

        val path: String = MediaStore.Images.Media.insertImage(
            context.contentResolver,
            compressedBitmap,
            "Title",
            null
        )
        return Uri.parse(path)

    }




   /* private fun ballspinfadeloadergone() {
        findViewById(R.id.BallSpinFadeLoader).visibility = GONE
    }





    private fun ballspinfadeloader(){
        findViewById(R.id.BallSpinFadeLoader).visibility = VISIBLE
    }*/
}
