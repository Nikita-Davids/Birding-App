package com.rnkbirdhaven.bird_haven

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.*

class SignIn : AppCompatActivity() {

    //Declaring EditText variables for login username and password inputs(Android Knowledge,2023)
    private lateinit var loginUsername: EditText
    private lateinit var loginPassword: EditText

    //Declaring google authentication variables(The IIE,2023)
    lateinit var mGoogleSignInClient: GoogleSignInClient
    val Req_Code: Int = 123
    private lateinit var firebaseAuth: FirebaseAuth

    //Declaring a DatabaseReference variable to store a reference to the Firebase Realtime Database(Android Knowledge,2023)
    private lateinit var reference: DatabaseReference

    //Companion object to keep track of current user logged in, in other classes(Android Knowledge,2023)
    companion object {
        var loggedInUser: String = ""
        var loggedInUsersPassword: String = ""
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        //If the supportActionBar object is not null, the hide() method is called to hide the support action bar(see Splash Screen - Android Studio,2020)
        supportActionBar?.hide()

        //Initializing variables(The IIE,2023)
        loginUsername = findViewById(R.id.txtSignInUsername)
        loginPassword = findViewById(R.id.txtSignInPassword)

        //Declaring button(The IIE,2023)
        val btnSignInBack = findViewById<Button>(R.id.btnSignInBack)
        val btnLogin2 = findViewById<Button>(R.id.btnSignIn2)

        //Button to go back to the main screen(The IIE,2023)
        btnSignInBack.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        //Button to login(The IIE,2023)
        btnLogin2.setOnClickListener {
            if (validateUsername() && validatePassword()) {
                //Checking if both username and password are valid using validation functions(Singh,2018)
                val rememberMeCheckbox = findViewById<CheckBox>(R.id.checkBoxRememberMe)
                val rememberMeChecked = rememberMeCheckbox.isChecked

                if (rememberMeChecked) {
                    //If "Remember Me" is checked, save the credentials (username and password) in SharedPreferences(Singh,2018)
                    saveCredentials(loginUsername.text.toString(), loginPassword.text.toString())
                }
                //Calling the checkUser method(The IIE,2023)
                checkUser()
                val fragmentManager: FragmentManager = supportFragmentManager
                val fragmentTransaction: FragmentTransaction = fragmentManager.beginTransaction()

                //Creating the SettingsFragment instance(The IIE,2023)
                val settingsFragment = Settings()

                //Starting the SettingsFragment without adding it to the UI stack(The IIE,2023)
                fragmentTransaction.add(settingsFragment, "SettingsFragmentTag")
                fragmentTransaction.commit()
            }
        }

        //Getting reference to Firebase database(Android Knowledge,2023)
        reference = FirebaseDatabase.getInstance().getReference("users")

        //Authentication
        FirebaseApp.initializeApp(this)

        //Displaying google sign in options(annianni,2021)
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        //Initializing variables(annianni,2021)
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
        firebaseAuth = FirebaseAuth.getInstance()

        //Declaring and initializing google sign in button(annianni,2021)
        val signInButton = findViewById<com.google.android.gms.common.SignInButton>(R.id.bt_sign_in)

        //Set on click sign in button for google authentication(annianni,2021)
        signInButton.setOnClickListener {
            Toast.makeText(this, "Logging In", Toast.LENGTH_SHORT).show()
            signInGoogle()

            val fragmentManager: FragmentManager = supportFragmentManager
            val fragmentTransaction: FragmentTransaction = fragmentManager.beginTransaction()

            //Creating the SettingsFragment instance(The IIE,2023)
            val settingsFragment = Settings()

            //Starting the SettingsFragment without adding it to the UI stack(The IIE,2023)
            fragmentTransaction.add(settingsFragment, "SettingsFragmentTag")
            fragmentTransaction.commit()
        }
    }

    //Function to sign in to google(annianni,2021)
    private fun signInGoogle() {
        val signInIntent: Intent = mGoogleSignInClient.signInIntent
        startActivityForResult(signInIntent, Req_Code)
    }

    //onActivityResult() function: This is where we handle the result of the Google Sign-In process(annianni,2021)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        //Checking if the result is related to our specific request code(annianni,2021)
        if (requestCode == Req_Code) {
            //Obtaining the task containing the GoogleSignInAccount from the provided data(annianni,2021)
            val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
            //Calling the handleResult function to process the result(annianni,2021)
            handleResult(task)
        }
    }

    private fun handleResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            //Trying to retrieve the GoogleSignInAccount from the completed task(annianni,2021)
            val account: GoogleSignInAccount? = completedTask.getResult(ApiException::class.java)

            //Checkinf if the account is not null (i.e., sign-in was successful)(annianni,2021)
            if (account != null) {
                //Calling a function to update the user interface with the signed-in account information(annianni,2021)
                UpdateUI(account)
            }
        } catch (e: ApiException) {
            //Show a Toast message with the error details(annianni,2021)
            Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show()
        }
    }


    //This is where we update the UI after Google sign-in takes place(annianni,2021)
    private fun UpdateUI(account: GoogleSignInAccount) {
        //Creating Firebase authentication credentials using the Google Sign-In account's ID token(annianni,2021)
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)

        //Signing in to Firebase using the provided credential and handle the result(annianni,2021)
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                //Signing-in was successful; navigate to the Welcome activity
                val intent = Intent(this, Welcome::class.java)
                startActivity(intent)

                //Finishing the current activity to prevent going back to it(annianni,2021)
                finish()
            }
        }
    }


    override fun onStart() {
        super.onStart()

        //Checking if there is a previously signed-in Google account(annianni,2021)
        if (GoogleSignIn.getLastSignedInAccount(this) != null) {
            //If a previously signed-in account exists, automatically navigate to the Welcome activity(annianni,2021)

            //Creating an Intent to start the Welcome activity(annianni,2021)
            val intent = Intent(this, Welcome::class.java)

            //Starting the Welcome activity(annianni,2021)
            startActivity(intent)

            //Finishing the current activity to prevent going back to it(annianni,2021)
            finish()
        }
    }


    //Function to validate username(Android Knowledge,2023)
    private fun validateUsername(): Boolean {
        val validate = loginUsername.text.toString()
        return if (validate.isEmpty()) {
            loginUsername.error = "Username cannot be empty!"
            false
        } else {
            loginUsername.error = null
            true
        }
    }

    //Function to validate password(Android Knowledge,2023)
    private fun validatePassword(): Boolean {
        val validate = loginPassword.text.toString()
        return if (validate.isEmpty()) {
            loginPassword.error = "Password cannot be empty!"
            false
        } else {
            loginPassword.error = null
            true
        }
    }

    //Function to check if user exists in the database(Android Knowledge,2023)
    private fun checkUser() {
        // Get username and password entered by user(Android Knowledge,2023)
        val userUsername = loginUsername.text.toString().trim()
        val userPassword = loginPassword.text.toString().trim()

        //Query the database to check if user exists(Android Knowledge,2023)
        val checkUserDatabase = reference.orderByChild("username").equalTo(userUsername)

        //Add listener for the query result(Android Knowledge,2023)
        checkUserDatabase.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    //If user exists, get the password from the database and compare it with the entered password(Android Knowledge,2023)
                    val passwordFromDB =
                        snapshot.child(userUsername).child("password").getValue(String::class.java)
                    if (passwordFromDB == userPassword) {
                        //If passwords match, get the username from the database and the user will be redirected to the Welcome page(Android Knowledge,2023)
                        val usernameFromDB = snapshot.child(userUsername).child("username")
                            .getValue(String::class.java)

                        //Keeping track of current user logged in, in other classes(Android Knowledge,2023)
                        SignIn.loggedInUser = usernameFromDB ?: ""

                        //Keeping track of current user logged in users password, in other classes(Android Knowledge,2023)
                        SignIn.loggedInUsersPassword = passwordFromDB ?: ""

                        val intent = Intent(this@SignIn, Welcome::class.java)
                        intent.putExtra("username", usernameFromDB)
                        intent.putExtra("password", passwordFromDB)
                        startActivity(intent)
                        finish()
                    } else {
                        //If passwords do not match, show error message(Android Knowledge,2023)
                        loginPassword.error = "Invalid Credentials!"
                        loginPassword.requestFocus()
                    }
                } else {
                    //If user does not exist, show error message(Android Knowledge,2023)
                    loginUsername.error = "User does not exist!"
                    loginUsername.requestFocus()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                //Handling the error if the onDataChange operation is canceled(Android Knowledge,2023)
                Toast.makeText(this@SignIn, "Error occurred", Toast.LENGTH_SHORT).show()
            }
        })
    }

    //Function to save credentials if remember me checkbox is ticked(Singh,2018)
    private fun saveCredentials(username: String, password: String) {
        //Getting a reference to the SharedPreferences for storing user credentials(Singh,2018)
        val sharedPreferences = getSharedPreferences("user_credentials", Context.MODE_PRIVATE)

        //Creating an editor for making changes to the SharedPreferences(Singh,2018)
        val editor = sharedPreferences.edit()

        //Storing the username and password as key-value pairs in SharedPreferences(Singh,2018)
        editor.putString("username", username) //Saving the username(Singh,2018)
        editor.putString("password", password) //Saving the password(Singh,2018)

        //Applying the changes to the SharedPreferences to save the credentials(Singh,2018)
        editor.apply()
    }
}