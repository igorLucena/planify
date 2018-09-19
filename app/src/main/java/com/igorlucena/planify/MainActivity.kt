package com.igorlucena.planify

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.support.v7.app.AppCompatActivity
import android.widget.Button
import org.jetbrains.anko.alert
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.net.URL

class MainActivity : AppCompatActivity() {

    var API_URL = "https://en.wikipedia.org/w/api.php?action=query&prop=revisions&rvprop=content&titles="
    val REQUEST_IMAGE_CAPTURE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val photo_button = findViewById<Button>(R.id.photo_button)

        photo_button.setOnClickListener {
            dispatchTakePictureIntent()
        }
    }

    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(packageManager) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            val extras = data!!.extras
            val especificationIntent = Intent(this, EspecificationActivity::class.java)
            especificationIntent.putExtras(extras)
            startActivity(especificationIntent)
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
