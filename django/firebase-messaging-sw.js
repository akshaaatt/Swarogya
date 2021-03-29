importScripts('https://www.gstatic.com/firebasejs/7.15.0/firebase-app.js');
importScripts('https://www.gstatic.com/firebasejs/7.15.0/firebase-messaging.js');
importScripts("https://www.gstatic.com/firebasejs/7.15.5/firebase-firestore.js");

var firebaseConfig = {
  apiKey: "abcd",
  authDomain: "swarogya-aa30d.firebaseapp.com",
  databaseURL: "https://swarogya-aa30d.firebaseio.com",
  projectId: "swarogya-aa30d",
  storageBucket: "swarogya-aa30d.appspot.com",
  messagingSenderId: "819552361415",
  appId: "1:819552361415:web:5c22d654f938c1403697e2",
  measurementId: "G-3MEWE6QZGV"
};

firebase.initializeApp(firebaseConfig);

const messaging = firebase.messaging();

messaging.setBackgroundMessageHandler(function(payload) {
  // Customize notification here
  var d = new Date();
  const notificationTitle =payload['data']['title'] ;
  const notificationOptions = {
    body: d + payload['data']['body'] ,
    icon: '/firebase-logo.png',
  };
  return self.registration.showNotification(notificationTitle,
    notificationOptions);
});
