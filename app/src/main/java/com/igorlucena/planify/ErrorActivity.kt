package com.igorlucena.planify

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_error.*
import org.jetbrains.anko.longToast

class ErrorActivity : AppCompatActivity() {

    val RESTRICTIONS_VISION_API = "restrictions"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_error)

        val extra = intent.extras
        error_txt.text = (extra.get("error")).toString()

        val restrictions = getSharedPreferences(RESTRICTIONS_VISION_API, Context.MODE_PRIVATE)
                .getInt("9", 0)

        longToast("Ha utilizado $restrictions de 10 solicitudes en la aplicación.")

        try_again_button.setOnClickListener {
            finish()
        }
    }
}
