package com.rnkbirdhaven.bird_haven

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import java.io.ByteArrayOutputStream

//Defining a data class representing sightings/ observations information it holds the information of a movie(Mohabia,2018)
data class ObservationInfo(
    var id: String,
    var imageBase64: String,
    var location: String,
    var description: String,
    var date: String,
    var username: String
) {
    //No-argument constructor required for Firebase deserialization(Brusov,2018)
    constructor() : this("", "", "", "", "", "")

    //Companion object containing utility methods for converting images to/from ObservationInfo objects(Mishra,2016)
    companion object {
        //Converting a Bitmap object to a MovieInfo object(Mishra,2016)
        fun fromBitmap(bitmap: Bitmap, id: String): ObservationInfo {
            //Creating a ByteArrayOutputStream to hold the image bytes(Mishra,2016)
            val outputStream = ByteArrayOutputStream()
            //Compressing the bitmap to PNG format and store the result in the outputStream(Mishra,2016)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            //Getting the image bytes from the outputStream(Mishra,2016)
            val imageBytes = outputStream.toByteArray()
            //Encoding the image bytes to a Base64 string(Mishra,2016)
            val imageBase64 = Base64.encodeToString(imageBytes, Base64.DEFAULT)
            //Creating a MovieInfo object with the imageBase64 as the image representation(Mishra,2016)
            return ObservationInfo(id, imageBase64, "", "", "", "")
        }

        //Converting a ObservationInfo object to a Bitmap object(Mishra,2016)
        fun toBitmap(obeservationInfo: ObservationInfo): Bitmap {
            //Decoding the Base64 string to get the image bytes(Mishra,2016)
            val imageBytes = Base64.decode(obeservationInfo.imageBase64, Base64.DEFAULT)
            //Decoding the image bytes to a Bitmap object(Mishra,2016)
            return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
        }
    }
}
