package com.rnkbirdhaven.bird_haven

import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.rnkbirdhaven.bird_haven.ml.BirdsModel
import org.tensorflow.lite.support.image.TensorImage
import java.io.IOException

class BirdClassifier : AppCompatActivity() {

    //Declaring variables(The IIE,2023)
    private lateinit var btLoadImage: Button
    private lateinit var btCaptureImage: Button
    private lateinit var tvResult: TextView
    private lateinit var tvResult2: TextView
    private lateinit var ivAddImage: ImageView
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bird_classifier)

        //If the supportActionBar object is not null, the hide() method is called to hide the support action bar(see Splash Screen - Android Studio,2020)
        supportActionBar?.hide()

        //Initializing UI elements(The IIE,2023)
        ivAddImage = findViewById(R.id.iv_add_image)
        tvResult = findViewById(R.id.tv_result)
        tvResult2 = findViewById(R.id.tv_result2)
        btLoadImage = findViewById(R.id.iv_load_image)

        //Declaring and initializing back button(The IIE,2023)
        val btnBack = findViewById<Button>(R.id.btnBack)

        //Setting a click listener for the "Back" button(The IIE,2023)
        btnBack.setOnClickListener {
            finish()
        }

        //Initializing an ActivityResultLauncher for capturing an image(see How to Build an Application Like Google Lens in Android,2021)
        activityResultLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult(),
            ActivityResultCallback { result ->
                if (result.resultCode == RESULT_OK) {
                    val data = result.data
                    if (data != null) {
                        val imageUri: Uri? = data.data
                        try {
                            //Loading the captured image into the ImageView(see How to Build an Application Like Google Lens in Android,2021)
                            val imageBitmap: Bitmap =
                                MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
                            ivAddImage.setImageBitmap(imageBitmap)
                            //Processing and generate output from the image(see How to Build an Application Like Google Lens in Android,2021)
                            outputGenerator(imageBitmap)
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    }
                }
            })

        //Setting a click listener for the "Load Image" button(see How to Build an Application Like Google Lens in Android,2021)
        btLoadImage.setOnClickListener {
            //Creating an Intent to open the image picker (gallery)(see How to Build an Application Like Google Lens in Android,2021)
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            activityResultLauncher.launch(intent)
        }

        //Setting a click listener for the result TextView to open a web search(see How to Build an Application Like Google Lens in Android,2021)
        tvResult2.setOnClickListener {
            val searchQuery = tvResult.text.toString()
            if (searchQuery.isNotEmpty() && !searchQuery.equals("None", ignoreCase = true)) {
                val intent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://www.google.com/search?q=$searchQuery")
                )
                startActivity(intent)
            } else {
                //Displaying an AlertDialog for an invalid or "None" bird image(Singh,2018)
                AlertDialog.Builder(this)
                    .setTitle("Invalid Bird Image")
                    .setMessage("Please upload a valid bird image!")
                    .setPositiveButton("OK") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .show()
            }
        }

    }

    //Function to process the captured image and generate output(see How to Build an Application Like Google Lens in Android,2021)
    private fun outputGenerator(imageBitmap: Bitmap) {
        try {
            //Loading the machine learning model(see How to Build an Application Like Google Lens in Android,2021)
            val model = BirdsModel.newInstance(this)
            //Converting the image to a TensorImage(see How to Build an Application Like Google Lens in Android,2021)
            val image = TensorImage.fromBitmap(imageBitmap)
            //Processing the image with the model(see How to Build an Application Like Google Lens in Android,2021)
            val outputs = model.process(image)
            val probability = outputs.probabilityAsCategoryList

            var index = 0
            var max = probability[0].score

            //Finding the category with the highest probability(see How to Build an Application Like Google Lens in Android,2021)
            for (i in probability.indices) {
                if (max < probability[i].score) {
                    max = probability[i].score
                    index = i
                }
            }

            //Getting the name of the bird(see How to Build an Application Like Google Lens in Android,2021)
            val birdName = probability[index].label
            //Setting the bird name to the TextView(see How to Build an Application Like Google Lens in Android,2021)
            tvResult.text = birdName

            //Setting the visibility of tv_result2 based on whether birdName is empty(see How to Build an Application Like Google Lens in Android,2021)
            if (birdName.isNotEmpty()) {
                tvResult2.visibility = View.VISIBLE
            } else {
                tvResult2.visibility = View.GONE
            }

            //Closing the machine learning model(see How to Build an Application Like Google Lens in Android,2021)
            model.close()

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
