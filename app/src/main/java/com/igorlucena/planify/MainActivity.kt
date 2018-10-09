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
import android.widget.Button
import org.jetbrains.anko.*
import java.time.ZoneId
import java.util.*

class MainActivity : AppCompatActivity() {

    val REQUEST_IMAGE_CAPTURE = 1
    var specificationIntent = Intent()
    val RESTRICTIONS_VISION_API = "restrictions"


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val photoButton = findViewById<Button>(R.id.photo_button)

        specificationIntent = Intent(this, SpecificationActivity::class.java)

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
            if (restrictions < 20) {
                if (isNetworkConnected()) {
                    dispatchTakePictureIntent()
                } else {
                    val message = resources.getString(R.string.no_connection)
                    startActivity(intentFor<ErrorActivity>("error" to message))
                }
            } else {
                longToast("Usted ha utilizado el máximo de 10 solicitudes por mes. Espera el próximo mes para continuar utilizando la aplicación.")
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

            specificationIntent.putExtras(extras)
            startActivity(specificationIntent)
        }
    }

}
