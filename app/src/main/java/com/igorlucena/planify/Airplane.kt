package com.igorlucena.planify

import android.os.Parcel
import android.os.Parcelable
import java.io.Serializable

data class Airplane(val model: String = "", val description: String = "", val maxSpeed: String = "",
                     val spectrum: String = "", val firstFlight: String = "", val length: String = "",
                     val wingspan: String = "", val cruisingSpeed: String = "",
                     val absoluteCeiling: String = "", val automatic: String = "") : Serializable