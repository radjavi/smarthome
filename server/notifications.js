// See documentation on defining a message payload.
var ledMsg = {
    android: {
      priority: 'high',
      notification: {
        title: 'LED',
        body: 'LED has been turned on!',
        sound: 'default',
        color: '#E53935'
      }
    },
    topic: 'all'
};

var motionMsg = {
    android: {
      priority: 'high',
      notification: {
        title: 'Motion',
        body: 'Motion has been detected!',
        sound: 'default',
        color: '#E53935'
      }
    },
    topic: 'all'
};

var tempMsg = {
  android: {
    priority: 'high',
    notification: {
      title: 'Temperature',
      body: 'Temperature is 32°C!',
      sound: 'default',
      color: '#E53935'
    }
  },
  topic: 'all'
};

module.exports = {
  ledMsg: ledMsg,
  motionMsg: motionMsg,
  tempMsg: tempMsg
}
