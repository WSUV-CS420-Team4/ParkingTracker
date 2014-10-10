ParkingTracker
==============

An android application for tracking parking usage within a neighborhood.


To build you must use the https://github.com/rmtheis/tess-two TessTwo library. Follow their instructions but make sure you have Android NDK r9d and not r10b. Making sure ndk-build, android, and ant are all on the PATH enviroment variable will help get everything installed. After you build TessTwo, set it as a library in eclipse and then link it to your ParkingTracker app as a library.

You will be required to have the Android-14 sdk as well as the google play services library.