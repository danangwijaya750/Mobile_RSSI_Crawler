package com.dngwjy.datasetcollector.data

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName

/**
 * Represents a CrawledRequest object used for sending crawled data to the backend API.
 *
 * @param access_points The list of AccessPoint objects containing information about access points.
 * @param handset The Handset object representing the handset information associated with the crawled data.
 * @param location The Location object representing the geographical location associated with the crawled data.
 */
data class CrawledRequest(
    var access_points: List<AccessPoint> = listOf(),
    var handset: Handset = Handset(),
    @SerializedName("location")
    var location: Location = Location()
) {
    /**
     * Converts the CrawledRequest object to its JSON representation.
     *
     * @return The JSON representation of the CrawledRequest object.
     */
    fun toJson(): String {
        val gson = Gson()
        return gson.toJson(this)
    }
}

/**
 * Represents an AccessPoint object containing information about a WiFi access point.
 *
 * @param bssid The BSSID (Basic Service Set Identifier) of the WiFi access point.
 * @param channel The channel of the WiFi access point.
 * @param data The list of DataRssi objects containing RSSI (Received Signal Strength Indicator) data associated with the access point.
 * @param ssid The SSID (Service Set Identifier) of the WiFi access point.
 */
data class AccessPoint(
    var bssid: String = "",
    var channel: String = "",
    var data: List<DataRssi> = listOf(),
    var ssid: String = ""
)

/**
 * Represents a Handset object containing information about the handset.
 *
 * @param id The ID of the handset.
 * @param os The operating system of the handset.
 * @param type The type of the handset.
 */
data class Handset(
    var id: String = "",
    var os: String = "",
    var type: String = ""
)

/**
 * Represents a Location object containing geographical location information.
 *
 * @param building The building name associated with the location.
 * @param floor The floor number associated with the location.
 * @param latitude The latitude coordinate of the location.
 * @param longitude The longitude coordinate of the location.
 */
data class Location(
    var building: String = "",
    var floor: Int = 0,
    var latitude: Double = 0.0,
    var longitude: Double = 0.0
)

/**
 * Represents a DataRssi object containing RSSI (Received Signal Strength Indicator) data.
 *
 * @param rssi The RSSI value associated with the data.
 * @param timeString The time string associated with the data.
 */
data class DataRssi(
    var rssi: String = "",
    var timeString: String = ""
)