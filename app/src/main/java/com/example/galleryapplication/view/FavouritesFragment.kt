package com.example.galleryapplication.view

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import com.example.galleryapplication.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_favourites.*
import kotlinx.android.synthetic.main.fragment_favourites.view.*

/**
 * A simple [Fragment] subclass.
 */
class FavouritesFragment : Fragment() {
    private lateinit var mAuth: FirebaseAuth
    private lateinit var currentUserId: String
    private lateinit var db: FirebaseFirestore
    private lateinit var favList: ArrayList<FavouritesModel>
    private lateinit var favAdapter: FavoutitesAdapter
    private lateinit var favs_progressBar : ProgressBar


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val output: View = inflater.inflate(R.layout.fragment_favourites, container, false)

        mAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        currentUserId = mAuth.currentUser!!.uid
        favs_progressBar = output.findViewById(R.id.progressbar_favs)


        favList = arrayListOf()


        val layoutManager = GridLayoutManager(context, 4, GridLayoutManager.VERTICAL, false)
        output.favourites_recycleView.layoutManager = layoutManager
        output.favourites_recycleView.setHasFixedSize(true)
        output.favourites_recycleView.itemAnimator = DefaultItemAnimator()


        favs_progressBar.visibility = View.VISIBLE

        loadDataFromFirebase()

        return output
    }

    private fun loadDataFromFirebase() {
        //real time listening firestore
        if (favList.size > 0) {
            favList.clear()
        }
        db.collection("users").document(currentUserId).collection("Favourites")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w("TAG", "listen:error", e)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    favs_progressBar.visibility = View.GONE
                    val documentChangeList: List<DocumentChange> = snapshot.documentChanges

                    for (documentChange: DocumentChange in documentChangeList) {
                        documentChange.document.data
                        val fetchedFav =
                            FavouritesModel(
                                (documentChange.document.get("image").toString()),
                                documentChange.document.get("time").toString()
                            )
                        favList.add(fetchedFav)
                    }
                    favAdapter =
                        FavoutitesAdapter(
                            context!!,
                            favList
                        )
                    favourites_recycleView.adapter = favAdapter
                } else {
                    Log.d("TAG", "Query snapshot is null")
                }

            }
    }
}
