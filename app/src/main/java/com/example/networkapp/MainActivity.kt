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
    private lateinit var textBox: EditText
    private lateinit var checkBox: CheckBox

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
        if(/*file exists*/){
            showComic(loadComic())
        }

        autoSave = preferences.getBoolean(AUTO_SAVE_KEY, false)
        textBox = findViewById(R.id.comicDescriptionTextView)
        checkBox = findViewById(R.id.comicImageView)

        checkBox.isChecked = autoSave
        autoSave = preferences.getBoolean(AUTO_SAVE_KEY, false)

        if (autoSave && file.exists()) {
            try {
                val br = BufferedReader(FileReader(file))
                val text = StringBuilder()
                var line: String?
                while (br.readLine().also { line = it } != null) {
                    text.append(line)
                    text.append('\n')
                }
                br.close()
                textBox.setText(text.toString())
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        checkBox.setOnCheckedChangeListener { _, isChecked ->
            autoSave = isChecked

            // Update shared preferences when toggled
            val editor = preferences.edit()
            editor.putBoolean(AUTO_SAVE_KEY, autoSave)
            editor.apply()
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
        Picasso.get().load(comicObject.getString("img")).into(comicImageView)
    }

    private fun saveComic(comicObject: JSONObject) {
        if (autoSave) {
            try {
                val outputStream = FileOutputStream(file)
                outputStream.write(textBox.text.toString().toByteArray())
                outputStream.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    private fun loadComic(){

    }
}