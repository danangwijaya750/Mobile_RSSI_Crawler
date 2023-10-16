package com.dngwjy.datasetcollector.util

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

/**
 * Logs the given message at the ERROR level using the class name of the reified type as the tag.
 *
 * @param msg The message to be logged.
 */
inline fun<reified T>T.logE(msg:String)=msg.let {
    Log.e(T::class.java.simpleName,it)
}
/**
 * Sets the visibility of the View to GONE, making it invisible and not taking any space in the layout.
 */
fun View.toGone(){
    this.visibility=View.GONE
}
/**
 * Sets the visibility of the View to VISIBLE, making it visible within the layout.
 */
fun View.toVisible(){
    this.visibility=View.VISIBLE
}
/**
 * Displays a toast message with the given text.
 *
 * @param msg The message to be displayed in the toast.
 */
fun Context.toast(msg: String){
    Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
}
/**
 * Converts a vector drawable resource to a BitmapDescriptor, suitable for use with Google Maps markers.
 *
 * @param context The context used to access resources.
 * @param vectorResId The resource ID of the vector drawable to be converted.
 * @return A BitmapDescriptor representing the converted vector drawable, or null if conversion fails.
 */
fun bitmapDescriptorFromVector(context: Context, vectorResId: Int): BitmapDescriptor? {
    return ContextCompat.getDrawable(context, vectorResId)?.run {
        setBounds(0, 0, (intrinsicWidth), (intrinsicHeight))
        val bitmap = Bitmap.createBitmap((intrinsicWidth), (intrinsicHeight), Bitmap.Config.ARGB_8888)
        draw(Canvas(bitmap))
        BitmapDescriptorFactory.fromBitmap(bitmap)
    }
}
/**
 * Calculates the LatLngBounds based on the current LatLng and given width and height offsets.
 *
 * @param w The width offset in meters.
 * @param h The height offset in meters.
 * @return A LatLngBounds object representing the bounds of the region defined by the current LatLng and offsets.
 */
fun LatLng.getBounds(w:Float,h:Float):LatLngBounds{
    val southWest = LatLng(this.latitude, this.longitude - (w / 111111))
    val northEast = LatLng(this.latitude + (h / 111111), this.longitude)
    return LatLngBounds(southWest,northEast)
}