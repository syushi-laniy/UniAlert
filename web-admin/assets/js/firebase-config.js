// Firebase configuration and compatibility helpers
window.SafeCampus = window.SafeCampus || {};

// Firebase config
const firebaseConfig = {
  apiKey: "AIzaSyA0oZJfwDxBEnjZ6PbiyqcRAGq-g4sTBWU",
  authDomain: "safecampus-c39ba.firebaseapp.com",
  projectId: "safecampus-c39ba",
  storageBucket: "safecampus-c39ba.firebasestorage.app",
  messagingSenderId: "437897447187",
  appId: "1:437897447187:web:65571a5782f352fa51bce6",
  measurementId: "G-MBFCL68QJ9"
};

// Initialize Firebase (compat / v8 style)
firebase.initializeApp(firebaseConfig);

// App services
SafeCampus.auth = firebase.auth();
SafeCampus.db = firebase.firestore();

// Collection names used in the app
SafeCampus.collections = {
  INCIDENTS: "incidents",
  LOCATIONS: "locations",
  NEWS: "news",
  USERS: "users",
  LOGS: "logs"
};

// --- Minimal compatibility helpers so existing code keeps working ---
firebase.onAuthStateChanged = (auth, callback) => auth.onAuthStateChanged(callback);
firebase.signInWithEmailAndPassword = (auth, email, password) =>
  auth.signInWithEmailAndPassword(email, password);
firebase.signOut = (auth) => auth.signOut();

firebase.collection = (db, path) => db.collection(path);
firebase.addDoc = (collectionRef, data) => collectionRef.add(data);
firebase.getDocs = (queryOrCollection) => queryOrCollection.get();

firebase.orderBy = (field, direction = "asc") => ({
  __type: "orderBy",
  field,
  direction
});

firebase.query = (collectionRef, ...constraints) => {
  let ref = collectionRef;
  constraints.forEach((constraint) => {
    if (constraint && constraint.__type === "orderBy") {
      ref = ref.orderBy(constraint.field, constraint.direction);
    }
  });
  return ref;
};

firebase.serverTimestamp = () => firebase.firestore.FieldValue.serverTimestamp();
