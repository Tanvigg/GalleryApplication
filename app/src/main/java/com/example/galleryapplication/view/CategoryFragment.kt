package com.example.galleryapplication.view

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import com.example.galleryapplication.R
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.fragment_category.*
import kotlinx.android.synthetic.main.fragment_category.view.*
import java.io.IOException

/**
 * A simple [Fragment] subclass.
 */
class CategoryFragment : Fragment() {
    private val GALLERY = 1
    var downloadUrl: String? = null
    private lateinit var contentUri: Uri
    private lateinit var category: Category
    private lateinit var categoryImage: CircleImageView
    private lateinit var categoryName: String
    private lateinit var mAuth: FirebaseAuth
    private lateinit var currentUserId: String
    private lateinit var db: FirebaseFirestore
    private lateinit var userCategoryImagReference: StorageReference
    private lateinit var categoryAdapter: CategoryAdapter
    private var categoryList = ArrayList<Category>()
    private lateinit var dp: OnDataPass


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val output: View = inflater.inflate(R.layout.fragment_category, container, false)



        mAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        currentUserId = mAuth.uid.toString()
        userCategoryImagReference = FirebaseStorage.getInstance().reference.child("Category Images")


        output.fab_btn.setOnClickListener {
            RequestNewCategory()
        }


        val layoutManager = GridLayoutManager(context, 2, GridLayoutManager.VERTICAL, false)
        output.category_recyclerView.setHasFixedSize(true)
        output.category_recyclerView.layoutManager = layoutManager
        output.category_recyclerView.itemAnimator = DefaultItemAnimator()

        output.progressbar.visibility = View.VISIBLE

        loadDataFromFirebase()

        return output
    }


    private fun loadDataFromFirebase() {
        //real time listening firestore
        if (categoryList.size > 0)
            categoryList.clear()

        db.collection("users").document(currentUserId).collection("categories")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w("TAG", "listen:error", e)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    progressbar.visibility = View.GONE
                    val documentChangeList: List<DocumentChange> = snapshot.documentChanges

                    for (documentChange: DocumentChange in documentChangeList) {
                        documentChange.document.data
                        val fetchedCategory =
                            Category(
                                (documentChange.document.get("categoryName").toString()),
                                documentChange.document.get("categoryImage").toString()
                            )
                        categoryList.add(fetchedCategory)

                    }
                    categoryAdapter =
                        CategoryAdapter(
                            context!!,
                            categoryList,
                            object :
                                CategoryClickListener {
                                override fun onCategoryClick(categoryName: String) {
                                    if (dp != null) {
                                        dp.sendCategoryName(categoryName)
                                        Log.d("name", categoryName)
                                    }
                                }
                            })
                    category_recyclerView.adapter = categoryAdapter

                } else {
                    Log.d("TAG", "Query snapshot is null")
                }


            }

    }

    private fun RequestNewCategory() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(context,
            R.style.AlertDialog
        )
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.category_custom_layout, null)
        builder.setView(dialogView)
        val categoryNameField: EditText = dialogView.findViewById(R.id.grpName)
        val createBtn: Button = dialogView.findViewById(R.id.buttonOk)
        val cancelBtn: Button = dialogView.findViewById(R.id.buttonCancel)
        categoryImage = dialogView.findViewById(R.id.category_image)


        val dialog: AlertDialog = builder.create()

        categoryImage.setOnClickListener {
            val galleryIntent =
                Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(galleryIntent, GALLERY)
        }

        createBtn.setOnClickListener {
            categoryName = categoryNameField.text.toString()
            if (TextUtils.isEmpty(categoryName)) {
                Toast.makeText(context, "Please type a Categrory Name", Toast.LENGTH_SHORT)
                    .show()
            } else {
                dialog.dismiss()
                createNewCategory()
            }
        }

        cancelBtn.setOnClickListener {
            dialog.dismiss()
            //do nothing
        }
        builder.setCancelable(true)
        dialog.show()

    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == GALLERY && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                contentUri = data.data!!

                val bitmap: Bitmap =
                    MediaStore.Images.Media.getBitmap(context?.contentResolver, contentUri)

                Toast.makeText(context, "Image Saved", Toast.LENGTH_SHORT).show()
                categoryImage.setImageBitmap(bitmap)
            }
        }

        try {
            val filePath: StorageReference =
                userCategoryImagReference.child("image" + contentUri.lastPathSegment)

            Log.d("image", contentUri.toString())
            filePath.putFile(contentUri).addOnCompleteListener { task ->

                if (task.isSuccessful) {
                    filePath.downloadUrl.addOnCompleteListener(OnCompleteListener { task ->
                        downloadUrl = task.result.toString()


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

    fun createNewCategory() {
        if (downloadUrl != null) {
            category = Category(
                categoryName,
                downloadUrl!!
            )
        } else {
            val uri: Uri =
                Uri.parse("android.resource://com.example.galleryapplication/" + R.drawable.profile_image.toString())
            category = Category(
                categoryName,
                uri.toString()
            )

        }
        Log.d("categoryData", category.toString())

        db.collection("users").document(currentUserId).collection("categories")
            .document(categoryName).set(category).addOnCompleteListener { task ->
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

    interface OnDataPass {
        fun sendCategoryName(categoryName: String)
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        dp = context as OnDataPass
    }


}


