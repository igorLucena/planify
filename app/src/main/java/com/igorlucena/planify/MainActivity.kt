package com.igorlucena.planify

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Build
import android.os.Bundle
import android.os.ProxyFileDescriptorCallback
import android.provider.MediaStore
import android.support.annotation.RequiresApi
import android.support.v7.app.AppCompatActivity
import android.text.Html
import android.text.Html.FROM_HTML_OPTION_USE_CSS_COLORS
import android.text.SpannableStringBuilder
import android.util.Log
import android.widget.Button
import org.jetbrains.anko.*
import org.json.JSONObject
import java.net.URL

class MainActivity : AppCompatActivity() {

    val REQUEST_IMAGE_CAPTURE = 1
    var specificationIntent = Intent()
    val RESTRICTIONS_VISION_API = "restrictions"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val photoButton = findViewById<Button>(R.id.photo_button)

        specificationIntent = Intent(this, SpecificationActivity::class.java)

        val sharedPreferences = getSharedPreferences(RESTRICTIONS_VISION_API, Context.MODE_PRIVATE)

        photoButton.setOnClickListener {
            val restrictions = sharedPreferences.getInt("9", 0)
            if (restrictions < 10) {
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
