package com.igorlucena.planify

import android.graphics.Bitmap
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.igorlucena.planify.R.id.image_plane
import kotlinx.android.synthetic.main.activity_especification.*

class EspecificationActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_especification)

        val extra = intent.extras
        image_plane.setImageBitmap(extra!!.get("data") as Bitmap)
    }
}
