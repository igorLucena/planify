package com.igorlucena.planify

import android.annotation.TargetApi
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.google.api.client.extensions.android.json.AndroidJsonFactory
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.services.vision.v1.Vision
import com.google.api.services.vision.v1.VisionRequestInitializer
import com.google.api.services.vision.v1.model.*
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_specification.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.toast
import org.jetbrains.anko.uiThread
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.time.ZoneId
import java.util.*

class SpecificationActivity : AppCompatActivity() {

    val RESTRICTIONS_VISION_API = "restrictions"
    var mModel = ""
    var mDescription = ""
    var mMaxSpeed = ""
    var mSpectrum = ""
    var mFirstFlight = ""
    var mLength = ""
    var mWingspan = ""
    var mCruisingSpeed = ""
    var mRestrictions = 0
    var MAX_RESTRICTIONS = 0
    lateinit var mAirplane: Airplane
    lateinit var mSharedPreferences: SharedPreferences
    var mMonth = 0

    companion object {
        private val MODEL = "MODEL"
        private val DESCRIPTION = "DESCRIPTION"
        private val MAX_SPEED = "MAX_SPEED"
        private val SPECTRUM = "SPECTRUM"
        private val FIRST_FLIGHT ="FIRST_FLIGHT"
        private val LENGTH = "LENGTH"
        private val WINGSPAN = "WINGSPAN"
        private val CRUISING_SPEED = "CRUISING_SPEED"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_specification)

        val extra = intent.extras

        if (savedInstanceState != null) {
            image_plane.setImageBitmap(extra!!.get("data") as Bitmap)

            mModel = savedInstanceState.getString(MODEL)
            mDescription = savedInstanceState.getString(DESCRIPTION)
            mMaxSpeed = savedInstanceState.getString(MAX_SPEED)
            mSpectrum = savedInstanceState.getString(SPECTRUM)
            mFirstFlight = savedInstanceState.getString(FIRST_FLIGHT)
            mLength = savedInstanceState.getString(LENGTH)
            mWingspan = savedInstanceState.getString(WINGSPAN)
            mCruisingSpeed = savedInstanceState.getString(CRUISING_SPEED)

            setTextActivity()
        } else {
            val date = Date()
            mMonth = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
                localDate.monthValue
            } else {
                val cal = Calendar.getInstance()
                cal.time = date
                cal.get(Calendar.MONTH)
            }

            mSharedPreferences = getSharedPreferences(RESTRICTIONS_VISION_API,
                    Context.MODE_PRIVATE)
            mRestrictions = mSharedPreferences!!.getInt("$mMonth", 0)

            image_plane.setImageBitmap(extra!!.get("data") as Bitmap)

            MAX_RESTRICTIONS = extra.get("max_restrictions") as Int

            mAirplane = extra.getSerializable("airplane") as Airplane

            setSpecifications()
        }
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)

        if (outState != null) {
            outState.putString(MODEL, mModel)
            outState.putString(DESCRIPTION, mDescription)
            outState.putString(MAX_SPEED, mMaxSpeed)
            outState.putString(SPECTRUM, mSpectrum)
            outState.putString(FIRST_FLIGHT, mFirstFlight)
            outState.putString(LENGTH, mLength)
            outState.putString(WINGSPAN, mWingspan)
            outState.putString(CRUISING_SPEED, mCruisingSpeed)
        }
    }

    @TargetApi(Build.VERSION_CODES.N)
    private fun setSpecifications() {
        mRestrictions++
        val editor = mSharedPreferences!!.edit()
        editor.putInt(mMonth.toString(), mRestrictions)
        editor.commit()

        mModel = mAirplane.model
        mDescription = mAirplane.description
        mMaxSpeed = mAirplane.maxSpeed
        mSpectrum = mAirplane.spectrum
        mFirstFlight = mAirplane.firstFlight
        mLength = mAirplane.length
        mWingspan = mAirplane.wingspan
        mCruisingSpeed = mAirplane.cruisingSpeed

        setTextActivity()
    }

    private fun setTextActivity() {
        title_plane.text = mModel
        description_txt.text = mDescription
        max_speed_txt.text = ": $mMaxSpeed"
        spectrum_txt.text = ": $mSpectrum"
        first_flight_txt.text = ": $mFirstFlight"
        length_txt.text = ": $mLength"
        wingspan_txt.text = ": $mWingspan"
        cruising_speed_txt.text = ": $mCruisingSpeed"
    }
}



