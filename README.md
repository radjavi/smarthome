# Smart Home - Raspberry Pi 3 Project

A server made in Node communicates with the GPIO ports in use and emits values to connected clients. A web interface or an Android app can connect to the server and read/write to the ports. As of now, a temperature sensor, motion sensor and 3 LEDs have been connected to the pi.

The Node server uses Express, sockets.io, http-auth and firebase-admin.
