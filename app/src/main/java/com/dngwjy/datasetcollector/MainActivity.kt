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
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import com.clj.fastble.BleManager
import com.clj.fastble.callback.BleScanCallback
import com.clj.fastble.data.BleDevice
import com.dngwjy.datasetcollector.databinding.ActivityMainBinding
import com.dngwjy.datasetcollector.databinding.LayoutDialogBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.util.*

class MainActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener,SensorEventListener {
    private val fixedMAC= mutableListOf<String>("78:02:B7:2A:04:4A","A0:78:17:5B:E1:22")
    private lateinit var binding: ActivityMainBinding
    private var mapView:GoogleMap?=null
    private val southEast=arrayOf(LatLng(25.011595368321558, 121.5412002293108),LatLng(-7.7711140765450395, 110.38687646895046))
    private var pinMarker:Marker?=null
    private lateinit var floorOverlay: GroundOverlayOptions
    private var sensorManager: SensorManager? = null
    private var geomagneticSensor:Sensor?=null
    private var accelSensor:Sensor?=null
    private var gyroSensor:Sensor?=null
    private var pressureSensor:Sensor?=null
    private var floorsName= arrayOf(
        arrayOf("1F Floor","6F Floor", "7F Floor","8F Floor"),
        arrayOf("Lt.1 Floor","Lt.2 Floor", "Lt.3 Floor"))
    private val floors= arrayOf(
        //tw
        arrayOf(R.drawable.onef1_full,R.drawable.sixf6_full,R.drawable.sevenf7_full,R.drawable.eightf8_full),
        //idb
        arrayOf(R.drawable.idb_lt1,R.drawable.idb_lt2,R.drawable.idb_lt3))
    private val bearings= arrayOf(47f,13.5f)
    private val buildingWH= arrayOf(arrayOf(18f,80f), arrayOf(15f,50f))
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
    private lateinit var wifiManger :WifiManager
    private var bleMode=false
    private var wifiMode=false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        val mf=supportFragmentManager.findFragmentById(R.id.maps_view)
        as SupportMapFragment
        mf.getMapAsync(this)
        initSensor()
        checkPermissions()
        binding.spinnerBuilding.onItemSelectedListener = object :AdapterView.OnItemSelectedListener{
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                selectedBuilding=p2
                selectedBearing=bearings[p2]
                changeFloorSpinnerItems()
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {

            }

        }

    }

    private fun changeFloorSpinnerItems(){
        val aa = ArrayAdapter(this, android.R.layout.simple_spinner_item, floorsName[selectedBuilding])
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerFloor.adapter = aa
        if(selectedBuilding==0){
            mapView?.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(25.0119275,121.5414292),19f))
        }else{
            mapView?.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(-7.7711140765450395, 110.38687646895046),19f))
        }
        binding.spinnerFloor.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedFloor=position
                drawFloor()
            }
        }
    }

    companion object {
        private const val REQUEST_CODE_PERMISSION_LOCATION = 2
        private const val REQUEST_CODE_OPEN_GPS = 1
        const val DIRECTORY=""
    }

    private fun checkPermissions() {
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (!bluetoothAdapter.isEnabled) {
            toast("Please turn on Bluetooth first")
            return
        }
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
                    scannedWifi.add(WifiData(it.BSSID,it.level.toString()))
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
            scannedBle.add(BleData(it.mac,it.rssi.toString()))
        }
        logE("BLE data $scannedBle")
    }


    override fun onMapReady(map: GoogleMap) {
        mapView=map
        mapView?.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(25.0119275,121.5414292),19f))
        mapView?.setOnMapLongClickListener(GoogleMap.OnMapLongClickListener {
            if(pinMarker!=null){
                pinMarker?.remove()
            }
            pinMarker = mapView?.addMarker(MarkerOptions()
                .position(it).title("Dropped Pin"))
            val sheetView = LayoutDialogBinding.inflate(layoutInflater)
            curLatLng= LatLng(it.latitude,it.longitude)
            val dialog=Dialog(this)
            dialog.setContentView(sheetView.root)
            sheetView.tvLatLng.text = "Current Lat/Lng : ${it.latitude}, ${it.longitude}"
            sheetView.tvTime.text=Calendar.getInstance().time.toString()
            sheetView.cbBleMode.setOnCheckedChangeListener { _, b ->
                bleMode=b
            }
            sheetView.cbWifiMode.setOnCheckedChangeListener { _, b ->
                wifiMode=b
            }
            sheetView.btnStart.setOnClickListener {
                fileName="${sheetView.tvTime.text}_${binding.spinnerFloor.selectedItem.toString()}.csv"
                dataSets.clear()
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
                if (wifiMode){
                    unregisterReceiver(wifiScanReceiver)
                }
                if(bleMode){
                    isScanning=!isScanning
                    bleManager.cancelScan()
                }
                sheetView.btnStart.toVisible()
                sheetView.btnStop.toGone()
                sensorStopListening()
                writingFile()
                dialog.setCancelable(true)
            }
            dialog.show()
        })
        drawFloor()
    }
    private fun drawFloor(){
        mapView?.clear()
        floorOverlay=GroundOverlayOptions()
            .image(BitmapDescriptorFactory.fromResource(floors[selectedBuilding][selectedFloor]))
            .anchor(1f,1f)
            .position(southEast[selectedBuilding],buildingWH[selectedBuilding][0],buildingWH[selectedBuilding][1])
            .bearing(selectedBearing)
        mapView?.addGroundOverlay(floorOverlay)

    }

    override fun onMarkerClick(p0: Marker): Boolean {
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
        logE("add data$scannedBle")
        logE("add data $scannedWifi")
        dataSets
            .add(DataSet(Calendar.getInstance().time.toString(),
                curLatLng.latitude,curLatLng.longitude,
            scannedBle.toMutableList(), scannedWifi.toMutableList(),currentGeo,currentAccel,currentGyro))
        if(isScanning) {
            if (bleMode) {
                readBle()
            }else if(wifiMode){
                wifiManger.startScan()
            }
        }
        resetScanned()
        Log.e("dataset collected",dataSets.size.toString())
    }
    private fun resetScanned(){
        scannedBle.clear()
        scannedWifi.clear()
//        fixedMAC.forEach {
//            scannedBle.add(BleData(it,"100"))
//        }
    }

}