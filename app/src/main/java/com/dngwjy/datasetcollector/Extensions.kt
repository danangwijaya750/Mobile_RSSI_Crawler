package com.dngwjy.datasetcollector

import android.util.Log
import android.view.View

inline fun<reified T>T.logE(msg:String)=msg.let {
    Log.e(T::class.java.simpleName,it)
}
fun View.toGone(){
    this.visibility=View.GONE
}
fun View.toVisible(){
    this.visibility=View.VISIBLE
}