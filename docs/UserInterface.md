# User Interface
The User Interface layout was build using **XML layout files** in the [res/layout](https://github.com/danangwijaya750/Mobile_RSSI_Crawler/tree/main/app/src/main/res/layout) folder.\
<img src="https://i.imgur.com/uuIkR0d.png" width="30%"><img src="https://i.imgur.com/hhngQj5.png" width="30%">


## Components
The app only have 1 Activity that contain several components :
- **Building and Floor plan selector (Spinner)**\
  ![](https://i.imgur.com/7gA2iAn.png)
1. User can **select** the **building and floor plan** where the data crawled. this component is located in [activity_main.xml](https://github.com/danangwijaya750/Mobile_RSSI_Crawler/blob/main/app/src/main/res/layout/activity_main.xml#L21).
2. **Building and Floor plan** data is still **hardcoded** in the resource [strings.xml](https://github.com/danangwijaya750/Mobile_RSSI_Crawler/blob/main/app/src/main/res/values/strings.xml#L14) file.The **buildings** data is populate from **resource** file using the built-in method from xml on [activity_main.xml](https://github.com/danangwijaya750/Mobile_RSSI_Crawler/blob/main/app/src/main/res/layout/activity_main.xml#L36).
3. Method to handle if **user change** the value of **building selector** it will change the **floor selector items** on [MainActivity.kt](https://github.com/danangwijaya750/Mobile_RSSI_Crawler/blob/main/app/src/main/java/com/dngwjy/datasetcollector/MainActivity.kt#L92)
4. App will draw the selected floor plan on Google Maps MapView.

- **Data Collector Dialog**
  The dialog layout is **separated** on [layout_dialog.xml](https://github.com/danangwijaya750/Mobile_RSSI_Crawler/blob/main/app/src/main/res/layout/layout_dialog.xml) file.\
  ![](https://i.imgur.com/3XM2iIX.png) \
  This layout will inflated to **MainActivity** with [**AlertDialog**](https://developer.android.com/reference/android/app/AlertDialog) using **setContentView()** method. to inflate the layout is in [MainActivity.kt](https://github.com/danangwijaya750/Mobile_RSSI_Crawler/blob/main/app/src/main/java/com/dngwjy/datasetcollector/MainActivity.kt#L349).

- **Google Maps MapView**
  MapView is use to perform **map control** such as zooming, scrolling, and pin point for help data crawling process to get the several information about the building like **Geopoint (Latitude, Longitude)**, **anchor**, **building size**, etc. On MapView can also **draw** the floor plan image **above** the MapView layer.
  Google Maps MapView is provided by **Google Maps SDK** and required an **API Key**.
  Follow this steps to [setup Maps SDK](https://developers.google.com/maps/documentation/android-sdk/start). If already have the API Key, add the API key to local.properties file like this:
    ```
    MAPS_API_KEY=hgjfkhg5764317698315768549027nfdsngadf
    ```
 


