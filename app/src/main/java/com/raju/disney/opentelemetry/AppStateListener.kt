package com.raju.disney.opentelemetry

internal interface AppStateListener {
    fun appForegrounded()
    fun appBackgrounded()
}