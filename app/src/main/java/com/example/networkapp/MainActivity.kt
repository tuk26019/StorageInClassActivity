package com.example.networkapp

import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.squareup.picasso.Picasso
import org.json.JSONObject
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.IOException
import java.io.FileOutputStream

// TODO (1: Fix any bugs)
// TODO (2: Add function saveComic(...) to save and load comic info automatically when app starts)

private const val AUTO_SAVE_KEY = "auto_save"

class MainActivity : AppCompatActivity() {

    private lateinit var requestQueue: RequestQueue
    lateinit var titleTextView: TextView
    lateinit var descriptionTextView: TextView
    lateinit var numberEditText: EditText
    lateinit var showButton: Button
    lateinit var comicImageView: ImageView

    private var autoSave = false
    private lateinit var preferences: SharedPreferences

    private val internalFileName = "comic_info"
    private lateinit var file: File
    private var comicImageUrl: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        file = File(filesDir, internalFileName)
        preferences = getPreferences(MODE_PRIVATE)

        requestQueue = Volley.newRequestQueue(this)

        titleTextView = findViewById<TextView>(R.id.comicTitleTextView)
        descriptionTextView = findViewById<TextView>(R.id.comicDescriptionTextView)
        numberEditText = findViewById<EditText>(R.id.comicNumberEditText)
        showButton = findViewById<Button>(R.id.showComicButton)
        comicImageView = findViewById<ImageView>(R.id.comicImageView)

        showButton.setOnClickListener {
            downloadComic(numberEditText.text.toString())
        }

        if (savedInstanceState != null) {
            // Restore the state
            titleTextView.text = savedInstanceState.getString("comicTitle")
            descriptionTextView.text = savedInstanceState.getString("comicDescription")
            comicImageUrl = savedInstanceState.getString("comicImageUrl")
            if (comicImageUrl != null) {
                Picasso.get().load(comicImageUrl).into(comicImageView)
            }
        } else if (file.exists()) {
            // Load the comic if the file exists and the activity is not being recreated
            val comicObject = loadComic()
            if (comicObject != null) {
                showComic(comicObject)
            }
        }

        autoSave = preferences.getBoolean(AUTO_SAVE_KEY, false)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // Save the comic title, description, and image URL
        outState.putString("comicTitle", titleTextView.text.toString())
        outState.putString("comicDescription", descriptionTextView.text.toString())
        if (comicImageUrl != null) {
            outState.putString("comicImageUrl", comicImageUrl)
        }
    }

    private fun downloadComic(comicId: String) {
        val url = "https://xkcd.com/$comicId/info.0.json"
        requestQueue.add(
            JsonObjectRequest(url, { showComic(it)
                saveComic(it) }, {
            })
        )
    }

    private fun showComic(comicObject: JSONObject) {
        titleTextView.text = comicObject.getString("title")
        descriptionTextView.text = comicObject.getString("alt")
        comicImageUrl = comicObject.getString("img")
        Picasso.get().load(comicImageUrl).into(comicImageView)
    }

    private fun saveComic(comicObject: JSONObject) {
        if (autoSave) {
            try {
                val outputStream = FileOutputStream(file)
                outputStream.write(comicObject.toString().toByteArray())
                outputStream.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun loadComic(): JSONObject? {
        return try {
            val br = BufferedReader(FileReader(file))
            val text = StringBuilder()
            var line: String?
            while (br.readLine().also { line = it } != null) {
                text.append(line)
                text.append('\n')
            }
            br.close()
            JSONObject(text.toString())
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }
}