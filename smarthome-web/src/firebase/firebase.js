import firebase from 'firebase/app';
import 'firebase/auth';
import { config } from './config.js'

if (!firebase.apps.length) {
  firebase.initializeApp(config);
  firebase.auth().setPersistence(firebase.auth.Auth.Persistence.SESSION);
}

const auth = firebase.auth();

export {
  auth,
};
