package com.raju.disney.opentelemetry;

interface AppStateListener {
    void appForegrounded();
    void appBackgrounded();
}
