package com.example.galleryapplication.view.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import com.example.galleryapplication.R
import com.example.galleryapplication.model.showToast
import com.example.galleryapplication.view.adapter.TimeLineAdapter
import com.example.galleryapplication.viewmodel.TimeLineViewModel
import kotlinx.android.synthetic.main.fragment_time_line.*
import kotlinx.android.synthetic.main.fragment_time_line.view.*

/**
 * A simple [Fragment] subclass.
 */
class TimeLineFragment : Fragment() {
    private lateinit var timeLineAdapter: TimeLineAdapter
    private val viewModel: TimeLineViewModel by lazy {
        ViewModelProvider(this).get(TimeLineViewModel::class.java)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?

    ): View? {
        // Inflate the layout for this fragment
        val output: View = inflater.inflate(R.layout.fragment_time_line, container, false)
        setObserver()
        viewModel.fetchTimeLine().observe(viewLifecycleOwner, Observer {
            timeLineAdapter =
                TimeLineAdapter(
                    it,
                    context!!
                )
            Log.d("imageIt",it.toString())
            output.timeline_recyclerView.layoutManager =
                GridLayoutManager(context, 3, GridLayoutManager.VERTICAL, false)
            output.timeline_recyclerView.itemAnimator = DefaultItemAnimator()
            output.timeline_recyclerView.adapter = timeLineAdapter
        })
        return output
    }

    private fun setObserver() {
        viewModel.getTimeLineStatus().observe(viewLifecycleOwner, Observer {
            when (it) {
                TimeLineViewModel.TimeLineStatus.SHOW_PROGRESS -> showProgress()
                TimeLineViewModel.TimeLineStatus.HIDE_PROGRESS -> hideProgress()
            }
        })

        viewModel.getError().observe(viewLifecycleOwner, Observer {
            context!!.showToast(it)

        })
    }

    private fun showProgress() {
        linearscaleprogressloader.visibility = View.VISIBLE

    }

    private fun hideProgress() {
        linearscaleprogressloader.visibility = View.GONE
    }
}
