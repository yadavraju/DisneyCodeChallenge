package com.raju.disney.opentelemetry

internal interface SessionIdChangeListener {
    /**
     * Gets called every time a new sessionId is generated.
     */
    fun onChange(oldSessionId: String, newSessionId: String)
}