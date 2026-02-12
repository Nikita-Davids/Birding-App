package com.rnkbirdhaven.bird_haven

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class Settings : Fragment() {

    //Declaring variables(The IIE,2023)
    private lateinit var btnLogOut: Button
    private lateinit var spinnerSystem: Spinner
    private lateinit var spinnerPreferredDistance: Spinner
    private lateinit var sharedPrefs: SharedPreferences
    private lateinit var sharedViewModel: SharedViewModel
    private val SYSTEM_PREF_KEY = "selected_system"
    private val PREFERRED_DISTANCE_PREF_KEY = "preferred_distance"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val layout = inflater.inflate(R.layout.fragment_settings, container, false)

        //If the supportActionBar object is not null, the hide() method is called to hide the support action bar(see Splash Screen - Android Studio,2020)
        val actionBar = (activity as AppCompatActivity).supportActionBar
        actionBar?.hide()

        //Initializing UI elements(The IIE,2023)
        btnLogOut = layout.findViewById(R.id.btnLogOut)
        spinnerSystem = layout.findViewById(R.id.spinnerSystem)
        spinnerPreferredDistance = layout.findViewById(R.id.spinnerPreferredDistance)

        //Defining the system options and distances arrays(The IIE,2023)
        val systemOptions = arrayOf("Imperial System(KM)", "Metric System(Miles)")
        val distances = arrayOf("10", "20", "30", "40", "50", "60", "70", "80", "90", "100")

        val linearEditProfile = layout.findViewById<LinearLayout>(R.id.linearEditProfile)

        //Creating ArrayAdapter for the system spinner(GeeksforGeeks,2022)
        val systemAdapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, systemOptions)
        systemAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerSystem.adapter = systemAdapter

        //Creating ArrayAdapter for the preferred distance spinner(GeeksforGeeks,2022)
        val distanceAdapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, distances)
        distanceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerPreferredDistance.adapter = distanceAdapter

        //Getting a reference to the SharedPreferences object for this app(Singh,2018)
        sharedPrefs = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)

        //Initializing the sharedViewModel to share data with the parent activity(Singh,2018)
        sharedViewModel = ViewModelProvider(requireActivity()).get(SharedViewModel::class.java)

        //Getting the currently logged-in user's UID(annianni,2021)
        val uid = FirebaseAuth.getInstance().currentUser?.uid

        //Getting the logUser (fallback to savedUsername if not available)(Android Knowledge,2023)
        var logUser = SignIn.loggedInUser
        if (logUser.isEmpty()) {
            //If logUser is empty, fallback to using the saved username from SharedPreferences(Singh,2018)
            val sharedPreferences =
                requireContext().getSharedPreferences("user_credentials", Context.MODE_PRIVATE)
            val savedUsername = sharedPreferences.getString("username", "")

            //Use the saved username as logUser if available(Android Knowledge,2023)
            if (!savedUsername.isNullOrEmpty()) {
                logUser = savedUsername
            }
        }

        //Determining the user identifier to use(annianni,2021)
        val userIdentifier = if (uid != null) {
            uid
        } else if (logUser.isNotEmpty()) {
            logUser
        } else {
            //If both uid and logUser are empty, fallback to using the saved username from SharedPreferences(Singh,2018)
            val sharedPreferences =
                requireContext().getSharedPreferences("user_credentials", Context.MODE_PRIVATE)
            val savedUsername = sharedPreferences.getString("username", "")
            savedUsername.toString()
        }

        //Create a reference to the Firebase Realtime Database(Android Knowledge,2023)
        val database = FirebaseDatabase.getInstance()

        //Creating a DatabaseReference pointing to the location where you want to save the(Android Knowledge,2023)
        val userRef = database.getReference("users").child(userIdentifier)

        //Retrieving the user's settings from Firebase Realtime Database(Android Knowledge,2023)
        userRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val selectedSystem =
                        dataSnapshot.child("selectedSystem").getValue(String::class.java)
                    val selectedDistance =
                        dataSnapshot.child("selectedDistance").getValue(Int::class.java)

                    //Update the UI with the retrieved settings(The IIE,2023)
                    if (selectedSystem != null) {
                        val position = systemOptions.indexOf(selectedSystem)
                        if (position != -1) {

                            //Setting the spinner to display the previously selected system(Singh,2018)
                            spinnerSystem.setSelection(position)

                            //Updating the selected system in the SharedViewModel(Singh,2018)
                            sharedViewModel.selectedDistanceUnit = selectedSystem
                        }
                    }

                    if (selectedDistance != null) {
                        val distanceString = selectedDistance.toString()
                        if (distances.contains(distanceString)) {
                            val position = distances.indexOf(distanceString)

                            //Setting the spinner to display the previously selected distance(Singh,2018)
                            spinnerPreferredDistance.setSelection(position)

                            //Updating the selected distance in the SharedViewModel(Singh,2018)
                            sharedViewModel.updatePreferredDistance(selectedDistance)
                        }
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                //Handle database read error(Android Knowledge,2023)
            }
        })

        //Setting an item selection listener for the 'spinnerSystem' dropdown(The IIE,2023)
        spinnerSystem.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {

                //Getting the selected measurement system from the 'systemOptions' array(Singh,2018)
                val selectedSystem = systemOptions[position]

                //Storing the selected system in the SharedViewModel for application-wide access(Singh,2018)
                sharedViewModel.selectedDistanceUnit = selectedSystem

                //Saving the selected system to SharedPreferences for persistent storage(Singh,2018)
                with(sharedPrefs.edit()) {
                    putString(SYSTEM_PREF_KEY, selectedSystem)
                    apply() //Committing the changes asynchronously(Singh,2018)
                }

                //Saving the selected system under the user's data in Firebase Database(Android Knowledge,2023)
                userRef.child("selectedSystem").setValue(selectedSystem)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                //Handling when nothing is selected(Android Knowledge,2023)
            }
        }

        //Setting an item selection listener for the 'spinnerPreferredDistance' dropdown(The IIE,2023)
        spinnerPreferredDistance.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {

                    //Getting the selected distance as an integer from the spinner(Singh,2018)
                    val selectedDistance = spinnerPreferredDistance.selectedItem.toString().toInt()

                    //Updating the selected distance in the SharedViewModel(Singh,2018)
                    sharedViewModel.updatePreferredDistance(selectedDistance)

                    //Saving the selected distance to SharedPreferences for persistence(Singh,2018)
                    sharedPrefs.edit()
                        .putString(PREFERRED_DISTANCE_PREF_KEY, selectedDistance.toString()).apply()

                    //Saving the selected distance under the user's data in Firebase Database(Android Knowledge,2023)
                    userRef.child("selectedDistance").setValue(selectedDistance)

                    //Getting the currentLatLng from the Hotspots fragment (if available)(Singh,2018)
                    val hotspotsFragment = parentFragment as? Explore

                    //Calling the method to update displayed hotspots in the Hotspots fragment(The IIE,2023)
                    hotspotsFragment?.updateDisplayedHotspots(selectedDistance)
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    //Handling when nothing is selected(Android Knowledge,2023)
                }
            }

        //Setting an item selection listener for linearEditProfile(The IIE,2023)
        linearEditProfile.setOnClickListener {
            //Inflate the profile edit layout from the dialog XML(Singh,2018)
            val inflater = LayoutInflater.from(requireContext())
            val dialogView = inflater.inflate(R.layout.profile_edit_layout, null)

            //Creating and show an AlertDialog with the inflated layout(Singh,2018)
            val alertDialog = AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .create()

            //Getting the logged-in user's username and password(Android Knowledge,2023)
            val logUser = SignIn.loggedInUser
            val logPassword = SignIn.loggedInUsersPassword

            //Getting the logged-in user's username and password from saved preferences(Singh,2018)
            val sharedPreferences =
                requireContext().getSharedPreferences("user_credentials", Context.MODE_PRIVATE)
            val loggedInUsersUsername = sharedPreferences.getString("username", "")
            val loggedInUsersPassword = sharedPreferences.getString("password", "")

            //Find views in the inflated layout(The IIE,2023)
            val textViewUsername = dialogView.findViewById<TextView>(R.id.editTextUsername)
            val editTextPassword = dialogView.findViewById<EditText>(R.id.editTextPassword)

            //Populating the UI elements based on the availability of data(The IIE,2023)
            if (logUser.isNotEmpty()) {
                //Use logged-in user's credentials(The IIE,2023)
                textViewUsername.text = logUser
                editTextPassword.setText(logPassword)
            } else {
                //Use saved preferences if logged-in user's credentials are empty(Singh,2018)
                textViewUsername.text = loggedInUsersUsername
                editTextPassword.setText(loggedInUsersPassword)
            }

            //Setting a click listener for the "Edit" button within the pop-up(The IIE,2023)
            val btnEdit = dialogView.findViewById<Button>(R.id.btnEdit)
            btnEdit.setOnClickListener {
                val newPassword = editTextPassword.text.toString()

                val usersRef = FirebaseDatabase.getInstance().getReference("users")

                //Check if the password meets the length requirement (8 characters)(Android Knowledge,2023)
                if (newPassword.length < 8) {
                    //Show an error message for an invalid password length(Android Knowledge,2023)
                    editTextPassword.error = "Password must be at least 8 characters long."
                } else {
                    //Updating the password field for the user(Chugh,2022)
                    usersRef.child(textViewUsername.text.toString()).child("password")
                        .setValue(newPassword)
                        .addOnSuccessListener {
                            Toast.makeText(
                                requireContext(),
                                "Successfully updated username and password!",
                                Toast.LENGTH_SHORT
                            ).show()

                            //Saving new password in sharedPreferences(Singh,2018)
                            val sharedPreferences = requireContext().getSharedPreferences(
                                "user_credentials",
                                Context.MODE_PRIVATE
                            )
                            sharedPreferences.edit().putString("password", newPassword).apply()

                            //Closing the AlertDialog after saving the information(Chugh,2022)
                            alertDialog.dismiss()
                        }
                        .addOnFailureListener { error ->
                            //Handling failure while updating username and password in the Realtime Database(The IIE,2023)
                            Toast.makeText(
                                requireContext(),
                                "Failed to update username and password!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                }
            }
            //Displaying dialog(Chugh,2022)
            alertDialog.show()
        }

        //Setting a click listener for the 'btnLogOut' button(The IIE,2023)
        btnLogOut.setOnClickListener {
            //Calling logOut methodS(annianni,2021)
            logOutAuth()
            logOutNormalSignIn()
        }
        return layout
    }

    //Function to sign out(annianni,2021)
    private fun logOutAuth() {
        //Log out from Firebase google authentication(annianni,2021)
        FirebaseAuth.getInstance().signOut()

        //Log out from Google Sign-In(annianni,2021)
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .build()
        val googleSignInClient = GoogleSignIn.getClient(requireContext(), gso)

        //Log out from Google Sign-In client(annianni,2021)
        googleSignInClient.signOut()
            .addOnCompleteListener(requireActivity()) {
                //Log-out was successful; navigate to the sign-in activity or do any necessary tasks(annianni,2021)

                //Create an Intent to start the SignIn activity(annianni,2021)
                val intent = Intent(requireActivity(), MainActivity::class.java)

                //Start the SignIn activity(annianni,2021)
                startActivity(intent)

                //Finishing the current activity to prevent going back to it(annianni,2021)
                requireActivity().finish()
            }
    }

    //Function to log out the user and reset loggedInUser(The IIE,2023)
    private fun logOutNormalSignIn() {
        //Clear the logged-in user information(The IIE,2023)
        SignIn.loggedInUser = ""
        SignIn.loggedInUsersPassword = ""

        //Remove saved credentials from SharedPreferences (Singh, 2018)
        clearUserCredentials()

        //Redirect the user back to the main activity or login screen(The IIE,2023)
        val intent = Intent(requireContext(), MainActivity::class.java)
        startActivity(intent)
        requireActivity().finish()
    }

    // Function to clear saved username and password in SharedPreferences (Singh, 2018)
    private fun clearUserCredentials() {
        // Clear saved credentials from SharedPreferences(The IIE,2023)
        val sharedPreferences =
            requireContext().getSharedPreferences("user_credentials", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.remove("username")
        editor.remove("password")
        editor.apply()
    }
}
