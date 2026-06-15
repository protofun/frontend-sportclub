package org.example.project.api

// 10.0.2.2 is the Android emulator's alias for the host machine's localhost.
// On a physical device, replace this with your computer's LAN IP address.
actual fun defaultApiBaseUrl(): String = "http://10.0.2.2:8080/api/v1"
