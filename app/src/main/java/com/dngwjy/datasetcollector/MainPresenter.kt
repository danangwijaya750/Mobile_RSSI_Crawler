package com.dngwjy.datasetcollector

import androidx.lifecycle.ViewModel
import com.dngwjy.datasetcollector.data.Response
import com.github.kittinunf.fuel.Fuel
import com.google.gson.Gson
import kotlinx.coroutines.*
import java.net.URL
import kotlin.math.log


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
}