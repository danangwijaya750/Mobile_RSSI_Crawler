package com.dngwjy.datasetcollector

data class BleData(
    var mac:String,
    var rssi:String
)

data class DataSet (
    var time_stamp:String,
    var latitude:Double,
    var longitude:Double,
    var bles:List<BleData>,
    var geomagnetic:List<Float>,
    var accel:List<Float>,
    var gyro:List<Float>
    )