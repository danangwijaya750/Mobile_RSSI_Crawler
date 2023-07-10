package com.dngwjy.datasetcollector

import com.dngwjy.datasetcollector.data.Point

interface MainView {
    fun onLoading()
    fun result(data:List<Point>)
}