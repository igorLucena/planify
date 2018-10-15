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

    val API_VISION_KEY = "AIzaSyDK0sjfsIqaOEQyNygIjSgr3aIh9hVlpX4"
    val RESTRICTIONS_VISION_API = "restrictions"
    var mTitlePlane = ""
    var mIsAirplane = false
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
    lateinit var mDatabase: DatabaseReference

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

        if (savedInstanceState != null) {
            val extra = intent.extras
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

            indeterminateBarLayout.visibility = View.GONE
            layout_specification.visibility = View.VISIBLE
        } else {
            val date = Date()
            val month = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
                localDate.monthValue
            } else {
                val cal = Calendar.getInstance()
                cal.time = date
                cal.get(Calendar.MONTH)
            }

            mAirplane = Airplane()

            mDatabase = FirebaseDatabase.getInstance().getReference()

            mSharedPreferences = getSharedPreferences(RESTRICTIONS_VISION_API,
                    Context.MODE_PRIVATE)
            mRestrictions = mSharedPreferences!!.getInt("$month", 0)

            val extra = intent.extras
            image_plane.setImageBitmap(extra!!.get("data") as Bitmap)

            MAX_RESTRICTIONS = extra.get("max_restrictions") as Int

            val bitmap = extra.get("data") as Bitmap
            val bos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 0, bos)
            val bitmapdata = bos.toByteArray()
            val bs = ByteArrayInputStream(bitmapdata)

            indeterminateBarLayout.visibility = View.VISIBLE
            layout_specification.visibility = View.GONE

            catchVision(bs)
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
    private fun catchVision(bs: ByteArrayInputStream) {
        val vision = Vision.Builder(NetHttpTransport(), AndroidJsonFactory(), null)
                .setVisionRequestInitializer(VisionRequestInitializer(API_VISION_KEY))
                .build()

        val inputStream = resources.openRawResource(R.raw.air17)
        val photoData = org.apache.commons.io.IOUtils.toByteArray(inputStream)
        inputStream.close()


        //val photoData = org.apache.commons.io.IOUtils.toByteArray(bs)
        //bs.close()

        val inputImage = Image()
        inputImage.encodeContent(photoData)

        val desiredFeature1 = Feature()
        desiredFeature1.setType("LABEL_DETECTION")
        val desiredFeature2 = Feature()
        desiredFeature2.setType("WEB_DETECTION")


        val request = AnnotateImageRequest()
        request.setImage(inputImage)
        request.setFeatures(Arrays.asList(desiredFeature1, desiredFeature2))

        val batchRequest = BatchAnnotateImagesRequest()
        batchRequest.setRequests(Arrays.asList(request))

        doAsync {
            val batchResponse = vision.images().annotate(batchRequest).execute()
            mRestrictions++
            val editor = mSharedPreferences!!.edit()
            val date = Date()
            val month = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
                localDate.monthValue
            } else {
                val cal = Calendar.getInstance()
                cal.time = date
                cal.get(Calendar.MONTH)
            }
            editor.putInt(month.toString(), mRestrictions)
            editor.commit()

            val descriptions = batchResponse.responses.get(0).labelAnnotations
            val models = batchResponse.responses.get(0).webDetection

            mTitlePlane = models.webEntities[0].description

            for (i in (0..(descriptions.size - 1))) {
                var description = descriptions.get(i).description
                if (description.equals("airplane")) {
                    mIsAirplane = true
                    break
                }
            }

            uiThread {
                if (!mIsAirplane) {
                    val message = resources.getString(R.string.no_exists_airplane)
                    startActivity(intentFor<ErrorActivity>("error" to message, "max_restrictions" to MAX_RESTRICTIONS))
                    finish()
                }

                val airplaneReference = mDatabase.child("airplanes")

                val airplaneListener = object : ValueEventListener {
                    override fun onDataChange(p0: DataSnapshot) {
                        val airplanes = p0.children.mapNotNull {
                            it.getValue(Airplane::class.java)
                        }

                        for (airplane in airplanes) {
                            if (mTitlePlane.contains(airplane.model) || airplane.model.contains(mTitlePlane)) {
                                mAirplane = airplane
                                break
                            }
                        }

                        if (mIsAirplane && mAirplane.model.length == 0) {
                            val message = resources.getString(R.string.no_specification)
                            startActivity(intentFor<ErrorActivity>("error" to message, "max_restrictions" to MAX_RESTRICTIONS))
                            finish()
                        } else {

                            mModel = mAirplane.model
                            mDescription = mAirplane.description
                            mMaxSpeed = mAirplane.maxSpeed
                            mSpectrum = mAirplane.spectrum
                            mFirstFlight = mAirplane.firstFlight
                            mLength = mAirplane.length
                            mWingspan = mAirplane.wingspan
                            mCruisingSpeed = mAirplane.cruisingSpeed

                            setTextActivity()

                            indeterminateBarLayout.visibility = View.GONE
                            layout_specification.visibility = View.VISIBLE

                            //indeterminateBarVisibility()

                            toast("Ha utilizado $mRestrictions de $MAX_RESTRICTIONS solicitudes en la aplicación.")
                        }

                    }

                    override fun onCancelled(p0: DatabaseError) {
                        println("loadPost:onCancelled ${p0.toException()}")
                    }

                }

                airplaneReference.addValueEventListener(airplaneListener)
            }
        }
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

    private fun indeterminateBarVisibility() {
        if (indeterminateBar.visibility == View.VISIBLE) {
            indeterminateBar.visibility = View.GONE
            layout_specification.visibility = View.VISIBLE
        } else {
            indeterminateBar.visibility = View.VISIBLE
            layout_specification.visibility = View.GONE
        }
    }
}



