package com.dngwjy.datasetcollector.data

/**
 * Represents a Data object containing various fields received from the floor plan backend API response.
 *
 * @param bottomRight The list of two Double values representing the bottom-right coordinates of a region.
 * @param center The Center object representing the center coordinates of a region.
 * @param id The ID associated with the data.
 * @param image The URL or path to an image associated with the data.
 * @param name The name associated with the data.
 * @param points The list of Point objects containing latitude and longitude coordinates of points.
 * @param rotate The rotation angle associated with the data.
 */
data class Response(
    var `data`: Data = Data()
)

/**
 * Represents a Center object containing latitude and longitude coordinates of a center point.
 *
 * @param lat The latitude coordinate of the center point.
 * @param lng The longitude coordinate of the center point.
 */
data class Data(
    var bottomRight: List<Double> = listOf(),
    var center: Center = Center(),
    var id: Int = 0,
    var image: String = "",
    var name: String = "",
    var points: List<Point> = listOf(),
    var rotate: Double = 0.0
)

/**
 * Represents a Center object containing latitude and longitude coordinates of a center point.
 *
 * @param lat The latitude coordinate of the center point.
 * @param lng The longitude coordinate of the center point.
 */
data class Center(
    var lat: Double = 0.0,
    var lng: Double = 0.0
)

/**
 * Represents a Point object containing latitude and longitude coordinates.
 *
 * @param lat The latitude coordinate of the point.
 * @param lng The longitude coordinate of the point.
 */
data class Point(
    var lat: Double = 0.0,
    var lng: Double = 0.0
)