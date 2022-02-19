package com.raju.disney.opentelemetry;

interface SessionIdChangeListener {

    /**
     * Gets called every time a new sessionId is generated.
     */
    void onChange(String oldSessionId, String newSessionId);
}
