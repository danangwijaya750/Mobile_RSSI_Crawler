package com.dngwjy.datasetcollector

import android.content.Context
import android.util.Log
import android.view.View
import android.widget.Toast

inline fun<reified T>T.logE(msg:String)=msg.let {
    Log.e(T::class.java.simpleName,it)
}
fun View.toGone(){
    this.visibility=View.GONE
}
fun View.toVisible(){
    this.visibility=View.VISIBLE
}
fun Context.toast(msg: String){
    Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
}