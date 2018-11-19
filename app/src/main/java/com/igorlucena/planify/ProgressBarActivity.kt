package com.igorlucena.planify

import android.annotation.TargetApi
import android.graphics.Bitmap
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.google.api.client.extensions.android.json.AndroidJsonFactory
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.services.vision.v1.Vision
import com.google.api.services.vision.v1.VisionRequestInitializer
import com.google.api.services.vision.v1.model.AnnotateImageRequest
import com.google.api.services.vision.v1.model.BatchAnnotateImagesRequest
import com.google.api.services.vision.v1.model.Feature
import com.google.api.services.vision.v1.model.Image
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_progress_bar.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.uiThread
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.Serializable
import java.util.*

class ProgressBarActivity : AppCompatActivity() {

    val API_VISION_KEY = "AIzaSyAQ9Qjfu59XjQpANCXPV-sbfJ0ZKcgO_kY"
    var MAX_RESTRICTIONS = 0
    lateinit var mBitmap: Bitmap
    var mIsAirplane = false
    var mTitlePlane = ""
    var mAirplane = Airplane()
    lateinit var mDatabase: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_progress_bar)

        mDatabase = FirebaseDatabase.getInstance().getReference()

        val extra = intent.extras

        MAX_RESTRICTIONS = extra.get("max_restrictions") as Int

        mBitmap = extra.get("data") as Bitmap
        val bos = ByteArrayOutputStream()
        mBitmap.compress(Bitmap.CompressFormat.PNG, 0, bos)
        val bitmapdata = bos.toByteArray()
        val bs = ByteArrayInputStream(bitmapdata)

        catchVision(bs)
    }

    @TargetApi(Build.VERSION_CODES.N)
    private fun catchVision(bs: ByteArrayInputStream) {

        determinateBar.progress = 0

        val vision = Vision.Builder(NetHttpTransport(), AndroidJsonFactory(), null)
                .setVisionRequestInitializer(VisionRequestInitializer(API_VISION_KEY))
                .build()

        val inputStream = resources.openRawResource(R.raw.air37)
        val photoData = org.apache.commons.io.IOUtils.toByteArray(inputStream)
        inputStream.close()

        //val photoData = org.apache.commons.io.IOUtils.toByteArray(bs)
        //bs.close()

        determinateBar.progress = 10

        val inputImage = Image()
        inputImage.encodeContent(photoData)

        val desiredFeature1 = Feature()
        desiredFeature1.setType("LABEL_DETECTION")
        val desiredFeature2 = Feature()
        desiredFeature2.setType("WEB_DETECTION")

        determinateBar.progress = 25

        val request = AnnotateImageRequest()
        request.setImage(inputImage)
        request.setFeatures(Arrays.asList(desiredFeature1, desiredFeature2))

        val batchRequest = BatchAnnotateImagesRequest()
        batchRequest.setRequests(Arrays.asList(request))

        doAsync {
            val batchResponse = vision.images().annotate(batchRequest).execute()

            determinateBar.progress = 75
            uiThread {
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
                            startActivity(intentFor<SpecificationActivity>("airplane" to mAirplane as Serializable, "data" to mBitmap, "max_restrictions" to MAX_RESTRICTIONS))
                            finish()
                        }

                    }

                    override fun onCancelled(p0: DatabaseError) {
                        println("loadPost:onCancelled ${p0.toException()}")
                    }
                }

                airplaneReference.addValueEventListener(airplaneListener)

                determinateBar.progress = 95
            }
        }
    }
}
