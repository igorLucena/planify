package com.igorlucena.planify

import android.annotation.TargetApi
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.text.Html
import android.view.View
import android.widget.TextView
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
import org.json.JSONObject
import java.lang.Math.round
import java.net.URL
import java.time.ZoneId
import java.util.*

class SpecificationActivity : AppCompatActivity() {
    
    val API_VISION_KEY = "AIzaSyDK0sjfsIqaOEQyNygIjSgr3aIh9hVlpX4"
    val RESTRICTIONS_VISION_API = "restrictions"
    var mTitlePlane = ""
    var mRestrictions = 0
    lateinit var mAirplane: Airplane
    lateinit var mSharedPreferences: SharedPreferences
    lateinit var mDatabase: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_specification)

        mAirplane = Airplane()

        mDatabase = FirebaseDatabase.getInstance().getReference()

        mSharedPreferences = getSharedPreferences(RESTRICTIONS_VISION_API,
                Context.MODE_PRIVATE)
        mRestrictions = mSharedPreferences!!.getInt("9", 0)

        indeterminateBar.visibility = View.VISIBLE

        layout_specification.visibility = View.GONE

        val extra = intent.extras
        image_plane.setImageBitmap(extra!!.get("data") as Bitmap)

        catchVision()
    }

    @TargetApi(Build.VERSION_CODES.N)
    private fun catchVision() {
        val vision = Vision.Builder(NetHttpTransport(), AndroidJsonFactory(), null)
                .setVisionRequestInitializer(VisionRequestInitializer(API_VISION_KEY))
                .build()

        val inputStream = resources.openRawResource(R.raw.air4)
        val photoData = org.apache.commons.io.IOUtils.toByteArray(inputStream)
        inputStream.close()

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

            var airplane = false
            for (i in (0..(descriptions.size - 1))) {
                var description = descriptions.get(i).description
                if (description.equals("airplane")) {
                    airplane = true
                }

                if (airplane && description.startsWith("boeing ") ||
                        description.startsWith("airbus "))
                {
                    val descriptionList = description.split(Regex.fromLiteral(" "))
                    description = ""
                    for (str in descriptionList) {
                        description += str.capitalize()
                        description += ' '
                    }
                    mTitlePlane = description.trim()
                }
            }

            uiThread {
                if (!airplane) {
                    val message = resources.getString(R.string.no_exists_airplane)
                    startActivity(intentFor<ErrorActivity>("error" to message))
                    finish()
                } else if (mTitlePlane.length == 0) {
                    val message = resources.getString(R.string.no_specification)
                    startActivity(intentFor<ErrorActivity>("error" to message))
                    finish()
                } else {
                    title_plane.text = mTitlePlane

                    val airplaneReference = mDatabase.child("airplanes")

                    val airplaneListener = object : ValueEventListener {
                        override fun onDataChange(p0: DataSnapshot) {
                            val air = p0.children.mapNotNull {
                                it.getValue(Airplane::class.java)
                            }

                            for (airplane in air) {
                                if (airplane.model == mTitlePlane) {
                                    mAirplane = airplane
                                    break
                                }
                            }

                            val model = mAirplane.model
                            val description = mAirplane.description
                            val maxSpeed = mAirplane.maxSpeed
                            val spectrum = mAirplane.spectrum
                            val firstFlight = mAirplane.firstFlight
                            val length = mAirplane.length
                            val wingspan = mAirplane.wingspan
                            val cruisingSpeed = mAirplane.cruisingSpeed

                            title_plane.text = model
                            description_txt.text = description
                            max_speed_txt.text = ": $maxSpeed"
                            spectrum_txt.text = ": $spectrum"
                            first_flight_txt.text = ": $firstFlight"
                            length_txt.text = ": $length"
                            wingspan_txt.text = ": $wingspan"
                            cruising_speed_txt.text = ": $cruisingSpeed"

                            indeterminateBar.visibility = View.GONE
                            layout_specification.visibility = View.VISIBLE

                        }

                        override fun onCancelled(p0: DatabaseError) {
                            println("loadPost:onCancelled ${p0.toException()}")
                        }

                    }

                    airplaneReference.addValueEventListener(airplaneListener)
                }
            }
        }
    }
}
