package com.dngwjy.datasetcollector.ui.main

import com.dngwjy.datasetcollector.data.DataSet
import com.dngwjy.datasetcollector.data.Response
import com.dngwjy.datasetcollector.logE
import com.dngwjy.datasetcollector.util.RequestDataBuilder
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.extensions.jsonBody
import com.google.gson.Gson
import kotlinx.coroutines.*

/**
 * Presenter class responsible for handling business logic and communication between the view and the backend API.
 *
 * @param mainView The MainView interface representing the view that this presenter interacts with.
 */
class MainPresenter(private val mainView: MainView) {

    /**
     * Fetches points data from the backend API based on the provided ID.
     *
     * @param id The ID parameter used to fetch points data.
     */
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

    /**
     * Sends crawled data to the backend API.
     *
     * @param data The list of DataSet objects containing the crawled data.
     * @param androidVersion The version of Android OS used in the crawled data.
     */
     fun sendCrawledData(data: MutableList<DataSet>,androidVersion:String){
        val scope = CoroutineScope(Dispatchers.Main)
         val dataRequest= RequestDataBuilder.buildSendCrawledData(data,androidVersion)
        var res =""
        scope.launch {
            Fuel.post("http://140.118.121.81:8080/api/crawling/").jsonBody(dataRequest.toJson()).response{
                a,b, result->
                val(bytes,error)=result
                if(error!=null){
                    logE(error.message.toString())
                    mainView.resultUpload(false,error.message!!)
                }else{
                    bytes?.let { String(it) }?.let { logE(it) }
                    mainView.resultUpload(true,"")
                }
            }
        }
    }

    /**
     * Stores handset data to the backend API.
     *
     * @param data The handset data to be stored in JSON format.
     */
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