package com.igorlucena.planify

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.support.annotation.RequiresApi
import android.support.v7.app.AppCompatActivity
import android.widget.ImageButton
import org.jetbrains.anko.*
import java.time.ZoneId
import java.util.*

class MainActivity : AppCompatActivity() {

    val REQUEST_IMAGE_CAPTURE = 1
    var progressBarIntent = Intent()
    val RESTRICTIONS_VISION_API = "restrictions"
    val MAX_RESTRICTIONS = 50


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val photoButton = findViewById<ImageButton>(R.id.photo_button)

        progressBarIntent = Intent(this, ProgressBarActivity::class.java)

        val sharedPreferences = getSharedPreferences(RESTRICTIONS_VISION_API, Context.MODE_PRIVATE)

        photoButton.setOnClickListener {
            val date = Date()
            val month = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
                localDate.monthValue
            } else {
                val cal = Calendar.getInstance()
                cal.time = date
                cal.get(Calendar.MONTH)
            }
            val restrictions = sharedPreferences.getInt(month.toString(), 0)
            if (restrictions < MAX_RESTRICTIONS) {
                if (isNetworkConnected()) {
                    dispatchTakePictureIntent()
                } else {
                    val message = resources.getString(R.string.no_connection)
                    startActivity(intentFor<ErrorActivity>("error" to message, "max_restrictions" to MAX_RESTRICTIONS))
                }
            } else {
                longToast("Usted ha utilizado el máximo de $MAX_RESTRICTIONS solicitudes por mes. Espera el próximo mes para continuar utilizando la aplicación.")
            }
        }
    }

    private fun isNetworkConnected(): Boolean {
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        return cm.activeNetworkInfo != null
    }

    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(packageManager) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            val extras = data!!.extras
            extras.putInt("max_restrictions", MAX_RESTRICTIONS)

            progressBarIntent.putExtras(extras)
            startActivity(progressBarIntent)
        }
    }

}
