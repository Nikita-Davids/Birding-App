package com.rnkbirdhaven.bird_haven

import android.os.AsyncTask
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*

class Weather : AppCompatActivity() {
    //API key for accessing the OpenWeatherMap API(see Make a Weather App for Android, 2020)
    val API: String = "06c921750b9a82d8f5d1294e1586276f"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_weather)

        //If the supportActionBar object is not null, the hide() method is called to hide the support action bar(see Splash Screen - Android Studio,2020)
        supportActionBar?.hide()

        //Declaring and initializing button(The IIE,2023)
        val btnUpdateWeather: Button = findViewById(R.id.btnUpdateWeather)

        //Setting a click listener for the "Update Weather" button(The IIE,2023)
        btnUpdateWeather.setOnClickListener {
            //Declaring and initializing edit text for city name(The IIE,2023)
            val editTextCity: EditText = findViewById(R.id.editTextCity)
            //Retrieving the entered city name from the EditText(The IIE,2023)
            val city: String = editTextCity.text.toString()

            //Checking if the entered city name is not blank(The IIE,2023)
            if (city.isNotBlank()) {
                //If not blank, execute the weatherTask AsyncTask with the entered city(The IIE,2023)
                weatherTask().execute(city)
            } else {
                //If blank, set an error message on the EditText(The IIE,2023)
                editTextCity.error = "Please enter a city!"
            }
        }

        //Declaring and initializing back button(The IIE,2023)
        val btnBack = findViewById<Button>(R.id.btnBack)

        //Setting a click listener for the "Back" button(The IIE,2023)
        btnBack.setOnClickListener {
            finish()
        }
    }

    inner class weatherTask() : AsyncTask<String, Void, String>() {
        override fun onPreExecute() {
            //Calling the superclass method to perform any necessary setup(see Make a Weather App for Android, 2020)
            super.onPreExecute()

            //Showing the ProgressBar to indicate that data is being loaded(see Make a Weather App for Android, 2020)
            findViewById<ProgressBar>(R.id.loader).visibility = View.VISIBLE

            //Hiding the main container layout to make it invisible during data loading(see Make a Weather App for Android, 2020)
            findViewById<RelativeLayout>(R.id.mainContainer).visibility = View.GONE

            //Hiding any previous error message text view(see Make a Weather App for Android, 2020)
            findViewById<TextView>(R.id.errorText).visibility = View.GONE
        }


        override fun doInBackground(vararg params: String?): String? {
            //Variable to store the API response(see Make a Weather App for Android, 2020)
            var response: String?

            try {
                //Use the CITY variable from the EditText for the weather API request(see Make a Weather App for Android, 2020)
                val editTextCity: EditText = findViewById(R.id.editTextCity)
                val CITY: String = editTextCity.text.toString()

                //Constructing the URL for the OpenWeatherMap API request using the entered city and API key(see Make a Weather App for Android, 2020)
                val apiUrl = "https://api.openweathermap.org/data/2.5/weather?q=${CITY}&units=metric&appid=${API}"

                //Perform the API request and read the response into the 'response' variable(see Make a Weather App for Android, 2020)
                response = URL(apiUrl).readText(Charsets.UTF_8)
            } catch (e: Exception) {
                //If an exception occurs during the API request, set the response to null(see Make a Weather App for Android, 2020)
                response = null
            }

            //Return the API response (or null if an exception occurred)(see Make a Weather App for Android, 2020)
            return response
        }


        override fun onPostExecute(result: String?) {
            //Calling the superclass method to perform any necessary cleanup(see Make a Weather App for Android, 2020)
            super.onPostExecute(result)

            try {
                //Parsing the JSON response from the OpenWeatherMap API(see Make a Weather App for Android, 2020)
                val jsonObj = JSONObject(result)
                val main = jsonObj.getJSONObject("main")
                val sys = jsonObj.getJSONObject("sys")
                val wind = jsonObj.getJSONObject("wind")
                val weather = jsonObj.getJSONArray("weather").getJSONObject(0)

                //Extracting relevant weather information from the JSON response(see Make a Weather App for Android, 2020)
                val updatedAt: Long = jsonObj.getLong("dt")
                val updatedAtText = "Updated at: " + SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.ENGLISH).format(Date(updatedAt * 1000))
                val temp = main.getString("temp") + "°C"
                val tempMin = "Min Temp: " + main.getString("temp_min") + "°C"
                val tempMax = "Max Temp: " + main.getString("temp_max") + "°C"
                val pressure = main.getString("pressure")
                val humidity = main.getString("humidity")

                val sunrise: Long = sys.getLong("sunrise")
                val sunset: Long = sys.getLong("sunset")
                val windSpeed = wind.getString("speed")
                val weatherDescription = weather.getString("description")

                val address = jsonObj.getString("name") + ", " + sys.getString("country")

                //Populating extracted data into our views(see Make a Weather App for Android, 2020)
                findViewById<TextView>(R.id.address).text = address
                findViewById<TextView>(R.id.updated_at).text = updatedAtText
                findViewById<TextView>(R.id.status).text = weatherDescription.capitalize()
                findViewById<TextView>(R.id.temp).text = temp
                findViewById<TextView>(R.id.temp_min).text = tempMin
                findViewById<TextView>(R.id.temp_max).text = tempMax
                findViewById<TextView>(R.id.sunrise).text = SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(Date(sunrise * 1000))
                findViewById<TextView>(R.id.sunset).text = SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(Date(sunset * 1000))
                findViewById<TextView>(R.id.wind).text = windSpeed
                findViewById<TextView>(R.id.humidity).text = humidity

                //Views populated, Hiding the loader, Showing the main design(see Make a Weather App for Android, 2020)
                //Hiding the ProgressBar
                findViewById<ProgressBar>(R.id.loader).visibility = View.GONE
                //Showing the main layout
                findViewById<RelativeLayout>(R.id.mainContainer).visibility = View.VISIBLE

            } catch (e: Exception) {
                //If an exception occurs during parsing or populating views(see Make a Weather App for Android, 2020)

                //Hiding the ProgressBar(see Make a Weather App for Android, 2020)
                findViewById<ProgressBar>(R.id.loader).visibility = View.GONE

                //Showing the main layout(see Make a Weather App for Android, 2020)
                findViewById<RelativeLayout>(R.id.mainContainer).visibility = View.VISIBLE

                //Displaying an error message next to the EditText(see Make a Weather App for Android, 2020)
                val editTextCity: EditText = findViewById(R.id.editTextCity)
                editTextCity.error = "Please enter a valid city!"
            }
        }
    }
}
