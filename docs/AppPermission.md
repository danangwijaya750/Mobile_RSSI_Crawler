![](https://i.imgur.com/JORnn3y.png =150x)@NTUST
# Mobile device sensors and hardware permissions
## Introduction
App permissions help support **user privacy** by **protecting access** to the following:
- Restricted data, such as system state and users' contact information
- Restricted actions, such as connecting to a paired device and recording audio.

## Used Sensors and Hardware
To perform RSSI data crawl the mobile phone must have the following features:

| No  | Sensor and Hardware        | Function                                                             |
| --- | -------------------------- | -------------------------------------------------------------------- |
| 1.  | Wi-Fi                      | Perform the Wifi device features                                     |
| 2.  | Bluetooth                  | Perfrom the Bluetooth device features                                |
| 3.  | GPS Location               | Perfrom the GPS features (Required for BLE Scanning)                 |
| 4.  | Storage                    | Perfrom the read and write to device storage                         |
| 5.  | High Sampling Rate Sensors | Perfrom the high sampling rate sensors (Gyro, Accelero, Geomagnetic) |

## Required App Permission
According to [Official Android Docs](https://developer.android.com/training/permissions/declaring) App Permissions are declared on [AndroidManifest.xml](https://github.com/danangwijaya750/Mobile_RSSI_Crawler/blob/main/app/src/main/AndroidManifest.xml) file.
Android Manifest Permission [Docs](https://developer.android.com/reference/android/Manifest.permission)
| Devices                                                                                    | No  | Permission                                                                                                                                      |
| ------------------------------------------------------------------------------------------ | --- | ----------------------------------------------------------------------------------------------------------------------------------------------- |
| [Wi-Fi](https://developer.android.com/guide/topics/connectivity/wifi-scan)                 | 1.  | [android.permission.ACCESS_WIFI_STATE](https://developer.android.com/reference/android/Manifest.permission#ACCESS_WIFI_STATE)                   |
|                                                                                            | 2.  | [android.permission.CHANGE_WIFI_STATE](https://developer.android.com/reference/android/Manifest.permission#CHANGE_WIFI_STATE)                   |
| [Bluetooth](https://developer.android.com/guide/topics/connectivity/bluetooth/permissions) | 1.  | [android.permission.BLUETOOTH](https://developer.android.com/reference/android/Manifest.permission#BLUETOOTH)                                   |
|                                                                                            | 2.  | [android.permission.BLUETOOTH_SCAN](https://developer.android.com/reference/android/Manifest.permission#BLUETOOTH_SCAN)                         |
|                                                                                            | 3.  | [android.permission.BLUETOOTH_ADMIN](https://developer.android.com/reference/android/Manifest.permission#BLUETOOTH_ADMIN)                       |
| [GPS](https://developer.android.com/training/location/permissions)                         | 1.  | [android.permission.ACCESS_COARSE_LOCATION](https://developer.android.com/reference/android/Manifest.permission#ACCESS_COARSE_LOCATION)         |
|                                                                                            | 2.  | [android.permission.ACCESS_FINE_LOCATION](https://developer.android.com/reference/android/Manifest.permission#ACCESS_FINE_LOCATION)             |
| [Sensors](https://developer.android.com/guide/topics/sensors/sensors_overview)             | 1.  | [android.permission.HIGH_SAMPLING_RATE_SENSORS](https://developer.android.com/reference/android/Manifest.permission#HIGH_SAMPLING_RATE_SENSORS) |
| [Device Storage](https://developer.android.com/training/data-storage)                      | 1.  | [android.permission.READ_EXTERNAL_STORAGE](https://developer.android.com/reference/android/Manifest.permission#READ_EXTERNAL_STORAGE)           |
|                                                                                            | 2.  | [android.permission.WRITE_EXTERNAL_STORAGE](https://developer.android.com/reference/android/Manifest.permission#WRITE_EXTERNAL_STORAGE)     |

## Setup Permission Request
The App permission request is handled by `checkPermission()` method in [MainActivity.kt](https://github.com/danangwijaya750/Mobile_RSSI_Crawler/blob/main/app/src/main/java/com/dngwjy/datasetcollector/MainActivity.kt#L133).
in this method is to check all permission that declared in [AndroidManifest.xml](https://github.com/danangwijaya750/Mobile_RSSI_Crawler/blob/main/app/src/main/AndroidManifest.xml) is granted or not.
![](https://media.giphy.com/media/v1.Y2lkPTc5MGI3NjExMWZmYTdkMDM1ZmIzM2RiYjc0ZTM2ZDU3ZTg5MmExMjkyYzAwYzc0MCZlcD12MV9pbnRlcm5hbF9naWZzX2dpZklkJmN0PWc/eKfCdW4SrtGCcF1sZu/giphy.gif)

