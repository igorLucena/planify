package com.igorlucena.planify

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_error.*

class ErrorActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_error)

        val extra = intent.extras
        error_txt.text = (extra.get("error")).toString()

        try_again_button.setOnClickListener {
            finish()
        }
    }
}
