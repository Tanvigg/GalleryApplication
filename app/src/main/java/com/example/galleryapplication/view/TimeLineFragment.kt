package com.example.galleryapplication.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import com.example.galleryapplication.R
import com.example.galleryapplication.viewmodel.FirebaseViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.fragment_time_line.view.*

/**
 * A simple [Fragment] subclass.
 */
class TimeLineFragment : Fragment() {
    private lateinit var timeLineAdapter: TimeLineAdapter
    private val viewModel: FirebaseViewModel by lazy {
        ViewModelProvider(this).get(FirebaseViewModel::class.java)
    }



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?

    ): View? {
        // Inflate the layout for this fragment
        val output: View = inflater.inflate(R.layout.fragment_time_line, container, false)

        timeLineAdapter = TimeLineAdapter(context!!)

        viewModel.fetchTimeLine().observe(viewLifecycleOwner, Observer{times->
            times.let{
                timeLineAdapter.setImage(it)
                output.timeline_recyclerView.adapter = timeLineAdapter
                output.timeline_recyclerView.layoutManager = GridLayoutManager(context,3,GridLayoutManager.VERTICAL,false)
                output.timeline_recyclerView.itemAnimator = DefaultItemAnimator()


            }
        })
        return output
    }
}
