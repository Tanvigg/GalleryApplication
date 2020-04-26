package com.example.galleryapplication

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.fragment_category.view.*
import kotlinx.android.synthetic.main.fragment_time_line.view.*

/**
 * A simple [Fragment] subclass.
 */
class TimeLineFragment : Fragment() {
    private lateinit var mAuth: FirebaseAuth
    private lateinit var currentUserId: String
    private lateinit var db: FirebaseFirestore
    private lateinit var timeLineList: ArrayList<TimeLineModel>
    private lateinit var photosReference: StorageReference
    private lateinit var timeLineAdapter: TimeLineAdapter


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?

    ): View? {
        // Inflate the layout for this fragment
        val output: View = inflater.inflate(R.layout.fragment_time_line, container, false)
        timeLineList = arrayListOf()

        output.timeline_recyclerView.layoutManager = GridLayoutManager(context,3,GridLayoutManager.VERTICAL,false)
        output.timeline_recyclerView.setHasFixedSize(true)
        output.timeline_recyclerView.itemAnimator = DefaultItemAnimator()

        mAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        currentUserId = mAuth.currentUser!!.uid
        photosReference = FirebaseStorage.getInstance().reference.child("Images in Category")
        photosReference.listAll().addOnSuccessListener {
            for (i in it.items) {
                i.metadata.addOnSuccessListener {
                    val timeLineModel = TimeLineModel(i.downloadUrl, it.creationTimeMillis)
                    timeLineList.add(timeLineModel)
                    var tList : List<TimeLineModel>  = timeLineList.sortedByDescending {
                        it.timeStamp as Long }
                    timeLineAdapter = TimeLineAdapter(context!!,tList)
                    output.timeline_recyclerView!!.adapter = timeLineAdapter
                }

            }
        }
        return output
    }
}
