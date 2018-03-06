// See documentation on defining a message payload.
var ledMsg = {
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

var motionMsg = {
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

module.exports = {
  ledMsg: ledMsg,
  motionMsg: motionMsg
}