var notif = require("./notifications");
var express = require('express'); // Web server
app = express();
server = require('http').createServer(app);
io = require('socket.io').listen(server); // Web socket server
io.set('heartbeat timeout', 4000); 
io.set('heartbeat interval', 2000);

// LIRC - For the IR transceiver
lirc = require('lirc_node');
lirc.init();

// Authentication module. 
var auth = require('http-auth');
var digest = auth.digest({
    realm: "Users",
    file: __dirname + "/.htdigest"
});

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

app.use(auth.connect(digest)); // Authenticate
app.use(express.static('public')); // Tell the server that ./public/ contains the static webpages

server.listen(8080, function(){
    console.log("Server is running at port 8080");
}); // Start the webserver on port 8080

io.on('connection', function(socket){
    console.log("New client connected: " + socket.id);
    socket.on('disconnect', function(){
        console.log("Client disconnected: " + socket.id);
    });
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

// ----- On client connection -----
io.sockets.on('connection', function(socket) { // Gets called whenever a client connects
    socket.emit('led', {value: ledOn}); // Send the new client the current brightness
    socket.emit('tempSensor', {value: temperature}, {value: humidity});
    socket.emit('alarm', {value: alarmOn});
    socket.on('led', function(data) { // Makes the socket react to 'led' packets by calling this function
        var rgbCode = data.value;
        switch (rgbCode) {
            case "BTN_ON":
                ledOn = true; 
                //rpio.write(7, 1); 
                lirc.irsend.send_once("rgb", rgbCode);
                break;
            case "BTN_OFF":
                ledOn = false;
                //rpio.write(7, 0); 
                lirc.irsend.send_once("rgb", rgbCode);
                break;
            default:
                lirc.irsend.send_once("rgb", rgbCode);
                break;
        }
        io.sockets.emit('led', {value: ledOn}); // Sends the updated ledOn value to all connected clients
    });
    socket.on('alarm', function(data) {
        alarmOn = data.value;
        if (alarmOn) { rpio.write(13, 1); buzzer(); console.log("Alarm has been activated."); }
        else         { rpio.write(13, 0); buzzer(); console.log("Alarm has been deactivated."); }
        io.sockets.emit('alarm', {value: alarmOn});
    });
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
    io.sockets.emit('tempSensor', {value: temperature}, {value: humidity});
    if (temperature == 30 && !tempSent) {
        sendNotification(notif.tempMsg);
        tempSent = true;
    }
    if (temperature < 30 && tempSent) tempSent = false;
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
        alarmOn = !alarmOn;
        if (alarmOn) { rpio.write(13, 1); console.log("Alarm has been activated by Iman (RFID)"); buzzer(); }
        else         { rpio.write(13, 0); console.log("Alarm has been deactivated by Iman (RFID)"); buzzer(); }
        io.sockets.emit('alarm', {value: alarmOn});
    }
    else { console.log("Unauthorized tag!"); buzzer2(); }
});