package com.dngwjy.datasetcollector

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
import android.widget.CompoundButton
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.clj.fastble.BleManager
import com.clj.fastble.callback.BleScanCallback
import com.clj.fastble.data.BleDevice
import com.clj.fastble.scan.BleScanRuleConfig
import com.dngwjy.datasetcollector.data.*
import com.dngwjy.datasetcollector.databinding.ActivityMainBinding
import com.dngwjy.datasetcollector.databinding.LayoutDialogBinding
import com.dngwjy.datasetcollector.databinding.LayoutFingerprintPointsBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import java.util.*

class MainActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener,SensorEventListener,MainView {
    private lateinit var presenter: MainPresenter
    private lateinit var binding: ActivityMainBinding
    private val definedPoints= mutableListOf<Point>()
    private var crawledPoints= ""
    private var showPoints=true
    private var mapView:GoogleMap?=null     // 25.01159698395817, 121.54119884517124 25.011595368321558, 121.5412002293108                   //-7.771113997008627, 110.38687652026972 (-7.771120618305493, 110.3868760009918)  (-7.7711140765450395, 110.38687646895046)
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
        arrayOf(R.drawable.onef1_full,R.drawable.sixf6_full,R.drawable.sevenf7_full,R.drawable.eightf8_full),
        //idb
        arrayOf(R.drawable.idb_lt1,R.drawable.idb_lt2,R.drawable.idb_lt3))
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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        sharedPref=SharedPef(this)
        presenter= MainPresenter(this)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        initSensor()
        checkPermissions()
        val mf=supportFragmentManager.findFragmentById(R.id.maps_view)
                as SupportMapFragment
        mf.getMapAsync(this)
        binding.spinnerBuilding.onItemSelectedListener = object :AdapterView.OnItemSelectedListener{
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                selectedBuilding=p2
                selectedBearing=bearings[p2]
                changeFloorSpinnerItems()
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {

            }

        }
        binding.swShowPoints.setOnCheckedChangeListener { _, value ->
            showPoints=value
            drawFloor()
        }

    }

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
                selectedFloor=position
                selectedFloorId=floorsId[selectedBuilding][selectedFloor]
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

    companion object {
        private const val REQUEST_CODE_PERMISSION_LOCATION = 2
        private const val REQUEST_CODE_OPEN_GPS = 1
        const val DIRECTORY=""
    }

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
                        .setNegativeButton("Batal", { dialog, which -> this.finish() })
                        .setPositiveButton("Pengaturan") { dialog, which ->
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

    private fun initSensor(){
        bleManager= BleManager.getInstance()

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

    private fun sensorStopListening(){
        sensorManager?.unregisterListener(geoListener)
        sensorManager?.unregisterListener(accelListener)
        sensorManager?.unregisterListener(gyroListener)
        sensorManager?.unregisterListener(pressureListener)
    }

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

    private val wifiScanReceiver = object : BroadcastReceiver(){
        override fun onReceive(p0: Context?, p1: Intent?) {
            val success = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED,true)
            if (success) {
                val result = wifiManger.scanResults
                logE(result.toString())
                result.forEach{
                    scannedWifi.add(WifiData(it.BSSID,it.SSID,it.level.toString()))
                }
                addData()
            }else {
                logE(wifiManger.scanResults.toString())
            }
        }

    }

    private val geoListener = object:SensorEventListener{
        override fun onSensorChanged(event: SensorEvent) {
            //Log.e("Geo", "onSensorChanged: ${event.values.size}")
            currentGeo.clear()
            currentGeo.addAll(event.values.toList())
        }

        override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
        }
    }
    private val accelListener= object:SensorEventListener{
        override fun onSensorChanged(event: SensorEvent) {
            //Log.e("Accel", "onSensorChanged: ${event.values.size}")
            currentAccel.clear()
            currentAccel.addAll(event.values.toList())
        }

        override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
        }
    }
    private val gyroListener=object:SensorEventListener{
        override fun onSensorChanged(event: SensorEvent) {
            //Log.e("Gyro", "onSensorChanged: ${event.values.size}")
            currentGyro.clear()
            currentGyro.addAll(event.values.toList())
        }

        override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
        }
    }
    private val pressureListener=object:SensorEventListener{
        override fun onSensorChanged(event: SensorEvent) {
            //Log.e("Pressure", "onSensorChanged: ${event.values.size}")
        }

        override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
        }
    }
    private fun scanning(){
        readBle()
        logE("stopped")
    }

    private fun readBle(){
        bleManager.scan(bleListener)
    }
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

        override fun onScanFinished(scanResultList: List<BleDevice>) {
            Log.e("ble","scanning finished")
            logE(scanResultList.toString())
            setBleScanned(scanResultList)
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
        mapView?.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(25.0119275,121.5414292),100f));
        mapView?.setOnMapLongClickListener(GoogleMap.OnMapLongClickListener {
            if(pinMarker!=null){
                pinMarker?.remove()
            }
            pinMarker = mapView?.addMarker(MarkerOptions()
                .position(it).title("Dropped Pin"))
            openCrawlDialog(it)
        })
    }
    private fun openCrawlDialog(it:LatLng){
        sheetView = LayoutDialogBinding.inflate(layoutInflater)
        curLatLng= LatLng(it.latitude,it.longitude)
        dialog=Dialog(this)
        dialog.setContentView(sheetView.root)
        sheetView.tvLatLng.text = "Current Lat/Lng : ${it.latitude}, ${it.longitude}"
        logE("${it.latitude}, ${it.longitude}")
        sheetView.tvTime.text=Calendar.getInstance().time.toString()
        sheetView.cbBleMode.setOnCheckedChangeListener { _, b ->
            bleMode=b
        }
        sheetView.cbWifiMode.setOnCheckedChangeListener { _, b ->
            wifiMode=b
        }
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
        sheetView.btnStop.setOnClickListener {
            stopDataCrawl()
        }
        dialog.show()
    }
    private fun stopDataCrawl(){
        isScanning=false
        if (wifiMode){
            unregisterReceiver(wifiScanReceiver)
        }
        if(bleMode){
            bleManager.cancelScan()
        }
        sensorStopListening()
        writingFile()
        sheetView.pbScanning.toGone()
        sheetView.btnStart.toVisible()
        sheetView.btnStop.toGone()
        crawledPoints+=""+curLatLng.latitude+","+curLatLng.longitude+";"
        updateCrawledPoint()
        dialog.setCancelable(true)
        drawFloor()
    }
    private fun drawFloor(){
        logE("drawing")
        mapView?.clear()
//        if(selectedBuilding==1) {
//            floorOverlay = GroundOverlayOptions()
//                .image(BitmapDescriptorFactory.fromResource(floors[selectedBuilding][selectedFloor]))
//                .anchor(1f, 1f)
//                .position(
//                    southEast[selectedBuilding],
//                    buildingWH[selectedBuilding][0],
//                    buildingWH[selectedBuilding][1]
//                )
//                .bearing(selectedBearing)
//        }else{
            floorOverlay = GroundOverlayOptions()
                .image(BitmapDescriptorFactory.fromResource(floors[selectedBuilding][selectedFloor]))
                .anchor(1f, 1f)
                .positionFromBounds(southEast[selectedBuilding].getBounds(buildingWH[selectedBuilding][0], buildingWH[selectedBuilding][1]))
                .bearing(selectedBearing)
        //}
        mapView?.addGroundOverlay(floorOverlay)
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
                mapView?.setOnMarkerClickListener(this)
            }
        }
    }

    override fun onMarkerClick(p0: Marker): Boolean {
        openCrawlDialog(p0.position)
        return false
    }

    override fun onSensorChanged(event: SensorEvent) {

    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {

    }

    private fun writingFile(){
        val writer=FileWriter(this)
        logE(dataSets.toString())
        val result=writer.writeToFile(dataSets, fileName)
        toast("File tersimpan di $result")
    }
    private fun addData(){
//        logE("add data$scannedBle")
//        logE("add data $scannedWifi")
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
//        fixedMAC.forEach {
//            scannedBle.add(BleData(it,"100"))
//        }
    }

    override fun onLoading() {

    }

    override fun result(data:List<Point>) {
        runOnUiThread {
            logE(data.size.toString())
            definedPoints.clear()
            definedPoints.addAll(data)
            drawFloor()
        }
    }
    private fun updateCrawledPoint(){
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

}