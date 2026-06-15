package org.example.project.util

@OptIn(kotlin.js.ExperimentalWasmJsInterop::class)
@JsFun("() => Date.now()")
private external fun jsDateNow(): Double

actual fun currentEpochMillis(): Long = jsDateNow().toLong()
