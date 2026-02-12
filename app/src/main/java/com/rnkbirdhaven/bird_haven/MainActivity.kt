package com.rnkbirdhaven.bird_haven

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //If the supportActionBar object is not null, the hide() method is called to hide the support action bar(see Splash Screen - Android Studio,2020)
        supportActionBar?.hide()

        //Getting references to the "Create Account" and "Sign In" buttons in the layout(The IIE,2023)
        val btnCreateAccount = findViewById<Button>(R.id.btnCreateAccount)
        val btnSignIn = findViewById<Button>(R.id.btnSignIn)

        //Setting a click listener for the "Create Account" button(The IIE,2023)
        btnCreateAccount.setOnClickListener {
            //Creating an Intent to navigate to the CreateAccount activity(The IIE,2023)
            val intent = Intent(this, CreateAccount::class.java)
            //Starting the CreateAccount activity (The IIE,2023)
            startActivity(intent)
        }

        //Setting a click listener for the "Sign In" button(The IIE,2023)
        btnSignIn.setOnClickListener {
            //Declaring and initializing variable(The IIE,2023)
            val sharedPreferences = getSharedPreferences("user_credentials", Context.MODE_PRIVATE)
            val savedUsername = sharedPreferences.getString("username", "")
            val savedPassword = sharedPreferences.getString("password", "")

            if (!savedUsername.isNullOrEmpty() && !savedPassword.isNullOrEmpty()) {
                val fragmentManager: FragmentManager = supportFragmentManager
                val fragmentTransaction: FragmentTransaction = fragmentManager.beginTransaction()

                //Creating the SettingsFragment instance(The IIE,2023)
                val settingsFragment = Settings()

                //Starting the SettingsFragment without adding it to the UI stack(The IIE,2023)
                fragmentTransaction.add(settingsFragment, "SettingsFragmentTag")
                fragmentTransaction.commit()

                //Credentials are saved, you can auto-fill the login fields if needed and redirect the user to the home fragment(The IIE,2023)
                val intent = Intent(this, Welcome::class.java)
                startActivity(intent)
            } else {
                //No saved credentials, proceed with normal login flow(The IIE,2023)
                val intent = Intent(this, SignIn::class.java)
                startActivity(intent)
            }

            val fragmentManager: FragmentManager = supportFragmentManager
            val fragmentTransaction: FragmentTransaction = fragmentManager.beginTransaction()

            //Creating the SettingsFragment instance(The IIE,2023)
            val settingsFragment = Settings()

            //Starting the SettingsFragment without adding it to the UI stack(The IIE,2023)
            fragmentTransaction.add(settingsFragment, "SettingsFragmentTag")
            fragmentTransaction.commit()
        }
    }
}
