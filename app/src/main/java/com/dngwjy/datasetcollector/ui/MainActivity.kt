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
     * A companion object containing constants used for requesting permissions and opening GPS settings.
     *
     * @property REQUEST_CODE_PERMISSION_LOCATION The request code used when requesting location permissions.
     * @property REQUEST_CODE_OPEN_GPS The request code used when opening the GPS settings.
     * @property DIRECTORY A constant representing the directory.
     */
    companion object {
        /**
         * The request code used when requesting location permissions.
         */
        private const val REQUEST_CODE_PERMISSION_LOCATION = 2

        /**
         * The request code used when opening the GPS settings.
         */
        private const val REQUEST_CODE_OPEN_GPS = 1

        /**
         * A constant representing the directory.
         */
        const val DIRECTORY = ""
    }

    // Properties and variables
    private lateinit var presenter: MainPresenter
    private lateinit var binding: ActivityMainBinding
    private val definedPoints= mutableListOf<Point>()
    private var crawledPoints= ""
    private var showPoints=true
    private var mapView:GoogleMap?=null
    private val southEast=arrayOf(LatLng(25.01159698395817, 121.54119884517124),LatLng(-7.771120618305493, 110.3868760009918))
    private var pinMarker:Marker?=null
    private lateinit var floorOverlay: GroundOverlayOptions
    private lateinit var dialog:Dialog
    private var sensorManager: SensorManager? = null
    private var geomagneticSensor:Sensor?=null
    private var accelSensor:Sensor?=null
    private var gyroSensor:Sensor?=null
    private var pressureSensor:Sensor?=null
    private var floorsName= arrayOf(
        arrayOf("1F Floor","6F Floor", "7F Floor","8F Floor"),
        arrayOf("Lt.1 Floor","Lt.2 Floor", "Lt.3 Floor"))
    private val floorsId= arrayOf(
        arrayOf("4","5","6","7"),
        arrayOf("1","2", "3")
    )
    private var selectedFloorId="1"
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
    private lateinit var crawledKeys:Array<Array<String>>
    private val bearings= arrayOf(46.3f,14.0f)
    private val buildingWH= arrayOf(arrayOf(16.3f,80.68f), arrayOf(15f,50f))
    private var selectedFloor=0
    private var selectedBuilding=0
    private var selectedBearing=0f
    private val currentGeo= mutableListOf<Float>()
    private val currentAccel= mutableListOf<Float>()
    private val currentGyro= mutableListOf<Float>()
    private val scannedBle= mutableListOf<BleData>()
    private val scannedWifi= mutableListOf<WifiData>()
    private val dataSets= mutableListOf<DataSet>()
    private var isScanning=false
    private lateinit var bleManager:BleManager
    private var curLatLng=LatLng(0.0,0.0)
    private var fileName=""
    private var maxData=0
    private lateinit var sheetView : LayoutDialogBinding
    private lateinit var wifiManger :WifiManager
    private var bleMode=false
    private var wifiMode=false
    private lateinit var sharedPref:SharedPef

    /**
     * Called when the activity is created.
     * Initializes views, sets up the Google Map fragment, and checks for necessary permissions.
     * @param savedInstanceState The saved instance state bundle.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inflate the layout and set up the binding for the activity
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize shared preferences and presenter
        sharedPref = SharedPef(this)
        presenter = MainPresenter(this)

        // Set the default night mode to MODE_NIGHT_NO (Disable night mode)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        // Initialize sensors
        initSensor()

        // Check for necessary permissions
        checkPermissions()

        // Get the Google Map fragment and set up the map asynchronously
        val mf = supportFragmentManager.findFragmentById(R.id.maps_view) as SupportMapFragment
        mf.getMapAsync(this)

        // Set up the onItemSelectedListener for the building spinner
        binding.spinnerBuilding.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                // Update the selectedBuilding and selectedBearing variables
                selectedBuilding = p2
                selectedBearing = bearings[p2]

                // Call the changeFloorSpinnerItems function to update the floor spinner items
                changeFloorSpinnerItems()
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                // No action when nothing is selected
            }
        }

        // Set up the onCheckedChangeListener for the switch to show points
        binding.swShowPoints.setOnCheckedChangeListener { _, value ->
            // Update the showPoints variable based on the switch state
            showPoints = value

            // Call the drawFloor function to draw the floor with points
            drawFloor()
        }
    }

    /**
     * Changes the items in the floor spinner based on the selected building and sets up the spinner's behavior.
     */
    private fun changeFloorSpinnerItems() {
        // Create an ArrayAdapter with the floor names for the selected building
        val aa = ArrayAdapter(this, android.R.layout.simple_spinner_item, floorsName[selectedBuilding])
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        // Set the ArrayAdapter as the adapter for the spinner
        binding.spinnerFloor.adapter = aa

        // Move the map camera to the corresponding location based on the selected building
        if (selectedBuilding == 0) {
            mapView?.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(25.0119275, 121.5414292), 100f))
        } else {
            mapView?.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(-7.7711140765450395, 110.38687646895046), 100f))
        }

        // Set up the onItemSelectedListener for the spinner
        binding.spinnerFloor.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
                // No action when nothing is selected
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                // Update the selectedFloor and selectedFloorId variables
                selectedFloor = position
                selectedFloorId = floorsId[selectedBuilding][selectedFloor]

                // Get the corresponding crawledPoints for the selected building and floor
                crawledKeys = arrayOf(
                    arrayOf(sharedPref.crawledPointsEE1, sharedPref.crawledPointsIEE6, sharedPref.crawledPointsIEE7, sharedPref.crawledPointsIEE8),
                    arrayOf(sharedPref.crawledPointsIdb1, sharedPref.crawledPointsIdb2, sharedPref.crawledPointsIdb3)
                )
                crawledPoints = crawledKeys[selectedBuilding][selectedFloor]

                // Log the selectedFloorId and call the presenter's getPoints function with the selectedFloorId
                logE(selectedFloorId)
                presenter.getPoints(selectedFloorId)
            }
        }
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
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
                // Call onPermissionGranted for each granted permission.
                onPermissionGranted(permission)
            } else {
                // Add the denied permission to the permissionDeniedList.
                permissionDeniedList.add(permission)
            }
        }
        // If there are any denied permissions, request them using ActivityCompat.requestPermissions.
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
        // Get the LocationManager service from the current context.
        val locationManager = this.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        // If the LocationManager is not available (e.g., due to missing permissions or service not supported),
        // return false to indicate that GPS is not open.
        locationManager ?: return false
        // Check if the GPS_PROVIDER is enabled in the LocationManager.
        return locationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)
    }


    /**
     * Handles actions after a specific permission has been granted.
     *
     * @param permission The permission that has been granted.
     */
    private fun onPermissionGranted(permission: String) {
        when (permission) {
            Manifest.permission.ACCESS_FINE_LOCATION -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !checkGPSIsOpen()) {
                    // If GPS is not open and API level is higher than or equal to Marshmallow, show an alert dialog.
                    AlertDialog.Builder(this)
                        .setTitle("Notifikasi")
                        .setMessage("BLE needs to open the positioning function")
                        .setNegativeButton("Batal") { dialog, which ->
                            // If the user chooses "Batal" (Cancel), finish the current activity.
                            this.finish()
                        }
                        .setPositiveButton("Pengaturan") { dialog, which ->
                            // If the user chooses "Pengaturan" (Settings), open location settings.
                            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                            startActivityForResult(intent, REQUEST_CODE_OPEN_GPS)
                        }
                        .setCancelable(false) // Prevent the dialog from being canceled with the back button.
                        .show()
                } else {
                    // If GPS is open or API level is lower than Marshmallow, proceed with BLE scan setup.
                    val rule = BleScanRuleConfig.Builder().setScanTimeOut(5000).build()
                    bleManager.initScanRule(rule)
                    bleManager.init(application)
                    bleManager.enableBluetooth()
                    bleManager.enableLog(true)
                }
            }
        }
    }

    /**
     * Initialize sensors (BLE, WiFi, and built-in sensors).
     * It initializes the BLE manager, WifiManager, and SensorManager.
     * It also logs information about the available sensors to the console.
     */
    private fun initSensor() {
        // Initialize BLE manager and enable Bluetooth
        bleManager = BleManager.getInstance()

        // Get a reference to the WifiManager
        wifiManger = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

        // Get a reference to the SensorManager for sensor management
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        // Get a list of all available sensors and log information about them
        val availableSensors = sensorManager?.getSensorList(Sensor.TYPE_ALL)
        logE("Available ${availableSensors?.size} sensors")
        availableSensors?.forEach {
            logE("Sensor ${it.name}")
        }

        // Get references to specific sensors: geomagnetic, accelerometer, gyro, and pressure sensors
        geomagneticSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        accelSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        gyroSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        pressureSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_PRESSURE)
    }

    /**
     * Unregister all sensor listeners to stop receiving sensor data updates.
     */
    private fun sensorStopListening() {
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
    private fun sensorStartListening() {
        // Register listener for the geomagnetic sensor with SENSOR_DELAY_NORMAL delay
        sensorManager?.registerListener(geoListener, geomagneticSensor, SensorManager.SENSOR_DELAY_NORMAL)

        // Register listener for the accelerometer sensor with SENSOR_DELAY_NORMAL delay
        sensorManager?.registerListener(accelListener, accelSensor, SensorManager.SENSOR_DELAY_NORMAL)

        // Register listener for the gyro sensor with SENSOR_DELAY_NORMAL delay
        sensorManager?.registerListener(gyroListener, gyroSensor, SensorManager.SENSOR_DELAY_NORMAL)

        // Register listener for the pressure sensor with SENSOR_DELAY_NORMAL delay
        sensorManager?.registerListener(pressureListener, pressureSensor, SensorManager.SENSOR_DELAY_NORMAL)

        // Log "started" to indicate that sensor listening has been initiated
        logE("started")
    }

    /**
     * BroadcastReceiver for handling WiFi scan results.
     */
    private val wifiScanReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            // Check if the WiFi scan was successful by retrieving the success flag from the intent.
            val success = intent?.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, true) ?: false
            if (success) {
                // WiFi scan was successful, retrieve the scan results.
                val scanResults = wifiManger.scanResults
                // Log the scan results (optional).
                logE(scanResults.toString())
                // Process the scan results and add WiFi data to the `scannedWifi` list.
                scanResults.forEach {
                    scannedWifi.add(WifiData(it.BSSID, it.SSID, it.level.toString()))
                }
                // Add the collected WiFi data to the `dataSets` list.
                addData()
            } else {
                // WiFi scan was not successful, log the scan results (optional).
                logE(wifiManger.scanResults.toString())
                // Handle any error or fallback action if needed.
                // For example, you can try to rescan or show an error message.
            }
        }
    }

    /**
     * Sensor listener for handling magnetometer sensor events.
     */
    private val geoListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            // Process the magnetometer sensor data and store it in the `currentGeo` list.
            currentGeo.clear()
            currentGeo.addAll(event.values.toList())
        }

        override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
            // This function is called when the sensor accuracy changes (not used in this implementation).
        }
    }

    /**
     * Sensor listener for handling accelerometer sensor events.
     */
    private val accelListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            // Process the accelerometer sensor data and store it in the `currentAccel` list.
            currentAccel.clear()
            currentAccel.addAll(event.values.toList())
        }

        override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
            // This function is called when the sensor accuracy changes (not used in this implementation).
        }
    }

    /**
     * Sensor listener for handling gyroscope sensor events.
     */
    private val gyroListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            // Process the gyroscope sensor data and store it in the `currentGyro` list.
            currentGyro.clear()
            currentGyro.addAll(event.values.toList())
        }

        override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
            // This function is called when the sensor accuracy changes (not used in this implementation).
        }
    }

    /**
     * Sensor listener for handling pressure sensor events.
     */
    private val pressureListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            // Process the pressure sensor data (not used in this implementation).
        }

        override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
            // This function is called when the sensor accuracy changes (not used in this implementation).
        }
    }

    /**
     * Initiates the scanning process by calling the `readBle()` function and logs a message.
     */
    private fun scanning() {
        // Start BLE scanning using the `readBle()` function.
        readBle()
        // Log a message indicating the scanning has started.
        logE("scanning started")
    }

    /**
     * Initiates BLE scanning using the `BleManager`.
     */
    private fun readBle(){
        // Start BLE scanning using the `BleManager` and pass the `bleListener`
        bleManager.scan(bleListener)
    }

    /**
     * The `bleListener` object implements the `BleScanCallback` interface to handle BLE scanning events.
     */
    private val bleListener = object : BleScanCallback() {
        /**
         * Called when BLE scanning starts.
         * @param success True if the scan was started successfully, false otherwise.
         */
        override fun onScanStarted(success: Boolean) {
            Log.e("ble", "scan started $success")
        }

        /**
         * Called when a BLE device is found during scanning.
         * Note: This method is not used in this implementation, and the super method is called for compatibility.
         * @param bleDevice The BLE device found during scanning.
         */
        override fun onLeScan(bleDevice: BleDevice?) {
            super.onLeScan(bleDevice)
        }

        /**
         * Called repeatedly during BLE scanning.
         * @param bleDevice The BLE device being scanned.
         */
        override fun onScanning(bleDevice: BleDevice) {
            Log.e("ble", "scanning")
        }

        /**
         * Called when BLE scanning finishes.
         * The list of scanned BLE devices is passed as `scanResultList`.
         * @param scanResultList The list of BLE devices found during scanning.
         */
        override fun onScanFinished(scanResultList: List<BleDevice>) {
            Log.e("ble", "scanning finished")
            logE(scanResultList.toString())
            setBleScanned(scanResultList)

            // If WiFi mode is enabled, start WiFi scanning; otherwise, add data to the dataSets list.
            if (wifiMode) {
                wifiManger.startScan()
            } else {
                addData()
            }
        }
    }

    /**
     * Called after BleManager scan finished
     * Function to add scanned BLE device to `scannedBle`
     * @param scanResultList The list of scanned BLE Device.
     */
    private fun setBleScanned(scanResultList: List<BleDevice>){
        scanResultList.forEach {
            scannedBle.add(BleData(it.mac,it.name,it.rssi.toString()))
        }
        logE("BLE data $scannedBle")
    }

    /**
     * Called when the Google Map is ready to be used.
     * Sets up the Google Map with default settings and adds a long-click listener to drop pins on the map.
     * @param map The Google Map instance.
     */
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
        sheetView.pbScanning.toGone()
        sheetView.btnStart.toVisible()
        sheetView.btnStop.toGone()
        // Update the list of crawled points
        crawledPoints+=""+curLatLng.latitude+","+curLatLng.longitude+";"
        updateCrawledPoint()
        dialog.setCancelable(true)
        // Redraw the floor plan with updated points
        drawFloor()
    }

    /**
     * Function to draw the floor plan and display fingerprint points
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
            val writer= FileWriter(this)
            val result=writer.writeToFile(dataSets, fileName)
            presenter.sendCrawledData(
                dataSets,
                androidVersion = android.os.Build.VERSION.CODENAME.toString()
            )
            toast("File tersimpan di $result")
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

    /**
     * Called when a sensor's value changes.
     * Currently, this function does nothing in the MainActivity.
     * @param event The SensorEvent object containing the sensor's data.
     */
    override fun onSensorChanged(event: SensorEvent) {

    }

    /**
     * Called when the accuracy of a sensor changes.
     * Currently, this function does nothing in the MainActivity.
     * @param sensor The sensor whose accuracy changed.
     * @param accuracy The new accuracy value.
     */
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

}