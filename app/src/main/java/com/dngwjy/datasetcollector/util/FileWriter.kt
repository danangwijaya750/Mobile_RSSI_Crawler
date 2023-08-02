package com.dngwjy.datasetcollector.util

import android.content.Context
import com.dngwjy.datasetcollector.ui.MainActivity
import com.dngwjy.datasetcollector.data.DataSet
import com.dngwjy.datasetcollector.logE
import java.io.File

class FileWriter(private val context: Context) {

    fun writeToFile(data:List<DataSet>, title:String):String{
        val uri = File(context.getExternalFilesDir(null).toString() + MainActivity.DIRECTORY)
        if (!uri.exists()) {
            uri.mkdirs()
        }
        val exportedFile = File(uri.toString() + File.separator.toString() + title)
        exportedFile.printWriter().use { out ->
            data.forEach {
                var bles=""
                var gyros=""
                var geos=""
                var accels=""
                var wifis=""
                it.bles.forEach { bleData ->
                    bles="$bles;$bleData"
                }
                logE(bles)
                it.wifis.forEach { wifiData->
                    wifis="$wifis;$wifiData"
                }
                logE(wifis)
                it.accel.forEach { accel->
                    accels="$accels;$accel"
                }
                it.gyro.forEach { gyro->
                    gyros="$gyros;$gyro"
                }
                it.geomagnetic.forEach { geo->
                    geos="$geos;$geo"
                }
                out.println("${it.time_stamp};${it.latitude};${it.longitude}${bles}${wifis}${accels}${geos}${gyros}")
            }
        }
        return exportedFile.absolutePath
    }
}