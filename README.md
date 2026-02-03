# UniAlert

UniAlert is a **campus safety alert system** consisting of an **Android mobile application** and a **web-based admin dashboard**, both connected to a shared **Firebase Firestore backend**. The system enables students to report incidents in real time while allowing administrators to monitor, manage, and respond efficiently.

---

## Project Overview

UniAlert was developed for **ICT602 Mobile Technology** as a group project. It demonstrates:

* Mobile application development (Android)
* Web-based administration panel
* Cloud-based backend using Firebase
* Real-time data synchronization (Netcentric concept)

---

## System Architecture

The system is implemented as a **mono-repository** with two main components:

```
UniAlert/
├── app/                 # Android application (Android Studio)
├── gradle/              # Android Gradle configuration
├── build.gradle.kts
├── settings.gradle.kts
│
├── web-admin/            # Web admin dashboard (VS Code)
│   ├── index.html
│   └── assets/
│       ├── css/
│       └── js/
│
└── README.md
```

---

## Android Application (User Side)

**Main Features:**

* User authentication (Firebase Authentication)
* View campus map with location markers
* Report safety incidents (with date, time, and GPS)
* View news and safety updates
* Incident list with custom adapter

**Technologies Used:**

* Java (Android)
* XML (UI layouts)
* Google Maps API
* Firebase Authentication
* Firebase Firestore

---

## Web Admin Dashboard

The web admin dashboard allows administrators to manage reported incidents.

**Main Features:**

* Admin login
* View reported incidents in real time
* Read incident details from Firestore
* Manage and monitor safety reports

**Technologies Used:**

* HTML, CSS, JavaScript
* Firebase Web SDK
* Firebase Firestore

---

## ☁️ Firebase Integration

Both the Android app and the web admin dashboard connect to the **same Firebase project**.

**Firebase Services Used:**

* Firebase Authentication
* Firebase Firestore (real-time database)

This enables **real-time synchronization**, where:

* Incidents reported from the Android app instantly appear on the web admin dashboard

---

## Security Notes

* Sensitive files such as `.env` and `local.properties` are excluded from version control
* Firebase security rules should be configured to restrict admin-only access for the web dashboard

---

## 🚀 How to Run the Project

### Android App

1. Open the `UniAlert` project in **Android Studio**
2. Sync Gradle
3. Add your `google-services.json`
4. Run the app on an emulator or physical device

### Web Admin

1. Open the `web-admin` folder in **VS Code**
2. Open `index.html` using Live Server or a browser
3. Ensure Firebase configuration is correct

---

## Academic Context

* Course: **ICT602 – Mobile Technology**
* Project Type: Group Assignment
* Focus Areas: Mobile Development, Firebase, Netcentric Computing

---

## Author

**Nurul Lailani Asyura Binti Abdull Aziz**
**Nurhanisa Najwa Binti Nordin**
**Puteri Noor Tassnim Binti Abdul Mu'iz**
**Raja Nur Afieda Binti Raja Abdul Halim**
Faculty of Computer and Mathematical Sciences

---

##  License

This project is developed for **educational purposes only**.
