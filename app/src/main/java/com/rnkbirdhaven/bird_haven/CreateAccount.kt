package com.rnkbirdhaven.bird_haven

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class CreateAccount : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_account)

        //If the supportActionBar object is not null, the hide() method is called to hide the support action bar(see Splash Screen - Android Studio,2020)
        supportActionBar?.hide()

        // Write a message to the database(Android Knowledge,2023)
        var database = Firebase.database
        var reference = database.getReference("Name")

        //Declaring variables(The IIE,2023)
        val txtCreateAccountUsername = findViewById<TextView>(R.id.txtCreateAccountUsername)
        val txtCreateAccountPassword = findViewById<TextView>(R.id.txtCreateAccountPassword)

        //Declaring buttons(The IIE,2023)
        val btnCreateAccount2 = findViewById<Button>(R.id.btnCreateAccount2)
        val btnCreateAccountBack = findViewById<Button>(R.id.btnCreateAccountBack)

        //Button to go back to the main screen(The IIE,2023)
        btnCreateAccountBack.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        //Button when user register (Android Knowledge, 2023)
        btnCreateAccount2.setOnClickListener(View.OnClickListener {

            //Validation for text fields to ensure they are not left empty (Android Knowledge, 2023)
            if (txtCreateAccountUsername.text.toString().isEmpty()) {
                txtCreateAccountUsername.error = "This is a required field!"
            } else if (txtCreateAccountPassword.text.toString().isEmpty()) {
                txtCreateAccountPassword.error = "This is a required field!"
            } else {
                val username: String = txtCreateAccountUsername.text.toString()
                val password: String = txtCreateAccountPassword.text.toString()

                //Check if username has exactly 8 characters(Android Knowledge, 2023)
                if (username.length != 8) {
                    txtCreateAccountUsername.error = "Username must be 8 characters long!"
                    return@OnClickListener
                }

                //Check if password has exactly 8 characters(Android Knowledge, 2023)
                if (password.length != 8) {
                    txtCreateAccountPassword.error = "Password must be 8 characters long!"
                    return@OnClickListener
                }

                //Get an instance of the Firebase database and reference the "users" node (Android Knowledge, 2023)
                database = FirebaseDatabase.getInstance()
                reference = database.getReference("users")

                //Check if username already exists in the database (Android Knowledge, 2023)
                reference.child(username).addListenerForSingleValueEvent(object :
                    ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        if (dataSnapshot.exists()) {
                            //Username already exists, show error message (Android Knowledge, 2023)
                            txtCreateAccountUsername.error = "Username already exists!"
                            txtCreateAccountUsername.requestFocus()
                        } else {
                            //UserDetails object with the entered username and password (Android Knowledge, 2023)
                            val userDetails = UserDetails(username, password)

                            //Save the user details to the database under the username node (Android Knowledge, 2023)
                            reference.child(username).setValue(userDetails)

                            //Displaying a success message using a Toast and redirecting the user to the Login page (Android Knowledge, 2023)
                            Toast.makeText(
                                this@CreateAccount,
                                "You have registered successfully!",
                                Toast.LENGTH_SHORT
                            ).show()
                            val intent = Intent(this@CreateAccount, SignIn::class.java)
                            startActivity(intent)
                            finish()
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        //Handling the error if the onDataChange operation is canceled (Android Knowledge, 2023)
                        Toast.makeText(this@CreateAccount, "Error occurred", Toast.LENGTH_SHORT)
                            .show()
                    }
                })
            }
        })
    }
}