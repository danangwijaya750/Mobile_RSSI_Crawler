package com.dngwjy.datasetcollector.data

import android.content.Context

/**
 * Helper class to manage shared preferences in the Android app.
 *
 * @param context The context used to access shared preferences.
 */
class SharedPef(private val context: Context) {
    companion object {
        private const val PREF_NAME = "com.dngwjy.datasetcollector"
        private const val DEFINED_POINTS = "defined.points"
        private const val CRAWLED_POINTS_IDB1 = "crawled.points.idb1"
        private const val CRAWLED_POINTS_IDB2 = "crawled.points.idb1"
        private const val CRAWLED_POINTS_IDB3 = "crawled.points.idb1"
        private const val CRAWLED_POINTS_EE1 = "crawled.points.idb1"
        private const val CRAWLED_POINTS_EE6 = "crawled.points.idb1"
        private const val CRAWLED_POINTS_EE7 = "crawled.points.idb1"
        private const val CRAWLED_POINTS_EE8 = "crawled.points.idb1"
    }

    private val pref = context.getSharedPreferences(PREF_NAME, 0)
    // yes it's dirty codes but this is the easiest and quick way hehe, u can optimize for this features with RoomDB
    var definedPoints: String
        get() = pref.getString(DEFINED_POINTS, "").toString()
        set(value) = pref.edit().putString(DEFINED_POINTS, value).apply()
    var crawledPointsIdb1: String
        get() = pref.getString(CRAWLED_POINTS_IDB1, "").toString()
        set(value) = pref.edit().putString(CRAWLED_POINTS_IDB1, value).apply()
    var crawledPointsIdb2: String
        get() = pref.getString(CRAWLED_POINTS_IDB2, "").toString()
        set(value) = pref.edit().putString(CRAWLED_POINTS_IDB2, value).apply()
    var crawledPointsIdb3: String
        get() = pref.getString(CRAWLED_POINTS_IDB3, "").toString()
        set(value) = pref.edit().putString(CRAWLED_POINTS_IDB3, value).apply()
    var crawledPointsEE1: String
        get() = pref.getString(CRAWLED_POINTS_EE1, "").toString()
        set(value) = pref.edit().putString(CRAWLED_POINTS_EE1, value).apply()
    var crawledPointsIEE6: String
        get() = pref.getString(CRAWLED_POINTS_EE6, "").toString()
        set(value) = pref.edit().putString(CRAWLED_POINTS_EE6, value).apply()
    var crawledPointsIEE7: String
        get() = pref.getString(CRAWLED_POINTS_EE7, "").toString()
        set(value) = pref.edit().putString(CRAWLED_POINTS_EE7, value).apply()
    var crawledPointsIEE8: String
        get() = pref.getString(CRAWLED_POINTS_EE8, "").toString()
        set(value) = pref.edit().putString(CRAWLED_POINTS_EE8, value).apply()

}