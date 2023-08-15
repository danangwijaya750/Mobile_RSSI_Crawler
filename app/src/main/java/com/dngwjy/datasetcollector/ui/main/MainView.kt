package com.dngwjy.datasetcollector.ui.main

import com.dngwjy.datasetcollector.data.Point

/**
 * The MainView interface represents the view contract for the main activity.
 * It defines callback methods to communicate with the presenter and update the UI.
 */
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
    fun result(data: List<Point>)

    /**
     * Callback method invoked when the result of the data upload operation is available.
     *
     * @param success A boolean indicating whether the data upload was successful or not.
     * @param msg The message associated with the result (e.g., success message or error message).
     * Implement this method to handle the result of the data upload operation and update the UI accordingly.
     */
    fun resultUpload(success: Boolean, msg: String?)
}