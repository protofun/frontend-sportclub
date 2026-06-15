package org.example.project.util

actual fun currentEpochMillis(): Long = kotlin.js.Date().getTime().toLong()
