package com.dngwjy.datasetcollector

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds

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
fun bitmapDescriptorFromVector(context: Context, vectorResId: Int): BitmapDescriptor? {
    return ContextCompat.getDrawable(context, vectorResId)?.run {
        setBounds(0, 0, (intrinsicWidth), (intrinsicHeight))
        val bitmap = Bitmap.createBitmap((intrinsicWidth), (intrinsicHeight), Bitmap.Config.ARGB_8888)
        draw(Canvas(bitmap))
        BitmapDescriptorFactory.fromBitmap(bitmap)
    }
}
fun LatLng.getBounds(w:Float,h:Float):LatLngBounds{
    val southWest = LatLng(this.latitude, this.longitude - (w / 111111))
    val northEast = LatLng(this.latitude + (h / 111111), this.longitude)
    return LatLngBounds(southWest,northEast)
}