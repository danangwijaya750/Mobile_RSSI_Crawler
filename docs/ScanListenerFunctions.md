# Wi-Fi and BLE Listener Functions
## Introduction
Wi-Fi and BLE RSSI is scanned using Listener Function that provided by Android API.

## Wi-Fi Listener
Wi-Fi scanning capabilities provided by the [WifiManager API](https://developer.android.com/reference/android/net/wifi/WifiManager) to get a list of Wi-Fi access points that are visible from the device.
There are three steps to the scanning process:

1. **Register a broadcast listener** for [SCAN_RESULTS_AVAILABLE_ACTION](https://developer.android.com/reference/android/net/wifi/WifiManager#SCAN_RESULTS_AVAILABLE_ACTION), which is called when scan requests are completed, providing their success/failure status. For devices running Android 10 (API level 29) and higher, this broadcast will be sent for any full Wi-Fi scan performed on the device by the platform or other apps. Apps can passively listen to all scan completions on device by using the broadcast without issuing a scan of their own.
   For broadcast listener is handled by `wifiScanReceiver` as BroadcastReceiver in [MainActivity.kt](https://github.com/danangwijaya750/Mobile_RSSI_Crawler/blob/main/app/src/main/java/com/dngwjy/datasetcollector/MainActivity.kt#L242).
   `wifiScanReceiver` is registered as to listen the `WifiManager.SCAN_RESULTS_AVAILABLE_ACTION` [MainActivity.kt](https://github.com/danangwijaya750/Mobile_RSSI_Crawler/blob/main/app/src/main/java/com/dngwjy/datasetcollector/MainActivity.kt#L374)
2. **Request a scan** using `WifiManager.startScan()` on [MainActivity.kt](https://github.com/danangwijaya750/Mobile_RSSI_Crawler/blob/main/app/src/main/java/com/dngwjy/datasetcollector/MainActivity.kt#L375). Check the return status of the method, since the call may fail for any of the following reasons:
    - Scan requests may be throttled because of too many scans in a short time.
    - The device is idle and scanning is disabled.
    - Wi-Fi hardware reports a scan failure.
3. **Get scan results** using `WifiManager.getScanResults()` on [MainActivity.kt](https://github.com/danangwijaya750/Mobile_RSSI_Crawler/blob/main/app/src/main/java/com/dngwjy/datasetcollector/MainActivity.kt#L244). The returned scan results are the most recently updated results, which may be from a previous scan if your current scan has not completed or succeeded. This means that you might get older scan results if you call this method before receiving a successful [SCAN_RESULTS_AVAILABLE_ACTION](https://developer.android.com/reference/android/net/wifi/WifiManager#SCAN_RESULTS_AVAILABLE_ACTION) broadcast.

## BLE Listener
For ble scanning use 3rd party library [FastBle](https://github.com/Jasonchenlijian/FastBle).
### FastBle Setup
1. Add Maven Repos to root build.gradle ([Project Level](https://github.com/danangwijaya750/Mobile_RSSI_Crawler/blob/main/build.gradle#L5))
    ```
    repositories {
        ...
        mavenCentral()
    }
      ```
3. Add Deps to build.gradle ([Module Level](https://github.com/danangwijaya750/Mobile_RSSI_Crawler/blob/main/app/build.gradle#L49))
     ```
    dependencies {
          implementation 'com.github.Jasonchenlijian:FastBle:2.4.0'
      }
    å```
### Usage
1. **Init**
   Init the FastBle Library `BleManager` object to `bleManager` variable to perform BLE scanning on [MainActivity.kt](https://github.com/danangwijaya750/Mobile_RSSI_Crawler/blob/main/app/src/main/java/com/dngwjy/datasetcollector/MainActivity.kt#L202)
2. **BLE Scanner Callback**
   The `bleManager` needs `BleScanCallback` as params in `BleManager.scan()` method to perfrom and listening result of BLE scanning. `BleScanCallback` declared as `bleListener` variable on [MainActivity.kt](https://github.com/danangwijaya750/Mobile_RSSI_Crawler/blob/main/app/src/main/java/com/dngwjy/datasetcollector/MainActivity.kt#L305). `BleScanCallback` implements 4 methods to listen the scanning process:
    - `onScanStarted()`
    - `onLeScan()`
    - `onScanning()`
    - `onScannFinished()`