# RSSI Data Structure
## Introduction
The RSSI data structure is obtained from the combined results of the wifi and ble scans carried out by `WifiManger` and `BleManager`
## Wi-Fi Scan Result Data
Wi-Fi scan is handled by `WifiManager` class, to get the result of scanned Wi-Fi network is provided by [`WifiManager.getScanResults()`](https://developer.android.com/reference/android/net/wifi/WifiManager#getScanResults()) and return the results od the latest access point scan `List<ScanResult>`. the information of scanned access point is in [`ScanResult`](https://developer.android.com/reference/android/net/wifi/ScanResult) class that contain fields.\
![](https://hackmd.io/_uploads/rJLKNcD42.png)

## BLE Scan Result Data
BLE scan handled by `BleManager` class from FastBle Library. BleManager.scan() result is handled by BleScanCallBack in method `onScanFinished()` that have Collection of scanned BLE devices in `BleDevice` class that contains fields.


| (Data type) Field Name   | Function                      |
| ------------------------ | ----------------------------- |
| (String) mac             | BLE Device MAC Address        |
| (ByteArray) scanRecord   | BLE Device Scan Record        |
| (String) key             | BLE Device Key                |
| (String) name            | BLE Device Name               |
| (int) rssi               | BLE Device RSSI               |
| (BluetoothDevice) device | More information about Device |
| (long) timestampNanos    | BLE Scanned Time stamp        |

## RSSI Scan Data Structure Combination
- From [`ScanResult`](https://developer.android.com/reference/android/net/wifi/ScanResult) class  the BSSID and RSSI field are convert into `WifiData` data class in [`DataSet.kt`](https://github.com/danangwijaya750/Mobile_RSSI_Crawler/blob/main/app/src/main/java/com/dngwjy/datasetcollector/DataSet.kt#L7) file.
- From `BleDevice` class the mac and rssi field are converted into `BleData` data class in [`DataSet.kt`](https://github.com/danangwijaya750/Mobile_RSSI_Crawler/blob/main/app/src/main/java/com/dngwjy/datasetcollector/DataSet.kt#L3) file.

This scanning result combination data will combine again with the Geolocation (Latitude and Longitude) from Maps Pin Point and High sampling rate sensors.