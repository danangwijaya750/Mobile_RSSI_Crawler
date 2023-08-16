# Android-based Crawler
## Introduction
This project involves the development of a mobile Android app for RSSI data crawling. The app will be designed to collect RSSI (Received Signal Strength Indicator) data from Wi-Fi networks, Bluetooth devices, and other wireless devices. The data will be used to create maps and track the location of devices within a given area.
## [User Interface](https://github.com/danangwijaya750/Mobile_RSSI_Crawler/blob/main/docs/UserInterface.md)
<img src="https://i.imgur.com/uuIkR0d.png" width="30%"> <img src="https://i.imgur.com/hhngQj5.png" width="30%">

## Dependencies
1. [Google Maps SDK](https://developers.google.com/maps/documentation/android-sdk/overview) This library is used to perform map control such as zoom-in-out, scroll up-down, etc.
2. [FastBle](https://github.com/Jasonchenlijian/FastBle) This library is to perform scanning BLE devices.
3. [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html) Coroutines is used to handle the async or non-blocking processes.
4. [Fuel](https://github.com/kittinunf/fuel) Fuel is HTTP Networking library for kotlin and baked by Kotlinx Coroutines.
5. [GSON](https://github.com/google/gson) Gson is a Java library that can be used to convert Java Objects into their JSON representation. It can also be used to convert a JSON string to an equivalent Java object.

## Project Directory Structure
```bash
|__ app #Application module
|   |__ build #Module level build output directory
|   |__ libs #Any external libraries (.jar) file.
|   |__ src #Sourcecode of the application module
|   |   |__ main #main sourcecode
|   |   |__ test #Unit test code
|   |   |__ androidTest #Instrument test code
|__ docs #Markdown file for documentation
|__ build #Project level build output directory
|__ dokka #KDocs file
|__ scripts #Other gradle scripts
|__ gradle #gradle properties file
```

## Setup
1. Clone the repo.
    ```
    git clone https://github.com/danangwijaya750/Mobile_RSSI_Crawler.git
    ```
2. Setup the Google Maps SDK. Because in this project we use Google Maps MapView that provided by Google Maps SDK and required an API Key.Follow this steps to [setup Maps SDK](https://developers.google.com/maps/documentation/android-sdk/start). If already have the API Key, add the API key to local.properties file like this:
   ```
    MAPS_API_KEY=hgjfkhg5764317698315768549027nfdsngadf
   ```
3. Perform Clean Build
    ```
    ./gradlew clean
    ```

## App Components :
1. [Device sensors and hardware permissions](https://github.com/danangwijaya750/Mobile_RSSI_Crawler/blob/main/docs/AppPermission.md)
2. [Wi-Fi and BLE Listener Functions](https://github.com/danangwijaya750/Mobile_RSSI_Crawler/blob/main/docs/ScanListenerFunctions.md)
3. [RSSI Data Structure](https://github.com/danangwijaya750/Mobile_RSSI_Crawler/blob/main/docs/RSSIDataStructure.md) 
4. [MapView and Floor Plan](https://github.com/danangwijaya750/Mobile_RSSI_Crawler/blob/main/docs/MapViewAndFloorPlan.md)
5. [Convert crawled data to .csv format](https://github.com/danangwijaya750/Mobile_RSSI_Crawler/blob/main/docs/CsvFileWriter.md)

## App Demo :
[![IMAGE ALT TEXT HERE](https://img.youtube.com/vi/89Mc40a0X2o/0.jpg)](https://www.youtube.com/watch?v=89Mc40a0X2o)
## Crawling Method :
Crawling process using 4 android smartphones with specs:
- Xiaomi Redmi Note 8
- Nokia 5.4 
- Samsung Galaxy A22 
- Xiaomi Poco F3
## Crawling Result :
All crawled RSSI Raw data at UNY building are uploaded on [Google Drive](https://drive.google.com/drive/folders/1HhX77txKW5WwzeOSNy21TfO1D4Qw7fOa?usp=sharing)
## Data Cleansing :
Run the data cleansing on [Google Colab](https://colab.research.google.com/drive/1qpv0opKzdw02QrMlnmC5_ZeHsrlF7to3?usp=sharing)
## Data Preprocessing and Training :
Run the data preprocessing and training on [Google Colab](https://colab.research.google.com/drive/1XY_0tv3KCQmeVUGN1-bhtpu1Ugq4Dgit?usp=sharing)

## Licensee
This project is licensed under the GNU General Public License version 3 (GPL-3.0).

### Permissions
- You are free to use, modify, and distribute this Software.
- You can use the Software for both personal and commercial purposes.

### Conditions
- Any modifications or derivative works based on this Software must also be licensed under the GPL-3.0.
- You must include a copy of the GPL-3.0 license along with the Software.

### Limitations
- The Software comes with no warranties and no liability. You assume all risks in using the Software.

### Full License Text
The full text of the GNU General Public License version 3 can be found at:
[GPL-3.0 License](https://www.gnu.org/licenses/gpl-3.0.en.html)