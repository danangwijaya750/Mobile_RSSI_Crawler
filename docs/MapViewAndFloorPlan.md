# MapView Floor Plan
## Introduction
Floor plan is an **image file** draw **on top** of Google Map **MapView**.\
![](https://media.giphy.com/media/v1.Y2lkPTc5MGI3NjExMmU3ZGZkN2ZmODk3ZTA0ZGNiYjdiN2FlNDg3Yjc4NDRlZGQ5MWE3MCZlcD12MV9pbnRlcm5hbF9naWZzX2dpZklkJmN0PWc/Edv2GwDxpbzT0qcyA0/giphy.gif)

## UNY Electrical and Electronics Engineering Building
- **Building Location** : [Electrical and Electronics Engineering Labs](https://goo.gl/maps/UZBV9hDWD6TCVf3s5) this building has **3 floors**. the first floor is for Electrical Engineering Department. second and third floor is for Electronics and Informatics Engineering Department.
- **Building Image** :


| Map View                                      | Satelite View                                 |
| --------------------------------------------- | --------------------------------------------- |
| ![](https://hackmd.io/_uploads/SkWBypuVn.png) | ![](https://hackmd.io/_uploads/Sy5Oy6O42.png) |

## Floor Plan Drawer
Floor Plan Image :
| 1st Floor Plan                                | 2nd Floor Plan                                | 3rd Floor Plan                                |
| --------------------------------------------- | --------------------------------------------- | --------------------------------------------- |
| ![](https://hackmd.io/_uploads/Hynh0k5Eh.jpg) | ![](https://hackmd.io/_uploads/Sy_pC1cV3.jpg) | ![](https://hackmd.io/_uploads/H1RpR19Eh.jpg) |

Floor plan is draw **on top** of Google Map MapView. Floor plan image is store in android resource folder **[(res/drawable-v24)](https://github.com/danangwijaya750/Mobile_RSSI_Crawler/tree/main/app/src/main/res/drawable-v24)**.
Floor plan drawer in handled by **`drawFloor()`** method on [MainActivity.kt](https://github.com/danangwijaya750/Mobile_RSSI_Crawler/blob/main/app/src/main/java/com/dngwjy/datasetcollector/MainActivity.kt#L398). To draw an image to the MapView using the Ground Overlays from **[`GroundOverlay`](https://developers.google.com/maps/documentation/android-sdk/groundoverlay)** class. Ground Overlays are **image overlays** that are **tied to latitude/longitude coordinates**, so they move when drag or zoom the map. The `drawFloor()` method is called every user change the **[Building or Floor Selector](https://hackmd.io/@danangwijaya750/UserInterface#Components)** and once MapView has **loaded**.
## Building Geolocation Anchor
Building Geolocation anchor for EEE building is **[(-7.7711140765450395, 110.38687646895046)](https://goo.gl/maps/UqWQzfcJUq8gRqme6)** **(still have to make sure for the location of these coordinates directly to the location)**.  This Coordinate is the **South-East corner** of the building and used **to tied** the floor plan overlay to MapView.


| Before Draw | After Draw |
| ----------- | ---------- |
| ![](https://hackmd.io/_uploads/BySlazcEn.png)        |  ![](https://hackmd.io/_uploads/rJ8NRGcE2.png)|



## Convert Geolocation (Latitude, Longitude) to plan format (x,y)
This step is to **convert** the **Geolocation coordinates** to **plan format (x,y)**. This conversion process is intended if deep learning will predict locations using the **plan format (x,y) as the ground truth**. The conversion process will be carried out during the **data preprocessing** before the model training process.
To convert the geolocation to plan format (x,y) is using **Equirectangular Projection** with combination references area using another 2 points, because Equirectangular Projection will return the X and Y positions related to **the globe (or the entire map)**, this means that will get global positions, so we related the projection point to another 2 points, getting the **global positions** and **relating to local** (on screen or floor plan) positions. Once got the global reference area in lat and lng, we do the same for screen positions/floor plan image.




