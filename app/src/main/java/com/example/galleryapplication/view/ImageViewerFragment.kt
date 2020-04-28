package com.example.galleryapplication.view

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.galleryapplication.R
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
    private lateinit var mAuth: FirebaseAuth
    private lateinit var currentUserId: String
    private lateinit var db: FirebaseFirestore
    private val TAG : String = "PhotoFragment"
    private val checked : Boolean = false





    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        // Inflate the layout for this fragment
        val output: View = inflater.inflate(R.layout.fragment_image_viewer, container, false)
        output.progressBar_bigImage.visibility = View.VISIBLE

        mAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        currentUserId = mAuth.currentUser!!.uid

        Date = arguments!!.getString("Date").toString()
        TimeinMilis = arguments!!.getString("Time").toString()
        Image = arguments!!.getString("Image").toString()
        categoryName = arguments!!.getString("CategoryName").toString()

        Picasso.get().load(Image).into(output.photo_view)
        output.progressBar_bigImage.visibility = View.GONE


        return output
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_on_imageviewer, menu)

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == R.id.fav) {
            if(!checked){
                item.setIcon(R.drawable.ic_favorite_red_500_18dp)
                addToDatabase()
            }
        } else if (item.itemId == R.id.delete) {
            deleteImageFromDatabase()

        }

        return super.onOptionsItemSelected(item)
    }

    private fun addToDatabase() {
        val favouritesModel =
            FavouritesModel(
                Image,
                TimeinMilis
            )
        db.collection("users").document(currentUserId).collection("Favourites").document(TimeinMilis).set(favouritesModel)
            .addOnCompleteListener { task ->
                if(task.isSuccessful){
                    Toast.makeText(
                        context,
                        "Photo saved in Favourites Successfully",
                        Toast.LENGTH_SHORT
                    ).show()

                }else{
                    Toast.makeText(context,"Failed",Toast.LENGTH_SHORT).show()

                }
            }

    }

    private fun deleteImageFromDatabase() {
        val documentReference: DocumentReference? = db.collection("users").document(currentUserId).collection("categories").document(categoryName).collection("images").document(TimeinMilis)
        documentReference!!.delete()
        fragmentManager!!.popBackStack()

    }





}



