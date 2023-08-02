package com.dngwjy.datasetcollector.util

import com.dngwjy.datasetcollector.data.*

/**
 * Singleton object responsible for building CrawledRequest data from a list of DataSets and OS version.
 */
object RequestDataBuilder {
    /**
     * Builds and returns a CrawledRequest object using the provided list of DataSets and OS version.
     *
     * @param data The list of DataSet objects containing WiFi data.
     * @param versionOs The version of the operating system.
     * @return A CrawledRequest object containing the crawled access points and handset information.
     */
    fun buildSendCrawledData(data:List<DataSet>,versionOs:String):CrawledRequest{
        // Initialize necessary variables
        val apSets = mutableSetOf<String>()
        val crawledAp= mutableListOf<AccessPoint>()

        // Extract unique WiFi MAC addresses from the list of DataSets
        data.forEach { 
            it.wifis.forEach { wifi->
                apSets.add(wifi.mac)
            }
        }

        // Process each unique WiFi MAC address to build AccessPoint objects
        apSets.forEach {
            val rssiList = mutableListOf<DataRssi>()
            var ssid=""
            data.forEach { dataSet ->  
                dataSet.wifis.forEach { wifi->
                    if(wifi.mac == it){
                        rssiList.add(DataRssi(wifi.rssi,dataSet.time_stamp))
                        ssid=wifi.ssid
                    }
                }
            }

            // Create an AccessPoint object and add it to the crawledAp list
            crawledAp.add(AccessPoint(it,"",rssiList,ssid))
        }
        // Create and return the final CrawledRequest object
        return CrawledRequest(crawledAp, Handset(id = "64b2ca4cde8240904fe653b2",versionOs,"Android"),
            Location("64c9e0f78969d04b50e0914c",1,data[0].latitude,data[0].longitude)
        )
        
    }
}