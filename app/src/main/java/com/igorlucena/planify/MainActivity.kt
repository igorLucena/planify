package com.igorlucena.planify

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Button
import android.os.AsyncTask
import android.util.Log
import android.widget.ProgressBar
import org.jetbrains.anko.alert
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.io.BufferedInputStream
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.MalformedURLException
import java.net.URL


class MainActivity : AppCompatActivity() {

    var API_URL = "https://en.wikipedia.org/w/api.php?action=query&prop=revisions&rvprop=content&titles="

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val photo_button = findViewById<Button>(R.id.photo_button)

        photo_button.setOnClickListener {
            val intent = Intent("android.media.action.IMAGE_CAPTURE")
            startActivity(intent)
        }

        catchWikipedia()
    }

    private fun catchWikipedia() {
        doAsync {
            API_URL += "Boeing%20777"
            val response = URL(API_URL).readText()

            uiThread {
                alert(response).show()
            }
        }
    }
}
