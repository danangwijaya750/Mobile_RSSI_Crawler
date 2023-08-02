package com.dngwjy.datasetcollector.data

/**
 * Represents data for a Bluetooth Low Energy (BLE) device.
 *
 * @param mac The MAC address of the BLE device.
 * @param device The name of the BLE device (optional).
 * @param rssi The Received Signal Strength Indicator (RSSI) value associated with the BLE device.
 */
data class BleData(
    var mac:String,
    var device:String?,
    var rssi:String,
)

/**
 * Represents data for a Wi-Fi access point.
 *
 * @param mac The MAC address of the Wi-Fi access point.
 * @param ssid The Service Set Identifier (SSID) of the Wi-Fi access point.
 * @param rssi The Received Signal Strength Indicator (RSSI) value associated with the Wi-Fi access point.
 */
data class WifiData(
    var mac: String,
    var ssid:String,
    var rssi: String
)

/**
 * Represents a dataset containing various types of data at a specific time and location.
 *
 * @param time_stamp The timestamp associated with the dataset.
 * @param latitude The latitude coordinate of the location where the dataset was collected.
 * @param longitude The longitude coordinate of the location where the dataset was collected.
 * @param bles The list of BLE data associated with the dataset.
 * @param wifis The list of Wi-Fi data associated with the dataset.
 * @param geomagnetic The list of geomagnetic data associated with the dataset.
 * @param accel The list of accelerometer data associated with the dataset.
 * @param gyro The list of gyroscope data associated with the dataset.
 */
data class DataSet (
    var time_stamp:String,
    var latitude:Double,
    var longitude:Double,
    var bles:List<BleData>,
    var wifis:List<WifiData>,
    var geomagnetic:List<Float>,
    var accel:List<Float>,
    var gyro:List<Float>
    )