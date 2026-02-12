package com.rnkbirdhaven.bird_haven

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class Observation : Fragment() {

    //Declaring variables(annianni,2021)
    private lateinit var databaseReference: DatabaseReference
    private val observationList: MutableList<ObservationInfo> = mutableListOf()
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val layout = inflater.inflate(R.layout.fragment_observation, container, false)
        val actionBar = (activity as AppCompatActivity).supportActionBar
        actionBar?.hide()

        //Initializing variables for firebase google authentication(annianni,2021)
        auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser

        //Creating reference for observations node in firebase realtime database(Android Knowledge,2023)
        databaseReference = FirebaseDatabase.getInstance().getReference("Observations")
        val observationsListView: ListView = layout.findViewById(R.id.observationsListView)
        val adapter = ObservationAdapter(requireContext(), observationList)
        observationsListView.adapter = adapter

        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                observationList.clear()

                //Setting uid to current user logged in using google authentication(annianni,2021)
                val uid = currentUser?.uid

                //Getting the saved username from SharedPreferences(Singh,2018)
                val sharedPreferences =
                    requireContext().getSharedPreferences("user_credentials", Context.MODE_PRIVATE)
                val savedUsername = sharedPreferences.getString("username", "")

                //Checking if the right information is being received(The IIE,2023)
                Log.d("ObservationFragment", "savedUsername: $savedUsername")
                Log.d("ObservationFragment", "uid: $uid")
                Log.d("ObservationFragment", "loggedInUser: ${SignIn.loggedInUser}")

                for (observationSnapshot in snapshot.children) {
                    val observationInfo = observationSnapshot.getValue(ObservationInfo::class.java)
                    if (observationInfo != null) {
                        // Checking if savedUsername, UID, or loggedInUser matches the observation's username(The IIE,2023)
                        if ((!savedUsername.isNullOrEmpty() && observationInfo.username == savedUsername) ||
                            (uid != null && observationInfo.username == uid) ||
                            (observationInfo.username == SignIn.loggedInUser)
                        ) {
                            observationList.add(observationInfo)
                        }

                    }
                }

                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                //Handle error(Android Knowledge,2023)
            }
        })
        return layout
    }
}
