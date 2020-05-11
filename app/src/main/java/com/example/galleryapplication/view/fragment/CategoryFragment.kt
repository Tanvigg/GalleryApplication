package com.example.galleryapplication.view.fragment

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
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
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import com.example.galleryapplication.R
import com.example.galleryapplication.view.adapter.CategoryAdapter
import com.example.galleryapplication.view.Interface.CategoryClickListener
import com.example.galleryapplication.model.showToast
import com.example.galleryapplication.viewmodel.CategoryViewModel
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.fragment_category.*
import kotlinx.android.synthetic.main.fragment_category.view.*

/**
 * A simple [Fragment] subclass.
 */
class CategoryFragment : Fragment(), View.OnClickListener {
    private val GALLERY = 1
    private  var contentUri: Uri?=null
    private lateinit var categoryImage: CircleImageView
    private lateinit var categoryName: String
    private lateinit var dp: OnDataPass
    private lateinit var categoryAdapter: CategoryAdapter

    private val viewModel: CategoryViewModel by lazy {
        ViewModelProvider(this).get(CategoryViewModel::class.java)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val output: View = inflater.inflate(R.layout.fragment_category, container, false)
        setObserver()
        output.fab_btn.setOnClickListener(this)
        categoryAdapter =
            CategoryAdapter(context!!,
                object :
                    CategoryClickListener {
                    override fun onCategoryClick(categoryName: String) {
                        if (dp != null) {
                            dp.sendCategoryName(categoryName)
                            Log.d("name", categoryName)
                        }
                    }
                })

        viewModel.fetchCategories().observe(viewLifecycleOwner, Observer { categories ->
            categories.let {
                categoryAdapter.setCategory(it)
                output.category_recyclerView.adapter = categoryAdapter
                output.category_recyclerView.layoutManager =
                    GridLayoutManager(context, 2, GridLayoutManager.VERTICAL, false)
                output.category_recyclerView.itemAnimator = DefaultItemAnimator()
            }

        })
        return output
    }


    private fun setObserver() {
        viewModel.getCategoryStatus().observe(viewLifecycleOwner, Observer {
            when (it) {
                CategoryViewModel.CategoryStatus.SHOW_PROGRESS -> showProgress()
                CategoryViewModel.CategoryStatus.HIDE_PROGRESS -> hideProgesss()
            }
        })
        viewModel.getError().observe(viewLifecycleOwner, Observer {
            context!!.showToast(it)
        })
    }

    private fun showProgress() {
        linearscaleprogressloader.visibility = View.VISIBLE

    }

    private fun hideProgesss() {
        linearscaleprogressloader.visibility = View.GONE

    }


    override fun onClick(v: View?) {
        if (v == fab_btn) {
            requestNewCategory()
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun requestNewCategory() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(context, R.style.AlertDialog)
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.category_custom_layout, null)
        builder.setView(dialogView)
        val categoryNameField: EditText = dialogView.findViewById(R.id.grpName)
        val createBtn: Button = dialogView.findViewById(R.id.buttonOk)
        val cancelBtn: Button = dialogView.findViewById(R.id.buttonCancel)
        categoryImage = dialogView.findViewById(R.id.category_image)
        val dialog: AlertDialog = builder.create()
        categoryImage.setOnClickListener {
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

    }

    private fun createNewCategory() {
        if (contentUri == null) {
            context!!.showToast("Please select an image for the category")
        } else {
            viewModel.addCategory(categoryName, contentUri!!)
        }

    }

    interface OnDataPass {
        fun sendCategoryName(categoryName: String)
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        dp = context as OnDataPass
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 5) {
            val galleryPermission: Boolean = grantResults[0] == PackageManager.PERMISSION_GRANTED
            if (galleryPermission) {
                context!!.showToast("Gallery Permission granted")
                val galleryIntent =
                    Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(galleryIntent, GALLERY)

            }
        }
    }



}


