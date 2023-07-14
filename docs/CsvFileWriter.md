# Convert crawled data to .csv format
## Introduction
The results of the crawled data will be stored in .csv file.
## FileWriter.kt
This code is collects the data sets and writes them to a file. Here is a summary of what the code does:
1. The code defines a class called [`FileWriter.kt`]() with a constructor that takes a `Context` object as a parameter. This class is responsible for writing data sets to a file.
2. The [`writeToFile`]() function within the FileWriter class is used to write data sets to a file. It takes a list of `DataSet` objects and a title as parameters and returns a `string` representing the absolute path of the exported file.
3. The function begins by obtaining the external files directory using the [`getExternalFilesDir()`]() method of the provided `Context` object. This directory is where the file will be stored.
4. Next, it checks if the directory specified by the `uri` variable exists. If it doesn't exist, it creates the directory using the `mkdirs()` method. This ensures that the directory is available for file storage.
5. The code then creates a File object named `exportedFile` using the `uri` and the provided title. This represents the file that will be written to.
6. It opens a [`PrintWriter`]() for the `exportedFile` using the [`printWriter()`]() method. The use function ensures that the writer is closed properly after writing.
7. The code enters a **loop** that iterates over each `DataSet` object in the provided list. Within the loop, the code initializes empty strings **bles**, **wifis**, **accels**, **gyros**, and **geos** to store the concatenated values of respective data fields.
9. For each `DataSet` object, it iterates over the **bles**, **wifis**, **accel**, **gyro**, and **geomagnetic** lists and concatenates their values into the corresponding string variables (bles, wifis, accels, gyros, and geos).
10. It then writes a formatted line to the file using the `println()` method of the `PrintWriter` object. The line includes the **timestamp**, **latitude**, **longitude**, **concatenated strings** of bles, wifis, accels, geos, and gyros, separated by **semicolons**.
11. The loop continues until all `DataSet` objects have been processed and written to the file.
12. Finally, the function returns the **absolute path** of the exported file using the `absolutePath` property of the `exportedFile` object.

