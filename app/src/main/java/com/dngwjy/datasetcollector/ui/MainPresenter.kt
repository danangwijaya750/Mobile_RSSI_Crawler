package com.dngwjy.datasetcollector.ui

import com.dngwjy.datasetcollector.data.CrawledRequest
import com.dngwjy.datasetcollector.data.Response
import com.dngwjy.datasetcollector.logE
import com.github.kittinunf.fuel.Fuel
import com.google.gson.Gson
import kotlinx.coroutines.*


class MainPresenter(private val mainView: MainView) {

    fun getPoints(id:String){
        val scope = CoroutineScope(Dispatchers.Main)
        var res=""
        scope.launch {
            logE("http://dev.phototop.niwabi.my.id/points/$id")
            Fuel.get("http://dev.phototop.niwabi.my.id/points/$id").response{
                _, _, result ->
                val(bytes,error)=result
                if(bytes!=null){
                    println(String(bytes))
                    val gson= Gson().fromJson(String(bytes),Response::class.java)
                    println(gson.data.points.size)
                    mainView.result(gson.data.points)
                }
            }
        }
    }
    private fun sendCrawledData(data: CrawledRequest){
        val scope = CoroutineScope(Dispatchers.Main)
        var res =""
        scope.launch {
            logE("")
            Fuel.post("http://140.118.121.81:8080/api/crawling/").body(data.toJson()).response{
                _,_, result->

            }
        }
    }

    fun storeHandsetData(data :String){
        val scope= CoroutineScope(Dispatchers.Main)
        var res=""
        scope.launch {
            Fuel.post("http://140.118.121.81:8080/api/handsets/").body(data).response{
                _,_,result->

            }
        }
    }


}