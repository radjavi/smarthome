var express = require('express'); // Web server
app = express();
server = require('http').createServer(app);
io = require('socket.io').listen(server); // Web socket server
io.set('heartbeat timeout', 4000); 
io.set('heartbeat interval', 2000);
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
// Authentication module. 
var auth = require('http-auth');
var digest = auth.digest({
    realm: "Users",
    file: __dirname + "/.htdigest"
});

app.use(auth.connect(digest)); // Authenticate
app.use(express.static('public')); // Tell the server that ./public/ contains the static webpages

// Firebase Admin
var admin = require('firebase-admin');
var serviceAccount = require('./smart-home-24c6a-firebase-adminsdk-pgkqk-6137da7b86.json');
admin.initializeApp({
  credential: admin.credential.cert(serviceAccount),
  databaseURL: 'https://smart-home-24c6a.firebaseio.com/'
});
// See documentation on defining a message payload.
var ledMessage = {
    android: {
      priority: 'high',
      notification: {
        title: 'Home',
        body: 'LED has been turned on!',
        sound: 'default'
      }
    },
    topic: 'all'
};

var motionMessage = {
    android: {
      priority: 'high',
      notification: {
        title: 'Home',
        body: 'Motion has been detected!',
        sound: 'default'
      }
    },
    topic: 'all'
};

server.listen(8080, function(){
    console.log("Server is running at port 8080");
}); // Start the webserver on port 8080

io.on('connection', function(socket){
    console.log("New client connected: " + socket.id);
    socket.on('disconnect', function(){
        console.log("Client disconnected: " + socket.id);
    });
});

var SerialPort = require("serialport");
var serialPort = new SerialPort("/dev/ttyAMA0", {baudRate: 115200});

var rpio = require('rpio');
rpio.init({gpiomem: false});
rpio.open(7, rpio.OUTPUT, 0); // LED
rpio.open(11, rpio.INPUT); // TempSensor
rpio.open(12, rpio.INPUT); // Motion Detector

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
        ledOn = data.value; // Updates brightness from the data object
        if (ledOn) rpio.write(7, 1);
        else rpio.write(7, 0);
        io.sockets.emit('led', {value: ledOn}); // Sends the updated brightness to all connected clients
    });
    socket.on('alarm', function(data) {
        alarmOn = data.value;
        io.sockets.emit('alarm', {value: alarmOn});
    });
});

// GPIO events
function pollcb(cbpin) {
    console.log('Pin ' + cbpin + ' value is now ' + rpio.read(cbpin));
    if (cbpin == 12 && alarmOn) {
        // Send a message to devices subscribed to the provided topic.
        admin.messaging().send(motionMessage)
            .then((response) => {
                // Response is a message ID string.
                console.log('Successfully sent message:', response);
            })
            .catch((error) => {
                console.log('Error sending message:', error);
            });
    }
}
rpio.poll(7, pollcb, rpio.POLL_HIGH); // LED
rpio.poll(12, pollcb, rpio.POLL_HIGH); // Motion Detector

// Temp. sensor
var rpiDhtSensor = require('rpi-dht-sensor');
var dht = new rpiDhtSensor.DHT11(17);

function read() {
    var readout = dht.read();
    temperature = readout.temperature;
    humidity = readout.humidity;
    io.sockets.emit('tempSensor', {value: temperature}, {value: humidity});
}
setInterval(read, 1000);