var notif = require("./notifications");

// LIRC - For the IR transceiver
lirc = require('lirc_node');
lirc.init();

// Console-stamp
require('console-stamp')(console, {
    metadata: function () {
        return ('[' + process.memoryUsage().rss + ']');
    },
    colors: {
        stamp: 'yellow',
        label: 'white',
        metadata: 'green'
    }
});

// Firebase Admin
var admin = require('firebase-admin');
var serviceAccount = require('./smart-home-24c6a-firebase-adminsdk-pgkqk-6137da7b86.json');
admin.initializeApp({
  credential: admin.credential.cert(serviceAccount),
  databaseURL: 'https://smart-home-24c6a.firebaseio.com/'
});
// Get database references
var db = admin.database();
var connectedRef = db.ref(".info/connected");
var piOnline = db.ref().child("piOnline");
var alarmRef = db.ref().child("alarm");
var ledRef = db.ref().child("led");
var tempSensorRef = db.ref().child("tempSensor");
piOnline.set(true);
piOnline.onDisconnect().set(false);
connectedRef.on("value", function(snap) {
  if (snap.val() === true) {
    piOnline.set(true);
    console.log("Connected to Firebase!");
  } else {
    console.log("Connection to Firebase lost!");
  }
});

var rpio = require('rpio');
rpio.init({gpiomem: false});
rpio.open(7, rpio.OUTPUT, 0); // Green LED
rpio.open(11, rpio.INPUT, rpio.PULL_UP); // TempSensor
rpio.open(12, rpio.INPUT); // Motion Detector
rpio.open(13, rpio.OUTPUT, 0); // Alarm LED
rpio.open(15, rpio.OUTPUT, 0); // Buzzer

// ----- Global variables -----
var ledOn = false;
var temperature = 0;
var humidity = 0;
var alarmOn = false;

// Update values on change
ledRef.on("value", function(snap) {
  ledOn = snap.val().ledOn;
  lirc.irsend.send_once("rgb", snap.val().rgbCode);
});
alarmRef.on("value", function(snap) {
  if (alarmOn != snap.val()) {
    alarmOn = snap.val();
    if (alarmOn) { rpio.write(13, 1); buzzer(); console.log("Alarm has been activated."); }
    else         { rpio.write(13, 0); buzzer(); console.log("Alarm has been deactivated."); }
  }
});

function buzzer() {
  for (var i=0; i<3; i++) {
      rpio.write(15, 1);
      rpio.msleep(50);
      rpio.write(15, 0);
      rpio.msleep(50);
  }
}

function buzzer2() {
    rpio.write(15,1);
    rpio.msleep(100);
    rpio.write(15,0);
}

// GPIO events
function pollcb(cbpin) {
    //console.log('Pin ' + cbpin + ' value is now ' + rpio.read(cbpin));

    // Motion sensor //
    if (cbpin == 12 && alarmOn) {
        console.log("Motion Detected!");
        // Send a message to devices subscribed to the provided topic.
        sendNotification(notif.motionMsg);
    }
}
rpio.poll(12, pollcb, rpio.POLL_HIGH); // Motion Detector

// Temp. sensor
var rpiDhtSensor = require('rpi-dht-sensor');
var dht = new rpiDhtSensor.DHT11(17);
var tempSent = false; // Check if temperature notification sent (over 30 Celsius)

function read() {
    var readout = dht.read();
    temperature = readout.temperature;
    humidity = readout.humidity;
    tempSensorRef.set({
      temp: temperature,
      humid: humidity
    });
    if (temperature == 32 && !tempSent) {
        console.log("Temperature is 32 degrees!");
        sendNotification(notif.tempMsg);
        tempSent = true;
    }
    if (temperature < 31 && tempSent) tempSent = false;
}
setInterval(read, 5000);

function sendNotification(message) {
    admin.messaging().send(message)
    .then((response) => {
        // Response is a message ID string.
        console.log('Successfully sent message:', response);
    })
    .catch((error) => {
        console.log('Error sending message:', error);
    });
}

// RFID Reader
var rc522 = require("rc522");
var tags = require("./rfid_tags");

rc522(function(rfidSerialNumber){
	if (rfidSerialNumber == tags.tag_iman) {
        alarmRef.set(!alarmOn);
    }
    else { console.log("Unauthorized tag!"); buzzer2(); }
});

console.log("Smart Home is online!");
