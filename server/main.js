var express = require('express'); // Web server
app = express();
server = require('http').createServer(app);
io = require('socket.io').listen(server); // Web socket server
io.set('heartbeat timeout', 4000); 
io.set('heartbeat interval', 2000);
// Authentication module. 
var auth = require('http-auth');
var basic = auth.digest({
    realm: "Web",
    file: __dirname + "/htpasswd"
});


app.use(auth.connect(basic)); // Authenticate
app.use(express.static('public')); // Tell the server that ./public/ contains the static webpages

server.listen(8080, function(){
    console.log("Server is running...");
}); // Start the webserver on port 8080

io.on('connection', function(socket){
    console.log("New client connected: " + socket.id);
    socket.on('disconnect', function(){
        console.log("Client disconnected: " + socket.id);
    });
});

var SerialPort = require("serialport");
var serialPort = new SerialPort("/dev/ttyAMA0", {baudRate: 115200});

var gpio = require('rpi-gpio');
gpio.setup(7, gpio.DIR_OUT); // LED
gpio.setup(17, gpio.DIR_IN); // TempSensor


var ledOn = false;
var temperature = 0;
var humidity = 0;

io.sockets.on('connection', function(socket) { // Gets called whenever a client connects
    socket.emit('led', {value: ledOn}); // Send the new client the current brightness
    socket.emit('tempSensor', {value: temperature}, {value: humidity});
    socket.on('led', function(data) { // Makes the socket react to 'led' packets by calling this function
        ledOn = data.value; // Updates brightness from the data object
        gpio.write(7, ledOn);
        io.sockets.emit('led', {value: ledOn}); // Sends the updated brightness to all connected clients
    });
});

// GPIO events
gpio.on('change', function(channel, value) {
    console.log('Channel ' + channel + ' value is now ' + value);
});
gpio.setup(18, gpio.DIR_IN, gpio.EDGE_RISING); // Motion Detector

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