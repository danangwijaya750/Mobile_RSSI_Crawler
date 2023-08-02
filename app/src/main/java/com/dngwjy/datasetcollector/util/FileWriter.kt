package com.dngwjy.datasetcollector.util

import android.content.Context
import com.dngwjy.datasetcollector.ui.MainActivity
import com.dngwjy.datasetcollector.data.DataSet
import com.dngwjy.datasetcollector.logE
import java.io.File

/**
 * Class responsible for writing data to a file in the external files directory of the application.
 *
 * @param context The Android Context used to access the external files directory.
 */
class FileWriter(private val context: Context) {

    /**
     * Writes the provided data to a file with the given title in the external files directory.
     *
     * @param data The list of DataSet objects containing the data to be written to the file.
     * @param title The title of the file to be created.
     * @return The absolute path of the created file.
     */
    fun writeToFile(data: List<DataSet>, title: String): String {
        // Get the URI for the external files directory
        val uri = File(context.getExternalFilesDir(null).toString() + MainActivity.DIRECTORY)

        // Create the directory if it doesn't exist
        if (!uri.exists()) {
            uri.mkdirs()
        }

        // Create the file in the specified directory
        val exportedFile = File(uri.toString() + File.separator.toString() + title)

        // Write data to the file
        exportedFile.printWriter().use { out ->
            data.forEach { dataSet ->
                var bles = ""
                var gyros = ""
                var geos = ""
                var accels = ""
                var wifis = ""

                // Process BLE data
                dataSet.bles.forEach { bleData ->
                    bles = "$bles;$bleData"
                }
                logE(bles)

                // Process WiFi data
                dataSet.wifis.forEach { wifiData ->
                    wifis = "$wifis;$wifiData"
                }
                logE(wifis)

                // Process accelerometer data
                dataSet.accel.forEach { accel ->
                    accels = "$accels;$accel"
                }

                // Process gyroscope data
                dataSet.gyro.forEach { gyro ->
                    gyros = "$gyros;$gyro"
                }

                // Process geomagnetic data
                dataSet.geomagnetic.forEach { geo ->
                    geos = "$geos;$geo"
                }

                // Write the data to the file
                out.println("${dataSet.time_stamp};${dataSet.latitude};${dataSet.longitude}${bles}${wifis}${accels}${geos}${gyros}")
            }
        }

        // Return the absolute path of the created file
        return exportedFile.absolutePath
    }
}