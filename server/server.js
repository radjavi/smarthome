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
var piOnline = db.ref("piOnline");
var alarmRef = db.ref("alarm");
var ledRef = db.ref("led");
var tempSensorRef = db.ref("tempSensor");
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
        var str = "Motion Detected!";
        console.log(str);
        // Send a message to devices subscribed to the provided topic.
        sendNotification(str, getDate());
    }
}
rpio.poll(12, pollcb, rpio.POLL_HIGH); // Motion Detector

// Temp. sensor
var rpiDhtSensor = require('rpi-dht-sensor');
var dht = new rpiDhtSensor.DHT11(17);
var tempSent = false; // Check if temperature notification sent (over 30 Celsius)

function getDate() {
  var currentdate = new Date();
  var datetime =  currentdate.getDate() + "/"
                  + (currentdate.getMonth()+1)  + "/"
                  + currentdate.getFullYear() + " - "
                  + ('0' + currentdate.getHours()).slice(-2) + ":"
                  + ('0' + currentdate.getMinutes()).slice(-2) + ":"
                  + ('0' + currentdate.getSeconds()).slice(-2);
  return datetime;
}

function setRef(temp, humid) {
  var minTemp;
  var maxTemp;
  var minHumid;
  var maxHumid;
  var date = getDate();

  tempSensorRef.once("value", function(data) {
    minTemp = data.val().minTemp;
    maxTemp = data.val().maxTemp;
    minHumid = data.val().minHumid;
    maxHumid = data.val().maxHumid;

    if (temp > maxTemp) maxTemp = temp;
    if (temp < minTemp && temp != 0) minTemp = temp;
    if (humid > maxHumid) maxHumid = humid;
    if (humid < minHumid && humid != 0) minHumid = humid;

    tempSensorRef.set({
      temp: temp,
      humid: humid,
      minTemp: minTemp,
      maxTemp: maxTemp,
      minHumid: minHumid,
      maxHumid: maxHumid,
      date: date
    });
  });
}

function read() {
    var readout = dht.read();
    temperature = readout.temperature;
    humidity = readout.humidity;
    setRef(temperature, humidity);
    if (temperature >= 32 && !tempSent) {
        var str = "Temperature is " + temperature + "°C";
        console.log(str);
        sendNotification(str, getDate());
        tempSent = true;
    }
    else if (temperature >= 50) {
        var str = "Temperature is " + temperature + "°C";
        console.log(str);
        sendNotification(str, getDate());
    }
    else if (temperature >= 40) {
        var str = "Temperature is " + temperature + "°C";
        console.log(str);
        sendNotification(str, getDate());
    }
    if (temperature < 31 && tempSent) tempSent = false;
}
setInterval(read, 5000);

function sendNotification(title, body) {
    var message = notif.msg;
    message.android.notification.title = title;
    message.android.notification.body = body;
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
