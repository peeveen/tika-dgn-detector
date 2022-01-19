# tika-dgn-detector
An Apache Tika Detector (not a Parser!) for MicroStation DGN drawings (v7 &amp; v8)

Detects v7 DGNs using a simple magic string detection in the opening bytes of the files.

Detects v8 DGNs by determining if the file is a Microsoft Compound Document, reading the contents using Apache POI, and
identifying a set of common filenames within.

# How to get it

Available from Maven Central
```
implementation("com.github.peeveen:tika-dgn-detector:0.4")
```
