package com.dngwjy.datasetcollector.ui

import com.dngwjy.datasetcollector.data.Point

interface MainView {
    fun onLoading()
    fun result(data:List<Point>)
}