package com.dngwjy.datasetcollector.ui

import com.dngwjy.datasetcollector.data.Point

interface MainView {
    /**
     * Callback method invoked when the loading process starts.
     * Implement this method to perform UI changes or show loading indicators
     * when data is being fetched or processed.
     */
    fun onLoading()
    /**
     * Callback method invoked when the result data is available.
     *
     * @param data The list of Point objects representing the result data.
     * Implement this method to handle the processed data and update the UI accordingly.
     */
    fun result(data:List<Point>)
}