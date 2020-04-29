package com.example.galleryapplication.view

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.galleryapplication.R
import com.example.galleryapplication.viewmodel.FirebaseViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_image_viewer.view.*

/**
 * A simple [Fragment] subclass.
 */
class ImageViewerFragment : Fragment() {
    private lateinit var Date: String
    private lateinit var TimeinMilis: String
    private lateinit var categoryName: String
    private lateinit var Image: String
    private val viewModel: FirebaseViewModel by lazy {
        ViewModelProvider(this).get(FirebaseViewModel::class.java)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        // Inflate the layout for this fragment
        val output: View = inflater.inflate(R.layout.fragment_image_viewer, container, false)

        Date = arguments!!.getString("Date").toString()
        TimeinMilis = arguments!!.getString("Time").toString()
        Image = arguments!!.getString("Image").toString()
        categoryName = arguments!!.getString("CategoryName").toString()

        Picasso.get().load(Image).into(output.photo_view)
        return output
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_on_imageviewer, menu)

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
       if (item.itemId == R.id.delete) {
            deleteImageFromDatabase()
       }
        return super.onOptionsItemSelected(item)
    }


    private fun deleteImageFromDatabase() {
       viewModel.deleteImage(Image,categoryName,TimeinMilis)
        activity!!.supportFragmentManager.popBackStack()
    }
}



