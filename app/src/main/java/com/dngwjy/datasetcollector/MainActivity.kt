package com.dngwjy.datasetcollector

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.LocationManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.AdapterView
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
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
    private val fixedMAC= mutableListOf<String>()

    private lateinit var binding: ActivityMainBinding
    private var mapView:GoogleMap?=null
    private val northEast=LatLng(25.01208680666584, 121.541765599863)
    private val northWest=LatLng(25.01219375599751, 121.54165294708444)
    private val southWest=LatLng(25.011707093458693, 121.5410867174273)
    private val southEast=LatLng(25.011595368321558, 121.5412002293108)
    private var pinMarker:Marker?=null
    private lateinit var floorOverlay: GroundOverlayOptions
    private var sensorManager: SensorManager? = null
    private var geomagneticSensor:Sensor?=null
    private var accelSensor:Sensor?=null
    private var gyroSensor:Sensor?=null
    private var pressureSensor:Sensor?=null
    private var isCollecting=false
    private val floors= arrayOf(R.drawable.onef1_full,R.drawable.sixf6_full,R.drawable.sevenf7_full,R.drawable.eightf8_full)
    private var selectedFloor=0
    private val currentGeo= mutableListOf<Float>()
    private val currentAccel= mutableListOf<Float>()
    private val currentGyro= mutableListOf<Float>()
    private val scannedBle= mutableListOf<BleData>()
    private var isScanning=false
    private lateinit var bleManager:BleManager
    private var curLatLng=LatLng(0.0,0.0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val mf=supportFragmentManager.findFragmentById(R.id.maps_view)
        as SupportMapFragment
        mf.getMapAsync(this)
        initSensor()
        checkPermissions()
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
    }

    private fun checkPermissions() {
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (!bluetoothAdapter.isEnabled) {
            toast("Please turn on Bluetooth first")
            return
        }

        val permissions = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE)
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

    private val geoListener = object:SensorEventListener{
        override fun onSensorChanged(event: SensorEvent) {
            Log.e("Geo", "onSensorChanged: ${event.values.size}")
            currentGeo.clear()
            currentGeo.addAll(event.values.toList())
        }

        override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
        }
    }
    private val accelListener= object:SensorEventListener{
        override fun onSensorChanged(event: SensorEvent) {
            Log.e("Accel", "onSensorChanged: ${event.values.size}")
            currentAccel.clear()
            currentAccel.addAll(event.values.toList())
        }

        override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
        }
    }
    private val gyroListener=object:SensorEventListener{
        override fun onSensorChanged(event: SensorEvent) {
            Log.e("Gyro", "onSensorChanged: ${event.values.size}")
            currentGyro.clear()
            currentGyro.addAll(event.values.toList())
        }

        override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
        }
    }
    private val pressureListener=object:SensorEventListener{
        override fun onSensorChanged(event: SensorEvent) {
            Log.e("Pressure", "onSensorChanged: ${event.values.size}")
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
            if(isScanning){
                Log.e("ble","rescan")
                bleManager.scan(this)
            }
        }
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
            val dialog = BottomSheetDialog(this)
            dialog.setContentView(sheetView.root)
            sheetView.tvLatLng.text = "Current Lat/Lng : ${it.latitude}, ${it.latitude}"
            sheetView.tvTime.text=Calendar.getInstance().time.toString()
            sheetView.btnStart.setOnClickListener {
                isCollecting=!isCollecting
                sheetView.btnStart.toGone()
                sheetView.btnStop.toVisible()
                sensorStartListening()
                isScanning=!isScanning
                scanning()
            }
            sheetView.btnStop.setOnClickListener {
                isCollecting=!isCollecting
                sheetView.btnStart.toVisible()
                sheetView.btnStop.toGone()
                isScanning=!isScanning
                bleManager.cancelScan()
                sensorStopListening()
            }
            dialog.show()
        })
        drawFloor()
    }
    private fun drawFloor(){
        mapView?.clear()
        floorOverlay=GroundOverlayOptions()
            .image(BitmapDescriptorFactory.fromResource(floors[selectedFloor]))
            .anchor(1f,1f)
            .position(southEast,18f,80f)
            .bearing(47f)
        mapView?.addGroundOverlay(floorOverlay)

    }

    override fun onMarkerClick(p0: Marker): Boolean {
        return false
    }

    override fun onSensorChanged(event: SensorEvent) {

    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {

    }


}