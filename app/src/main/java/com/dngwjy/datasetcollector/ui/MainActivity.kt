package com.dngwjy.datasetcollector.ui

import android.Manifest
import android.app.Dialog
import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.LocationManager
import android.net.wifi.WifiManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.clj.fastble.BleManager
import com.clj.fastble.callback.BleScanCallback
import com.clj.fastble.data.BleDevice
import com.clj.fastble.scan.BleScanRuleConfig
import com.dngwjy.datasetcollector.*
import com.dngwjy.datasetcollector.data.*
import com.dngwjy.datasetcollector.databinding.ActivityMainBinding
import com.dngwjy.datasetcollector.databinding.LayoutDialogBinding
import com.dngwjy.datasetcollector.util.FileWriter
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import java.util.*

/**
 * MainActivity class is the main activity for the Dataset Collector app.
 * This activity displays a Google Map and allows users to drop pins on the map to collect fingerprint data.
 */
class MainActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener,SensorEventListener,
    MainView {
    /**
     * Instance of the MainPresenter class responsible for handling business logic and data processing
     * for the main activity.
     */
    private lateinit var presenter: MainPresenter
    /**
     * View binding instance for the main activity layout, which provides direct access to the UI elements
     * in the layout XML file.
     */
    private lateinit var binding: ActivityMainBinding
    /**
     * A mutable list of Point objects representing the defined fingerprint points on the map.
     *
     * This list contains Point objects that represent the defined fingerprint points for the selected
     * building and floor. These points are used to display markers on the map and provide the location
     * data for the fingerprint points. The points can be modified to add or remove defined points.
     *
     * Note: Ensure that the `Point` class or data structure used here is defined and implemented correctly
     * to store latitude and longitude coordinates of the fingerprint points.
     */
    private val definedPoints= mutableListOf<Point>()
    /**
     * A string representing the crawled fingerprint points on the map.
     *
     * This property stores a string representing the crawled fingerprint points for the selected building
     * and floor. The crawled points are used to display markers on the map with different icons based on
     * whether they have been crawled or not. The format of the string should be "latitude,longitude" to
     * match the coordinates of the defined points.
     */
    private var crawledPoints= ""
    /**
     * A boolean indicating whether to display defined fingerprint points on the map.
     *
     * This property controls the visibility of defined fingerprint points on the map. If set to true,
     * defined points will be displayed as markers with custom icons. If set to false, the defined points
     * will not be shown on the map.
     */
    private var showPoints=true
    /**
     * An instance of the GoogleMap class representing the Google Map object.
     *
     * This property is used to interact with the Google Map fragment and perform operations such as
     * adding markers, overlays, camera movements, and other map-related functions.
     */
    private var mapView:GoogleMap?=null
    /**
     * An array of LatLng objects representing the South-East boundaries of buildings for different floors.
     * currently is still hardcoded, but u can improve it to dynamic data from backend
     *
     * The array stores coordinates representing the South-East corner of buildings on the map. It helps
     * in positioning the ground overlay of the floor plan image correctly for different buildings and
     * floors.
     *
     * Example: southEast[buildingIndex]
     *
     * Note: Ensure that the coordinates are accurate and correspond to the correct South-East corners
     * of the buildings on the map.
     */
    private val southEast=arrayOf(LatLng(25.01159698395817, 121.54119884517124),LatLng(-7.771120618305493, 110.3868760009918))
    /**
     * An instance of the Marker class representing the pin marker on the map.
     *
     * This property is used to keep track of the current marker representing the user's position or
     * selected point on the map.
     */
    private var pinMarker:Marker?=null
    /**
     * An instance of the GroundOverlayOptions class representing the floor plan overlay on the map.
     *
     * This property is used to add a ground overlay with the floor plan image to the map.
     */
    private lateinit var floorOverlay: GroundOverlayOptions
    /**
     * An instance of the Dialog class representing the custom dialog for user interactions.
     *
     * This property is used to create and display custom dialogs for various user interactions or alerts.
     */
    private lateinit var dialog:Dialog
    /**
     * An instance of the SensorManager class to manage access to device sensors.
     *
     * This property is used to access and manage various device sensors, such as the geomagnetic sensor,
     * accelerometer, gyroscope, and pressure sensor.
     */
    private var sensorManager: SensorManager? = null
    /**
     * Instances of Sensor class representing different device sensors.
     *
     * These properties store references to the device sensors, including geomagnetic, accelerometer,
     * gyroscope, and pressure sensors. They are used to register sensor listeners and retrieve sensor
     * data when required.
     */
    private var geomagneticSensor:Sensor?=null
    private var accelSensor:Sensor?=null
    private var gyroSensor:Sensor?=null
    private var pressureSensor:Sensor?=null
    /**
     * A 2D array representing the names of floors for different buildings.
     *
     * The array stores floor names for different buildings and is used to display floor options in the
     * spinner UI element.
     *
     * Example: floorsName[buildingIndex][floorIndex]
     */
    private var floorsName= arrayOf(
        arrayOf("1F Floor","6F Floor", "7F Floor","8F Floor"),
        arrayOf("Lt.1 Floor","Lt.2 Floor", "Lt.3 Floor"))
    /**
     * A 2D array representing the floor IDs for different buildings and floors.
     *
     * The array stores floor IDs corresponding to each building and floor combination. The floor IDs can
     * be used for querying or identifying specific floors in the database or application logic.
     *
     * Example: floorsId[buildingIndex][floorIndex]
     */
    private val floorsId= arrayOf(
        arrayOf("4","5","6","7"),
        arrayOf("1","2", "3")
    )
    private var selectedFloorId="1"
    /**
     * A 2D array representing the floor plan images for different buildings and floors.
     * currently is still hardcoded, but u can improve it to dynamic data from backend
     *
     * The array stores resource IDs of floor plan images to be displayed as ground overlays on the map.
     * The outer array represents different buildings, and the inner array represents different floors
     * within each building. The index of the outer array corresponds to the selected building, and the
     * index of the inner array corresponds to the selected floor within the building.
     *
     * Example: floors[buildingIndex][floorIndex]
     *
     * Note: Ensure that the resource IDs correspond to valid floor plan images stored in the drawable
     * resources of the application.
     */
    private val floors= arrayOf(
        //tw
        arrayOf(
            R.drawable.onef1_full,
            R.drawable.sixf6_full,
            R.drawable.sevenf7_full,
            R.drawable.eightf8_full
        ),
        //idb
        arrayOf(R.drawable.idb_lt1, R.drawable.idb_lt2, R.drawable.idb_lt3))
    /**
     * A 2D array representing the keys for crawled points for different buildings and floors.
     *
     * The array stores keys used to retrieve crawled points data from SharedPreferences or other storage
     * mechanisms. It is organized based on different buildings and floors, and each key corresponds to the
     * crawled points data for that specific floor.
     *
     * Example: crawledKeys[buildingIndex][floorIndex]
     */
    private lateinit var crawledKeys:Array<Array<String>>
    /**
     * An array of float values representing the bearing angles for different buildings.
     * Currently is still hardcoded but you can improve it to dynamic data from backend
     *
     * The array stores bearing angles (in degrees) used to rotate the map view for different buildings.
     * The bearing angle represents the orientation or direction of the building on the map.
     *
     * Example: bearings[buildingIndex]
     */
    private val bearings= arrayOf(46.3f,14.0f)
    /**
     * A 2D array representing the width and height of buildings for different floors.
     * Currently is still hardcoded but you can improve it to dynamic data from backend
     *
     * The array stores width and height values of buildings on the map. It is used in conjunction with
     * `southEast` array to determine the position of the ground overlay for the floor plan image. The
     * outer array represents different buildings, and the inner array represents different floors within
     * each building. The index of the outer array corresponds to the selected building, and the index of
     * the inner array corresponds to the selected floor within the building.
     *
     * Example: buildingWH[buildingIndex][floorIndex]
     *
     * Note: Ensure that the width and height values are accurate and correspond to the actual dimensions
     * of the buildings on the map.
     */
    private val buildingWH= arrayOf(arrayOf(16.3f,80.68f), arrayOf(15f,50f))
    /**
     * Integer and float properties representing the selected floor, building, and bearing angle, respectively.
     *
     * These properties store the index of the selected floor within the building, the index of the selected
     * building, and the bearing angle (in degrees) used to rotate the map view. They are updated based on
     * user interactions or spinner selections to change the displayed floor plan and points on the map.
     */
    private var selectedFloor=0
    private var selectedBuilding=0
    private var selectedBearing=0f
    /**
     * Mutable lists representing the current values of the geomagnetic, accelerometer, and gyroscope sensors.
     *
     * These lists are updated when sensor data changes, and they store the latest sensor readings for
     * geomagnetic, accelerometer, and gyroscope sensors, respectively.
     */
    private val currentGeo= mutableListOf<Float>()
    private val currentAccel= mutableListOf<Float>()
    private val currentGyro= mutableListOf<Float>()
    /**
     * Mutable lists representing the scanned BLE (Bluetooth Low Energy) devices and Wi-Fi access points.
     *
     * These lists store the scanned BLE devices and Wi-Fi access points' information obtained during the
     * scanning process. They are used to display the scanned data and perform further operations or analysis.
     */
    private val scannedBle= mutableListOf<BleData>()
    private val scannedWifi= mutableListOf<WifiData>()
    /**
     * Mutable list representing data sets for points.
     *
     * This list stores DataSet objects representing the data sets for different points on the map. The
     * DataSet class (not provided in the code snippet) should be defined to store relevant data for points,
     * such as coordinates, attributes, or other information.
     */
    private val dataSets= mutableListOf<DataSet>()
    /**
     * A boolean indicating whether the scanning process is currently active.
     *
     * This property is used to track the scanning status and control the BLE and Wi-Fi scanning processes.
     * It is set to true when the scanning process is active and false when the scanning process stops.
     */
    private var isScanning=false
    /**
     * An instance of the BleManager class for handling BLE (Bluetooth Low Energy) operations.
     *
     * This property is used to interact with BLE devices, perform BLE scans, and manage Bluetooth operations.
     */
    private lateinit var bleManager:BleManager
    /**
     * LatLng object representing the current latitude and longitude of the user's position.
     *
     * This property stores the current latitude and longitude coordinates of the user's position on the map.
     * It is updated when the user's location changes or when a new position is selected on the map.
     */
    private var curLatLng=LatLng(0.0,0.0)
    /**
     * String representing the file name for saving data.
     *
     * This property stores the file name used to save data or crawled points to a file or database.
     * The file name is updated based on specific operations or user interactions.
     */
    private var fileName=""
    /**
     * Integer representing the maximum data value.
     *
     * This property stores the maximum data value used for calculations or comparisons in the application.
     * The value may change based on specific scenarios or data processing requirements.
     */
    private var maxData=0
    /**
     * View binding instance for the custom dialog layout, which provides direct access to the UI elements
     * in the custom dialog layout XML file.
     */
    private lateinit var sheetView : LayoutDialogBinding
    /**
     * An instance of the WifiManager class for managing Wi-Fi operations.
     *
     * This property is used to interact with the Wi-Fi service, perform Wi-Fi scans, and manage Wi-Fi operations.
     */
    private lateinit var wifiManger :WifiManager
    /**
     * Boolean properties indicating the current mode of operation for BLE and Wi-Fi scanning.
     *
     * These properties are used to determine whether the application is in BLE mode, Wi-Fi mode, or both.
     * They are updated based on user preferences or specific scanning scenarios.
     */
    private var bleMode=false
    private var wifiMode=false
    /**
     * Instance of the SharedPef class for managing shared preferences.
     *
     * This property is used to interact with shared preferences to save and retrieve data persistently
     * across app sessions.
     */
    private lateinit var sharedPref:SharedPef

    /**
     * Called when the activity is created.
     * Initializes views, sets up the Google Map fragment, and checks for necessary permissions.
     * @param savedInstanceState The saved instance state bundle.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Inflate the layout and set up the binding for the activity
        binding= ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // Initialize shared preferences and presenter
        sharedPref=SharedPef(this)
        presenter= MainPresenter(this)
        // Set the default night mode to MODE_NIGHT_NO (Disable night mode)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        // Initialize sensors
        initSensor()
        // Check for necessary permissions
        checkPermissions()
        val mf=supportFragmentManager.findFragmentById(R.id.maps_view)
                as SupportMapFragment
        mf.getMapAsync(this)
        // Set up the onItemSelectedListener for the building spinner
        binding.spinnerBuilding.onItemSelectedListener = object :AdapterView.OnItemSelectedListener{
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                // Update the selectedBuilding and selectedBearing variables
                selectedBuilding=p2
                selectedBearing=bearings[p2]
                // Call the changeFloorSpinnerItems function to update the floor spinner items
                changeFloorSpinnerItems()
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {

            }

        }
        // Set up the onCheckedChangeListener for the switch to show points
        binding.swShowPoints.setOnCheckedChangeListener { _, value ->
            showPoints=value
            // Call the drawFloor function to draw the floor with points
            drawFloor()
        }

    }

    /**
     * Changes the items in the floor spinner based on the selected building and sets up the spinner's behavior.
     */
    private fun changeFloorSpinnerItems(){
        val aa = ArrayAdapter(this, android.R.layout.simple_spinner_item, floorsName[selectedBuilding])
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerFloor.adapter = aa
        if(selectedBuilding==0){
            mapView?.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(25.0119275,121.5414292),100f))
        }else{
            mapView?.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(-7.7711140765450395, 110.38687646895046),100f))
        }
        binding.spinnerFloor.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                // Update the selectedFloor and selectedFloorId variables
                selectedFloor=position
                selectedFloorId=floorsId[selectedBuilding][selectedFloor]
                // Get the corresponding crawledPoints for the selected building and floor
                crawledKeys= arrayOf(
                    arrayOf(sharedPref.crawledPointsEE1,sharedPref.crawledPointsIEE6,sharedPref.crawledPointsIEE7,sharedPref.crawledPointsIEE8),
                    arrayOf(sharedPref.crawledPointsIdb1,sharedPref.crawledPointsIdb2,sharedPref.crawledPointsIdb3)
                )
                crawledPoints = crawledKeys[selectedBuilding][selectedFloor]
                logE(selectedFloorId)
                presenter.getPoints(selectedFloorId)
            }
        }
    }

    /**
     * A companion object containing constants used for requesting permissions and opening GPS settings.
     *
     * @property REQUEST_CODE_PERMISSION_LOCATION The request code used when requesting location permissions.
     * @property REQUEST_CODE_OPEN_GPS The request code used when opening the GPS settings.
     * @property DIRECTORY A constant representing the directory.
     */
    companion object {
        const val REQUEST_CODE_PERMISSION_LOCATION = 2
        const val REQUEST_CODE_OPEN_GPS = 1
        const val DIRECTORY=""
    }

    /**
     * Checks and requests necessary permissions for the application.
     * The required permissions include ACCESS_FINE_LOCATION and WRITE_EXTERNAL_STORAGE.
     * On Android 12 (SDK_INT >= Build.VERSION_CODES.S), additional permissions BLUETOOTH_SCAN and BLUETOOTH_CONNECT are also included.
     *
     * If all permissions are already granted, the function calls [onPermissionGranted] for each granted permission.
     * If any permission is denied, it requests the required permissions using the [ActivityCompat.requestPermissions] method.
     */
        private fun checkPermissions() {
        val permissions = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
        )
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.S) {
            permissions.add(Manifest.permission.BLUETOOTH_SCAN)
            permissions.add(Manifest.permission.BLUETOOTH_CONNECT)
        }
        val permissionDeniedList = ArrayList<String>()
        for (permission in permissions) {
            val permissionCheck = this.let {
                ContextCompat.checkSelfPermission(it, permission)
            }
            if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
                if (!bluetoothAdapter.isEnabled) {
                    bluetoothAdapter.isEnabled
                }
                onPermissionGranted(permission)
            } else {
                permissionDeniedList.add(permission)
            }
        }
        if (permissionDeniedList.isNotEmpty()) {
            val deniedPermissions = permissionDeniedList.toTypedArray()
            this.let {
                ActivityCompat.requestPermissions(
                    it,
                    deniedPermissions,
                    REQUEST_CODE_PERMISSION_LOCATION
                )
            }
        }

    }

    /**
     * Checks if the GPS (Global Positioning System) is enabled on the device.
     *
     * @return True if GPS is enabled, false otherwise.
     */
    private fun checkGPSIsOpen(): Boolean {
        val locationManager = this.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            ?: return false
        return locationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)
    }


    private fun onPermissionGranted(permission: String) {
        when (permission) {
            Manifest.permission.ACCESS_FINE_LOCATION ->
                if (Build.VERSION.SDK_INT >=
                    Build.VERSION_CODES.M && !checkGPSIsOpen()
                ) {
                    AlertDialog.Builder(this)
                        .setTitle("Notifikasi")
                        .setMessage("BLE needs to open the positioning function")
                        .setNegativeButton("Cancel", { dialog, which -> this.finish() })
                        .setPositiveButton("Settings") { dialog, which ->
                            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                            startActivityForResult(intent, REQUEST_CODE_OPEN_GPS)
                        }
                        .setCancelable(false)
                        .show()
                } else {
                    val rule= BleScanRuleConfig.Builder().setScanTimeOut(5000).build()
                    bleManager.initScanRule(rule)
                    bleManager.init(application)
                    bleManager.enableBluetooth()
                    bleManager.enableLog(true)

                }
        }
    }

    /**
     * Initialize sensors (BLE, WiFi, and built-in sensors).
     * It initializes the BLE manager, WifiManager, and SensorManager.
     * It also logs information about the available sensors to the console.
     */
    private fun initSensor(){
        // Initialize BLE manager and enable Bluetooth
        bleManager= BleManager.getInstance()
        val rule= BleScanRuleConfig.Builder().setScanTimeOut(5000).build()
        bleManager.initScanRule(rule)
        bleManager.init(application)
        bleManager.enableBluetooth()
        bleManager.enableLog(true)

        wifiManger= applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

        sensorManager=getSystemService(Context.SENSOR_SERVICE) as SensorManager

        val availableSensor = sensorManager?.getSensorList(Sensor.TYPE_ALL)
        logE("Available ${availableSensor?.size}")
        availableSensor?.forEach {
            logE("Sensor ${it.name}")
        }

        geomagneticSensor=sensorManager?.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        accelSensor=sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        gyroSensor=sensorManager?.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        pressureSensor=sensorManager?.getDefaultSensor(Sensor.TYPE_PRESSURE)
    }

    /**
     * Unregister all sensor listeners to stop receiving sensor data updates.
     */
    private fun sensorStopListening(){
        sensorManager?.unregisterListener(geoListener)
        sensorManager?.unregisterListener(accelListener)
        sensorManager?.unregisterListener(gyroListener)
        sensorManager?.unregisterListener(pressureListener)
    }

    /**
     * Register sensor listeners to start receiving sensor data updates.
     * It registers listeners for geomagnetic, accelerometer, gyro, and pressure sensors.
     * It also logs "started" to the console to indicate that sensor listening has begun.
     */
    private fun sensorStartListening(){
        //geomagnetic
        sensorManager?.registerListener(geoListener, geomagneticSensor,SensorManager.SENSOR_DELAY_NORMAL)

        //accel
        sensorManager?.registerListener(accelListener, accelSensor,SensorManager.SENSOR_DELAY_NORMAL)

        //gyro
        sensorManager?.registerListener(gyroListener, gyroSensor,SensorManager.SENSOR_DELAY_NORMAL)

        //pressure
        sensorManager?.registerListener(pressureListener, pressureSensor,SensorManager.SENSOR_DELAY_NORMAL)
        logE("started")
    }

    /**
     * BroadcastReceiver for handling WiFi scan results.
     */
    private val wifiScanReceiver = object : BroadcastReceiver(){
        override fun onReceive(p0: Context?, p1: Intent?) {
            // Check if the WiFi scan was successful by retrieving the success flag from the intent.
            val success = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED,true)
            if (success) {
                val result = wifiManger.scanResults
                logE(result.toString())
                // Process the scan results and add WiFi data to the `scannedWifi` list.
                result.forEach{
                    scannedWifi.add(WifiData(it.BSSID,it.SSID,it.level.toString()))
                }
                // Add the collected WiFi data to the `dataSets` list.
                addData()
            }else {
                logE(wifiManger.scanResults.toString())
            }
        }

    }

    /**
     * Sensor listener for handling magnetometer sensor events.
     */
    private val geoListener = object:SensorEventListener{
        override fun onSensorChanged(event: SensorEvent) {
            //Log.e("Geo", "onSensorChanged: ${event.values.size}")
            // Process the magnetometer sensor data and store it in the `currentGeo` list.
            currentGeo.clear()
            currentGeo.addAll(event.values.toList())
        }

        override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
        }
    }

    /**
     * Sensor listener for handling accelerometer sensor events.
     */
    private val accelListener= object:SensorEventListener{
        override fun onSensorChanged(event: SensorEvent) {
            // Process the accelerometer sensor data and store it in the `currentAccel` list.
            currentAccel.clear()
            currentAccel.addAll(event.values.toList())
        }

        override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
        }
    }

    /**
     * Sensor listener for handling gyroscope sensor events.
     */
    private val gyroListener=object:SensorEventListener{
        override fun onSensorChanged(event: SensorEvent) {
            // Process the gyroscope sensor data and store it in the `currentGyro` list.
            currentGyro.clear()
            currentGyro.addAll(event.values.toList())
        }

        override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
        }
    }

    /**
     * Sensor listener for handling pressure sensor events.
     */
    private val pressureListener=object:SensorEventListener{
        override fun onSensorChanged(event: SensorEvent) {
            //Log.e("Pressure", "onSensorChanged: ${event.values.size}")
            // Process the pressure sensor data (not used in this implementation).
        }

        override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
        }
    }

    /**
     * Initiates the scanning process by calling the `readBle()` function and logs a message.
     */
    // Start BLE scanning using the `readBle()` function.
    private fun scanning(){
        readBle()
        logE("stopped")
    }

    /**
     * Initiates BLE scanning using the `BleManager`.
     */
    private fun readBle(){
        bleManager.scan(bleListener)
    }

    /**
     * The `bleListener` object implements the `BleScanCallback` interface to handle BLE scanning events.
     */
    private val bleListener=object : BleScanCallback() {
        override fun onScanStarted(success: Boolean) {
            Log.e("ble","scan started $success")
        }

        override fun onLeScan(bleDevice: BleDevice?) {
            super.onLeScan(bleDevice)
        }

        override fun onScanning(bleDevice: BleDevice) {
            Log.e("ble","scanning")
        }

        /**
         * Called when BLE scanning finishes.
         * The list of scanned BLE devices is passed as `scanResultList`.
         * @param scanResultList The list of BLE devices found during scanning.
         */
        override fun onScanFinished(scanResultList: List<BleDevice>) {
            Log.e("ble","scanning finished")
            logE(scanResultList.toString())
            setBleScanned(scanResultList)
            // If WiFi mode is enabled, start WiFi scanning; otherwise, add data to the dataSets list.
            if(wifiMode){
                wifiManger.startScan()
            }else {
                addData()
            }
        }
    }
    private fun setBleScanned(scanResultList: List<BleDevice>){
        scanResultList.forEach {
            scannedBle.add(BleData(it.mac,it.name,it.rssi.toString()))
        }
        logE("BLE data $scannedBle")
    }



    override fun onMapReady(map: GoogleMap) {
        mapView=map
        // Set up the Google Map with default settings
        mapView?.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(25.0119275,121.5414292),100f));
        // Set a long-click listener on the map to drop a marker and open a dialog
        mapView?.setOnMapLongClickListener(GoogleMap.OnMapLongClickListener {
            if(pinMarker!=null){
                pinMarker?.remove()
            }
            pinMarker = mapView?.addMarker(MarkerOptions()
                .position(it).title("Dropped Pin"))
            openCrawlDialog(it)
        })
    }
    /**
     * Opens the crawl dialog when a marker on the map is clicked.
     * The crawl dialog allows the user to start data collection for the selected fingerprint point.
     * @param it The LatLng object representing the latitude and longitude of the clicked marker.
     */
    private fun openCrawlDialog(it:LatLng){
        sheetView = LayoutDialogBinding.inflate(layoutInflater)
        curLatLng= LatLng(it.latitude,it.longitude)
        dialog=Dialog(this)
        dialog.setContentView(sheetView.root)
        sheetView.tvLatLng.text = "Current Lat/Lng : ${it.latitude}, ${it.longitude}"
        logE("${it.latitude}, ${it.longitude}")
        sheetView.tvTime.text=Calendar.getInstance().time.toString()
        // Set listeners for BLE and WiFi mode checkboxes
        sheetView.cbBleMode.setOnCheckedChangeListener { _, b ->
            bleMode=b
        }
        sheetView.cbWifiMode.setOnCheckedChangeListener { _, b ->
            wifiMode=b
        }
        // Start scanning for BLE devices when the "Start" button is clicked
        sheetView.btnStart.setOnClickListener {
            fileName="${sheetView.tvTime.text}_${binding.spinnerFloor.selectedItem}.csv"
            dataSets.clear()
            sheetView.pbScanning.toVisible()
            sheetView.btnStart.toGone()
            sheetView.btnStop.toVisible()
            if(sheetView.etInputMaxData.text.isBlank().not()) {
                maxData = sheetView.etInputMaxData.text.toString().toInt()
            }
            sensorStartListening()
            isScanning=!isScanning
            if (bleMode){
                scanning()
            }
            if(wifiMode){
                val intentFilter = IntentFilter()
                intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
                registerReceiver(wifiScanReceiver, intentFilter)
                wifiManger.startScan()
            }

            dialog.setCancelable(false)
        }
        // Stop data collection when the "Stop" button is clicked
        sheetView.btnStop.setOnClickListener {
            stopDataCrawl()
        }
        dialog.show()
    }

    /**
     * Stops the data collection and writes the collected data to a CSV file.
     * Also updates the list of crawled points and redraws the floor plan.
     */
    private fun stopDataCrawl(){
        isScanning=false
        if (wifiMode){
            unregisterReceiver(wifiScanReceiver)
        }
        if(bleMode){
            bleManager.cancelScan()
        }
        sensorStopListening()
        // Save the collected data to a CSV file
        writingFile()
        // Update the list of crawled points
        crawledPoints+=""+curLatLng.latitude+","+curLatLng.longitude+";"
        updateCrawledPoint()
        dialog.setCancelable(true)
        // Redraw the floor plan with updated points
        drawFloor()
    }

    /**
     * Draw the floor plan and fingerprint points on the Google Map.
     *
     * This function is responsible for displaying the floor plan image as a ground overlay on the map.
     * The overlay is positioned according to the selected building and floor. It also adds markers to
     * display fingerprint points based on defined and crawled points. The color of the markers depends
     * on whether the point has been crawled or not. If `showPoints` is true, the fingerprint points
     * will be displayed with appropriate markers. If `showPoints` is false, only the floor plan image
     * will be displayed without any fingerprint points.
     *
     * Note: This function will clear the current map and reset the overlay and markers to display the
     * selected floor's data. It utilizes the `floors`, `southEast`, `buildingWH`, `definedPoints`,
     * `crawledPoints`, and `selectedBuilding` variables to determine the data to be displayed.
     *
     * Important: Make sure that `mapView` is properly initialized and available for displaying the map
     * before calling this function. Additionally, a marker click listener is set on the map to allow
     * opening the crawl dialog for a specific fingerprint point.
     *
     * Note: The `bitmapDescriptorFromVector` function is used to create bitmap descriptors for the
     * marker icons, which are vector drawables converted to bitmaps for custom marker icons.
     *
     * @see bitmapDescriptorFromVector
     * `showPoints` A boolean flag indicating whether to display fingerprint points (true) or not (false).
     * When set to true, the function will add markers to display fingerprint points based on defined
     * and crawled points on the map. When set to false, only the floor plan image will be displayed
     * without any fingerprint points.
     */
    private fun drawFloor(){
        logE("drawing")
        // Clear the map and set a ground overlay for the floor plan image
        mapView?.clear()
        floorOverlay = GroundOverlayOptions()
            .image(BitmapDescriptorFactory.fromResource(floors[selectedBuilding][selectedFloor]))
            .anchor(1f, 1f)
            .positionFromBounds(southEast[selectedBuilding].getBounds(buildingWH[selectedBuilding][0], buildingWH[selectedBuilding][1]))
            .bearing(selectedBearing)
        mapView?.addGroundOverlay(floorOverlay)
        // Add markers to display fingerprint points based on defined and crawled points
        if (showPoints) {
            definedPoints.forEach {
                val pointString = it.lat.toString()+","+it.lng.toString()
                if(crawledPoints.contains(pointString)){
                    mapView?.addMarker(
                        MarkerOptions()
                            .anchor(0.5f, 0.5f)
                            .icon(
                                bitmapDescriptorFromVector(
                                    this,
                                    R.drawable.ic_baseline_my_location_24_green
                                )
                            )
                            .position(LatLng(it.lat, it.lng)).title("Fingerprint point")
                    )
                }else {
                    mapView?.addMarker(
                        MarkerOptions()
                            .anchor(0.5f, 0.5f)
                            .icon(
                                bitmapDescriptorFromVector(
                                    this,
                                    R.drawable.ic_baseline_my_location_24_red
                                )
                            )
                            .position(LatLng(it.lat, it.lng)).title("Fingerprint point")
                    )
                }
                // Set a marker click listener to open the crawl dialog for a specific point
                mapView?.setOnMarkerClickListener(this)
            }
        }
    }

    /**
     * Writes the collected data to a CSV file.
     * @return The file path where the data is stored.
     */
    private fun writingFile(){
        if(dataSets.size>0) {
            val writer = FileWriter(this)
            logE(dataSets.toString())
            val result = writer.writeToFile(dataSets, fileName)
            presenter.sendCrawledData(
                dataSets,
                androidVersion = android.os.Build.VERSION.CODENAME.toString()
            )
            toast("File saved in $result")
        }
    }
    /**
     * Adds data to the dataSets list.
     * Data from scanned BLE and WiFi devices is added to the list.
     */
    private fun addData(){
        // Add data from scanned BLE and WiFi devices to the dataSets list
        dataSets
            .add(
                DataSet(Calendar.getInstance().time.toString(),
                    curLatLng.latitude,curLatLng.longitude,
                    scannedBle.toMutableList(), scannedWifi.toMutableList(),currentGeo,currentAccel,currentGyro)
            )
        sheetView.tvCounter.text="Data Collected : ${dataSets.size}"

        resetScanned()
        if(maxData>0){
            if(maxData == dataSets.size){
                isScanning=false
                stopDataCrawl()
            }
        }
        if(isScanning) {
            if (bleMode) {
                bleManager.cancelScan()
                readBle()
                logE("blee")
            }else if(wifiMode){
                wifiManger.startScan()
                logE("here")
            }
        }
        Log.e("dataset collected",dataSets.size.toString())
    }
    private fun resetScanned(){
        scannedBle.clear()
        scannedWifi.clear()
    }


    /**
     * Updates the list of crawled points in shared preferences.
     * The crawled points are saved based on the selected building and floor.
     */
    private fun updateCrawledPoint(){
        // Save the crawled points to shared preferences based on the selected building and floor
        when(selectedBuilding){
            0->{
                when(selectedFloor){
                    0-> sharedPref.crawledPointsEE1=crawledPoints
                    1-> sharedPref.crawledPointsIEE6=crawledPoints
                    2-> sharedPref.crawledPointsIEE7=crawledPoints
                    3-> sharedPref.crawledPointsIEE8=crawledPoints
                }
            }
            1->{
                when(selectedFloor){
                    0-> sharedPref.crawledPointsIdb1=crawledPoints
                    1-> sharedPref.crawledPointsIdb2=crawledPoints
                    2-> sharedPref.crawledPointsIdb3=crawledPoints
                }
            }
        }
    }
    /**
     * Called when a marker on the map is clicked.
     * Opens the crawl dialog for the selected marker.
     * @param p0 The clicked marker object.
     * @return Always returns false.
     */
    override fun onMarkerClick(p0: Marker): Boolean {
        openCrawlDialog(p0.position)
        return false
    }

    override fun onSensorChanged(event: SensorEvent) {

    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {

    }


    /**
     * Implementation of the MainView interface function.
     * Currently, this function does nothing in the MainActivity.
     */
    override fun onLoading() {

    }

    /**
     * Implementation of the MainView interface function.
     * Updates the definedPoints list with the received data and redraws the floor plan.
     * @param data The list of defined points received from the presenter.
     */
    override fun result(data:List<Point>) {
        runOnUiThread {
            logE(data.size.toString())
            definedPoints.clear()
            definedPoints.addAll(data)
            drawFloor()
        }
    }

    /**
     * Implementation of the MainView interface function.
     * Callback method invoked when the result of the data upload operation is available
     * @param success A boolean indicating whether the data upload was successful or not.
     * @param msg The message associated with the result (e.g., success message or error message).
     * Implement this method to handle the result of the data upload operation and update the UI accordingly.
     */
    override fun resultUpload(success: Boolean,msg:String?) {
        runOnUiThread {
            if (!success){
                toast("Error $msg")
            }else{
                toast("Data sent to DB")
            }
            sheetView.pbScanning.toGone()
            sheetView.btnStart.toVisible()
            sheetView.btnStop.toGone()
        }
    }

}