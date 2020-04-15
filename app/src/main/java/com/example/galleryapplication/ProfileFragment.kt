package com.example.galleryapplication

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import com.wang.avi.AVLoadingIndicatorView.BallSpinFadeLoader
import com.wang.avi.indicator.BallSpinFadeLoaderIndicator
import kotlinx.android.synthetic.main.fragment_profile.*
import kotlinx.android.synthetic.main.fragment_profile.view.*

/**
 * A simple [Fragment] subclass.
 */
class ProfileFragment : Fragment() {
    private lateinit var mAuth: FirebaseAuth
    private lateinit var currentUserId: String
    private lateinit var db: FirebaseFirestore
    private lateinit var loadingBar: ProgressDialog




    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val output: View = inflater.inflate(R.layout.fragment_profile, container, false)


        mAuth = FirebaseAuth.getInstance()
        currentUserId = mAuth.currentUser!!.uid
        db = FirebaseFirestore.getInstance()
        loadingBar = ProgressDialog(context, R.style.MyAlertDialogStyle)


        //in order to fetch user details stored in firebase cloud database
        fetchUserData()


        output.button_signOut.setOnClickListener {
            val builder = AlertDialog.Builder(context)
            builder.setMessage("Do You want to Logout of the app?")
            builder.setCancelable(true)
            builder.setPositiveButton("YES",object:DialogInterface.OnClickListener{

                override fun onClick(dialog: DialogInterface?, which: Int) {
                    mAuth.signOut()

                    val intent = Intent(context,MainActivity::class.java)
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    startActivity(intent)
                }
            })

            builder.setNegativeButton("NO",object:DialogInterface.OnClickListener{

                override fun onClick(dialog: DialogInterface?, which: Int) {
                    dialog!!.cancel()

                }
            })
            builder.create().show()

        }
            return output


    }

    private fun fetchUserData() {
        loadingBar.setTitle("Setting Profile ")
        loadingBar.setMessage("please wait, while we are getting your Details...")
        loadingBar.setCanceledOnTouchOutside(false)
        loadingBar.show()
       // ballspinfadeloader()
        val documentReference: DocumentReference? = db.collection("users").document(currentUserId.toString())
        documentReference!!.get().addOnCompleteListener {task ->
            if(task.isSuccessful){
                val doc : DocumentSnapshot = task.result!!
                if(doc.exists()){
                    Log.d("Document",doc.getData().toString())
                    userName.setText(doc.getData()!!.get("Name").toString())
                    userName.isEnabled = false
                    userEmail.setText(doc.getData()!!.get("Email").toString())
                    userEmail.isEnabled = false
                  // ballspinfadeloadergone()
                    Picasso.get().load(doc.data!!.get("Image").toString()).into(userProfileImage)

                }else{
                    Log.d("Document","NO DATA")
                }
                loadingBar.dismiss()


            }
        }

    }

    /*private fun ballspinfadeloadergone() {
        findViewById(R.id.BallSpinFadeLoader).visibility = GONE
    }





    private fun ballspinfadeloader(){
        findViewById(R.id.BallSpinFadeLoader).visibility = VISIBLE
    }*/
}
