# Android-based Crawler
## Introduction
This project involves the development of a mobile Android app for RSSI data crawling. The app will be designed to collect RSSI (Received Signal Strength Indicator) data from Wi-Fi networks, Bluetooth devices, and other wireless devices. The data will be used to create maps and track the location of devices within a given area.
## [User Interface](https://hackmd.io/@danangwijaya750/UserInterface)
<img src="https://i.imgur.com/uuIkR0d.png" width="30%">
<img src="https://i.imgur.com/hhngQj5.png" width="30%">

## Dependencies
1. [Google Maps SDK](https://developers.google.com/maps/documentation/android-sdk/overview) This library is used to perform map control such as zoom-in-out, scroll up-down, etc.
2. [FastBle](https://github.com/Jasonchenlijian/FastBle) This library is to perfrom scanning BLE devices.
3. [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html) Coroutines is used to handle the async or non-blocking processes.
4. [Fuel](https://github.com/kittinunf/fuel) Fuel is HTTP Networking library for kotlin and baked by Kotlinx Coroutines.
5. [GSON](https://github.com/google/gson) Gson is a Java library that can be used to convert Java Objects into their JSON representation. It can also be used to convert a JSON string to an equivalent Java object.
## Setup
1. Clone the repo.
    ```
    git clone https://github.com/danangwijaya750/Mobile_RSSI_Crawler.git
    ```
2. Setup the Google Maps SDK. Because in this project we use Google Maps MapView that provided by Google Maps SDK and required an API Key.Follow this steps to [setup Maps SDK](https://developers.google.com/maps/documentation/android-sdk/start). If already have the API Key, add the API key to local.properties file like this:
    ```
    MAPS_API_KEY=hgjfkhg5764317698315768549027nfdsngadf
    ```
3. Perfom Clean Build
    ```
    ./gradlew clean
    ```
## App Components :
1. [Device sensors and hardware permissions]()
2. [Wi-Fi and BLE Listener Functions]()
3. [RSSI Data Structure]() 
4. [MapView and Floor Plan]()
5. [Convert crawled data to .csv format]()

## App Demo :
## Crawling Method :
## Crawling Result :
## Data Cleansing :
## Data Preprocessing :
## Model Training :


