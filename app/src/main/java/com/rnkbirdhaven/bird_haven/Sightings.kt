package com.rnkbirdhaven.bird_haven

import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.util.Calendar
import java.util.Locale

class Sightings : Fragment() {

    //Declaring the variables(The IIE,2023)
    private lateinit var textViewCurrentLocation: TextView
    private lateinit var imageViewBird: ImageView
    private lateinit var editTextDescription: EditText
    private lateinit var txtDate: TextView
    private lateinit var btnUploadImage: Button
    private lateinit var btnTakePicture: Button
    private lateinit var btnPickDate: Button
    private lateinit var btnAddObservation: Button
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    //Setting desired width and height for image resizing(Suryavanshi,2021)
    private val desiredWidth = 500
    private val desiredHeight = 500

    //Setting request code for starting an activities for result(a_local_nobody,2022)
    private val REQUEST_CODE = 100
    private val REQUEST_IMAGE_CAPTURE = 1

    //Setting permission request code(The IIE,2023)
    private val PERMISSION_REQUEST_CODE = 101

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val layout = inflater.inflate(R.layout.fragment_sightings, container, false)

        //If the supportActionBar object is not null, the hide() method is called to hide the support action bar(see Splash Screen - Android Studio,2020)
        val actionBar = (activity as AppCompatActivity).supportActionBar
        actionBar?.hide()

        //Initialize the FusedLocationProviderClient(The IIE,2023)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        //Initializing variables(The IIE,2023)
        textViewCurrentLocation = layout.findViewById(R.id.textViewCurrentLocation)
        imageViewBird = layout.findViewById(R.id.imageViewBird)
        editTextDescription = layout.findViewById(R.id.editTextDescription)
        txtDate = layout.findViewById(R.id.txtDate)
        btnUploadImage = layout.findViewById(R.id.btnSelectImage)
        btnTakePicture = layout.findViewById(R.id.btnTakePicture)
        btnPickDate = layout.findViewById(R.id.btnPickDate)
        btnAddObservation = layout.findViewById(R.id.btnAddObservation)

        //Call getLocation() to populate the location when the fragment is created(The IIE,2023)
        getLocation()

        //Initializing buttons(The IIE,2023)
        btnUploadImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, REQUEST_CODE)
        }

        //Button to upload image(The IIE,2023)
        btnTakePicture.setOnClickListener {
            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            if (takePictureIntent.resolveActivity(requireActivity().packageManager) != null) {
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
            }
        }

        //Button to select a date(The IIE,2023)
        btnPickDate.setOnClickListener {
            showDatePicker()
        }

        //Button to add an observation to the database(The IIE,2023)
        btnAddObservation.setOnClickListener {
            if (textViewCurrentLocation.text.toString().isEmpty() ||
                editTextDescription.text.toString().isEmpty() ||
                txtDate.text.toString().isEmpty()
            ) {
                Toast.makeText(requireContext(), "Enter required fields", Toast.LENGTH_SHORT).show()
            } else {
                val bitmap = (imageViewBird.drawable as BitmapDrawable).bitmap

                //Error handling to ensure theres an image uploaded(The IIE,2023)
                if (bitmap.sameAs((resources.getDrawable(android.R.drawable.ic_menu_gallery) as BitmapDrawable).bitmap)) {
                    Toast.makeText(requireContext(), "Please upload an image", Toast.LENGTH_SHORT)
                        .show()
                    return@setOnClickListener
                }

                //Initializing variables(The IIE,2023)
                val location = textViewCurrentLocation.text.toString()
                val description = editTextDescription.text.toString()
                val date = txtDate.text.toString()

                val database = FirebaseDatabase.getInstance()

                //Generating a unique key for the observation(Android Knowledge,2023)
                val observationsRef = database.getReference("Observations")
                val observationKey = observationsRef.push().key

                if (observationKey != null) {
                    //Getting the saved username from SharedPreferences(Singh,2018)
                    val sharedPreferences = requireContext().getSharedPreferences(
                        "user_credentials",
                        Context.MODE_PRIVATE
                    )
                    val savedUsername = sharedPreferences.getString("username", "")
                    val auth = FirebaseAuth.getInstance()
                    val user: FirebaseUser? = auth.currentUser
                    var uid: String? = null

                    //Checking if a user is authenticated(The IIE,2023)
                    if (user != null) {
                        uid = user.uid
                    }

                    //Using the saved username if available, otherwise use loggedInUser(Singh,2018)
                    val usernameToUse = when {
                        !savedUsername.isNullOrEmpty() -> savedUsername
                        !SignIn.loggedInUser.isNullOrEmpty() -> SignIn.loggedInUser
                        uid != null -> uid
                        else -> "Unknown User"
                    }

                    //Creating the ObservationInfo object with the selected username(Android Knowledge,2023)
                    val observationInfo = ObservationInfo(
                        observationKey,
                        ObservationInfo.fromBitmap(bitmap, observationKey).imageBase64,
                        location,
                        description,
                        date,
                        usernameToUse //Using either the saved username, loggedInUser or the uid(The IIE,2023)
                    )

                    observationsRef.child(observationKey)
                        .setValue(observationInfo, object : DatabaseReference.CompletionListener {
                            override fun onComplete(
                                databaseError: DatabaseError?, databaseReference: DatabaseReference
                            ) {
                                //Displaying error message(The IIE,2023)
                                if (databaseError != null) {
                                    Toast.makeText(
                                        requireContext(),
                                        "Failed to add observation: ${databaseError.message}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    //Displaying success message(The IIE,2023)
                                } else {
                                    Toast.makeText(
                                        requireContext(),
                                        "Sighting successfully added",
                                        Toast.LENGTH_SHORT
                                    ).show()

                                    //Clearing UI elements(The IIE,2023)
                                    editTextDescription.setText("")
                                    txtDate.text = ""
                                    imageViewBird.setImageResource(android.R.drawable.ic_menu_gallery)
                                }
                            }
                        })
                }
            }
        }
        return layout
    }

    //Handles image upload results by retrieving the URI and resizing the image(a_local_nobody,2022)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            val imageBitmap = data?.extras?.get("data") as Bitmap
            imageViewBird.setImageBitmap(imageBitmap)
        } else if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            val imageUri: Uri? = data.data
            if (imageUri != null) {
                val contentResolver = requireActivity().contentResolver
                val resizedBitmap = resizeImage(imageUri, contentResolver)
                if (resizedBitmap != null) {
                    imageViewBird.setImageBitmap(resizedBitmap)
                }
            }
        }
    }


    //Resizing the image to desired dimensions(jww,2018)
    private fun resizeImage(uri: Uri, contentResolver: ContentResolver): Bitmap? {
        //Opening an input stream to read the image data from the provided URI(jww,2018)
        val imageStream = contentResolver.openInputStream(uri)

        //Creating options for decoding the image, set inJustDecodeBounds to true to retrieve image dimensions(jww,2018)
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true

        //Decoding the image stream to retrieve the image dimensions(jww,2018)
        BitmapFactory.decodeStream(imageStream, null, options)

        //Closing the image stream(jww,2018)
        imageStream?.close()

        //Opening a new input stream to read the image data(jww,2018)
        val inputStream = contentResolver.openInputStream(uri)

        //Setting inJustDecodeBounds to false and calculate the scaling factor for resizing the image(jww,2018)
        options.inJustDecodeBounds = false
        options.inSampleSize = calculateScaleFactor(options, desiredWidth, desiredHeight)

        //Decoding the image stream with the specified options and obtain the resized bitmap(jww,2018)
        val resizedBitmap = BitmapFactory.decodeStream(inputStream, null, options)

        //Closing the input stream(jww,2018)
        inputStream?.close()

        //Returning the resized bitmap(jww,2018)
        return resizedBitmap
    }

    //Calculating the scale factor for resizing the image to maintain aspect ratio(Suryavanshi,2021)
    private fun calculateScaleFactor(
        options: BitmapFactory.Options,
        desiredWidth: Int,
        desiredHeight: Int
    ): Int {
        //Retrieving the original width and height of the image from the BitmapFactory options(Suryavanshi,2021)
        val width = options.outWidth
        val height = options.outHeight

        //Initializing the scaleFactor to 1 (no scaling)(Suryavanshi,2021)
        var scaleFactor = 1

        //Checking if the original width or height is larger than the desired width or height(Suryavanshi,2021)
        if (width > desiredWidth || height > desiredHeight) {
            //Calculating the width and height ratios to determine the scaling factor(Suryavanshi,2021)
            val widthRatio = Math.round(width.toFloat() / desiredWidth.toFloat())
            val heightRatio = Math.round(height.toFloat() / desiredHeight.toFloat())

            //Choosing the smaller ratio as the scaleFactor to maintain aspect ratio(Suryavanshi,2021)
            scaleFactor = if (widthRatio < heightRatio) widthRatio else heightRatio
        }
        //Returning the calculated scaleFactor(Suryavanshi,2021)
        return scaleFactor
    }

    private fun showDatePicker() {
        //Getting the current date(chaitanyamunje,2022)
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)

        //Creating a DatePickerDialog and set the selected date(chaitanyamunje,2022)
        val datePickerDialog =
            DatePickerDialog(requireContext(), { _, selectedYear, selectedMonth, selectedDay ->
                //Updating the EditText with the selected date
                val formattedDate = formatDate(selectedYear, selectedMonth, selectedDay)
                txtDate.text = formattedDate
            }, year, month, dayOfMonth)

        //Showing the DatePickerDialog(chaitanyamunje,2022)
        datePickerDialog.show()
    }

    private fun formatDate(year: Int, month: Int, dayOfMonth: Int): String {
        //Formatting the date as desired (e.g., "YYYY-MM-DD")(chaitanyamunje,2022)
        val formattedYear = year.toString()
        val formattedMonth = (month + 1).toString().padStart(2, '0')
        val formattedDay = dayOfMonth.toString().padStart(2, '0')
        return "$formattedYear-$formattedMonth-$formattedDay"
    }

    //Function get current location of user(The IIE,2023)
    private fun getLocation() {
        //Prompting user to enable current location(The IIE,2023)
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSION_REQUEST_CODE
            )
        } else {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        val latitude = location.latitude
                        val longitude = location.longitude
                        val geocoder = Geocoder(requireContext(), Locale.getDefault())
                        val addresses = geocoder.getFromLocation(latitude, longitude, 1)

                        //Populating the textview with the current location(The IIE,2023)
                        if (addresses != null) {
                            if (addresses.isNotEmpty()) {
                                val locationName = addresses?.get(0)?.getAddressLine(0)
                                textViewCurrentLocation.text = locationName
                            }
                        }
                    } else {
                        //Display toast if current location is not retrieved(The IIE,2023)
                        Toast.makeText(
                            requireContext(),
                            "Unable to retrieve location",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }
    }
}
