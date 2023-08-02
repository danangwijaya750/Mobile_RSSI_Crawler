package com.dngwjy.datasetcollector.util

import com.dngwjy.datasetcollector.data.*

object RequestDataBuilder {
    fun buildSendCrawledData(data:List<DataSet>,versionOs:String):CrawledRequest{
        val apSets = mutableSetOf<String>()
        val crawledAp= mutableListOf<AccessPoint>()
        data.forEach { 
            it.wifis.forEach { wifi->
                apSets.add(wifi.mac)
            }
        }
        
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
            crawledAp.add(AccessPoint(it,"",rssiList,ssid))
        }
        return CrawledRequest(crawledAp, Handset(id = "64b2ca4cde8240904fe653b2",versionOs,"Android"),
            Location("64c9e0f78969d04b50e0914c",1,data[0].latitude,data[0].longitude)
        )
        
    }
}