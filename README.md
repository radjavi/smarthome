# Smart Home - Raspberry Pi 3 Project

A server made in Node reads the GPIO ports in use and emits values to connected clients. A web interface or an Android app can connect to the server and read the values. As of now, only a temperature sensor has been connected to the pi.

The Node server uses Express, sockets.io and http-auth.
