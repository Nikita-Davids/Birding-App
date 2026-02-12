package com.rnkbirdhaven.bird_haven

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.io.ByteArrayOutputStream

class ObservationAdapter(
    private val context: Context,
    private val observations: MutableList<ObservationInfo>
) : BaseAdapter() {

    //Get the number of items in the list(Stefan,2022)
    override fun getCount(): Int = observations.size

    //Get the item at the specified position(Stefan,2022)
    override fun getItem(position: Int): Any = observations[position]

    //Get the item ID at the specified position(Stefan,2022)
    override fun getItemId(position: Int): Long = position.toLong()

    //Create and return a view for each item in the list(Stefan,2022)
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val observation = getItem(position) as ObservationInfo

        val inflater = LayoutInflater.from(context)
        val view: View

        //Reuse the convertView if available, otherwise inflate a new view(Stefan,2022)
        if (convertView == null) {
            view = inflater.inflate(R.layout.observation_item, parent, false)
        } else {
            view = convertView
        }

        //Declaring and initializing variables(The IIE,2023)
        val imageView: ImageView = view.findViewById(R.id.itemImageView)
        val locationTextView: TextView = view.findViewById(R.id.itemLocation)
        val descriptionTextView: TextView = view.findViewById(R.id.itemDescription)
        val dateTextView: TextView = view.findViewById(R.id.itemDate)

        //Declaring and initializing buttons(The IIE,2023)
        val editButton: Button = view.findViewById(R.id.editButton)
        val deleteButton: Button = view.findViewById(R.id.deleteButton)

        //Decode and set the image for the observation(Stefan,2022)
        val imageBytes = Base64.decode(observation.imageBase64, Base64.DEFAULT)
        val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
        imageView.setImageBitmap(bitmap)

        //Set the text for location, description, and date(Stefan,2022)
        locationTextView.text = observation.location
        descriptionTextView.text = observation.description
        dateTextView.text = observation.date

        //Handle edit button click(Stefan,2022)
        editButton.setOnClickListener {
            val position = getItemId(position).toInt()
            if (position != -1) {
                val observation = observations[position]
                showEditDialog(observation.id) //Pass the observation's ID(Stefan,2022)
            }
        }

        //Handle delete button click(Stefan,2022)
        deleteButton.setOnClickListener {
            val position = getItemId(position).toInt()
            if (position != -1) {
                val observation = observations[position]
                showDeleteConfirmationDialog(observation)
            }
        }

        return view
    }

    //Show the edit observation dialog(Chugh,2022)
    private fun showEditDialog(observationId: String) {
        val editDialogBuilder = AlertDialog.Builder(context)
        editDialogBuilder.setTitle("Edit Observation")

        val editView = LayoutInflater.from(context).inflate(R.layout.edit_observation_dialog, null)

        //Declaring and initializing variables(The IIE,2023)
        val etDescription = editView.findViewById<EditText>(R.id.etDescription)
        val etLocation = editView.findViewById<EditText>(R.id.etLocation)
        val etDate = editView.findViewById<EditText>(R.id.etDate)
        val imageView = editView.findViewById<ImageView>(R.id.imageView)

        val databaseReference: DatabaseReference =
            FirebaseDatabase.getInstance().getReference("Observations")

        //Find the observation in the Firebase database using its unique ID(Chugh,2022)
        val query = databaseReference.child(observationId)

        //Listen for data changes in the database(chaitanyamunje,2021)
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val observation = dataSnapshot.getValue(ObservationInfo::class.java)
                if (observation != null) {
                    //Set the values in the edit dialog
                    etDescription.setText(observation.description)
                    etLocation.setText(observation.location)
                    etDate.setText(observation.date)

                    //Load and display the image from the observation(Chugh,2022)
                    val decodedByteArray = Base64.decode(observation.imageBase64, Base64.DEFAULT)
                    val bitmap =
                        BitmapFactory.decodeByteArray(decodedByteArray, 0, decodedByteArray.size)
                    imageView.setImageBitmap(bitmap)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                //Handle the error if the retrieval operation is canceled(Android Knowledge,2023)
            }
        })

        //Set the view for the edit dialog(Chugh,2022)
        editDialogBuilder.setView(editView)

        //Handle the "Save" button click(Chugh,2022)
        editDialogBuilder.setPositiveButton("Save") { dialog, _ ->
            val newDescription = etDescription.text.toString()
            val newLocation = etLocation.text.toString()
            val newDate = etDate.text.toString()

            //Convert the updated image from the ImageView to a Base64 string(Chugh,2022)
            val updatedBitmap = (imageView.drawable as BitmapDrawable).bitmap
            val outputStream = ByteArrayOutputStream()
            updatedBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            val updatedImageByteArray = outputStream.toByteArray()
            val updatedImageBase64 = Base64.encodeToString(updatedImageByteArray, Base64.DEFAULT)

            //Update the observation in the Firebase database(Chugh,2022)
            query.child("description").setValue(newDescription)
            query.child("location").setValue(newLocation)
            query.child("date").setValue(newDate)
            query.child("imageBase64").setValue(updatedImageBase64)

            //Create a new ObservationInfo instance with the updated values(Chugh,2022)
            val updatedObservation = ObservationInfo(
                observationId,
                updatedImageBase64,
                newLocation,
                newDescription,
                newDate,
                ""
            )

            //Find the position of the observation in the list and replace it with the updated observation(Chugh,2022)
            val position = observations.indexOfFirst { it.id == observationId }
            if (position != -1) {
                observations[position] = updatedObservation
                notifyDataSetChanged()
            }
            dialog.dismiss()
        }

        //Handle the "Cancel" button click(Chugh,2022)
        editDialogBuilder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }

        val editDialog = editDialogBuilder.create()
        editDialog.show()
    }

    //Show the delete confirmation dialog(Chugh,2022)
    private fun showDeleteConfirmationDialog(observation: ObservationInfo) {
        val alertDialogBuilder = AlertDialog.Builder(context)
        alertDialogBuilder.setTitle("Confirm Action")
        alertDialogBuilder.setMessage("Do you want to delete this observation?")

        //Handle the "Delete" button click(Chugh,2022)
        alertDialogBuilder.setPositiveButton("Delete") { dialog, _ ->
            deleteObservation(observation)
            dialog.dismiss()
        }

        //Handle the "Cancel" button click(Chugh,2022)
        alertDialogBuilder.setNeutralButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }

        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    //Delete the observation(Chugh,2022)
    private fun deleteObservation(observation: ObservationInfo) {
        val databaseReference: DatabaseReference =
            FirebaseDatabase.getInstance().getReference("Observations")

        //Delete the observation using its unique ID
        databaseReference.child(observation.id).removeValue()

        //Find the position of the observation in the list and remove it(chaitanyamunje,2021)
        val position = observations.indexOfFirst { it.id == observation.id }
        if (position != -1) {
            observations.removeAt(position)
            notifyDataSetChanged()
        }
    }
}