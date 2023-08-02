package com.dngwjy.datasetcollector.data

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName


data class CrawledRequest(
    var access_points: List<AccessPoint> = listOf(),
    var handset: Handset = Handset(),
    @SerializedName("location")
    var location: Location = Location()
){
    fun toJson(): String {
        val gson = Gson()
        return gson.toJson(this)
    }
}

data class AccessPoint(
    var bssid: String = "",
    var channel: String = "",
    var `data`: List<DataRssi> = listOf(),
    var ssid: String = ""
)

data class Handset(
    var id:String="",
    var os: String = "",
    var type: String = ""
)

data class Location(
    var building: String = "",
    var floor: Int = 0,
    var latitude: Double = 0.0,
    var longitude: Double = 0.0
)

data class DataRssi(
    var rssi: String = "",
    var timeString: String = ""
)