package com.dngwjy.datasetcollector.data

data class Response(
    var `data`: Data = Data()
)

data class Data(
    var bottomRight: List<Double> = listOf(),
    var center: Center = Center(),
    var id: Int = 0,
    var image: String = "",
    var name: String = "",
    var points: List<Point> = listOf(),
    var rotate: Double = 0.0
)

data class Center(
    var lat: Double = 0.0,
    var lng: Double = 0.0
)

data class Point(
    var lat: Double = 0.0,
    var lng: Double = 0.0
)