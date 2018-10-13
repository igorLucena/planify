package com.igorlucena.planify

import android.content.Context
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_error.*
import org.jetbrains.anko.longToast
import java.time.ZoneId
import java.util.*

class ErrorActivity : AppCompatActivity() {

    val RESTRICTIONS_VISION_API = "restrictions"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_error)

        val extra = intent.extras
        error_txt.text = (extra.get("error")).toString()

        val date = Date()
        val month = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
            localDate.monthValue
        } else {
            val cal = Calendar.getInstance()
            cal.time = date
            cal.get(Calendar.MONTH)
        }

        val restrictions = getSharedPreferences(RESTRICTIONS_VISION_API, Context.MODE_PRIVATE)
                .getInt("$month", 0)

        longToast("Ha utilizado $restrictions de 30 solicitudes en la aplicación.")

        try_again_button.setOnClickListener {
            finish()
        }
    }
}
