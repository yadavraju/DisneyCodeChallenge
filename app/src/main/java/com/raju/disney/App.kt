package com.raju.disney

import android.app.Application
import com.raju.disney.opentelemetry.DisneyOTel
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class App : Application() {

    override fun onCreate() {
        super.onCreate()
        //Use your computer IP as endpoint when you are running locally, Port should be same
        val config = DisneyOTel.newConfigBuilder()
            .oltpExporterEndPoint("http://192.168.0.102:4317")
            .jaegerExporterEndPoint("http://192.168.0.102:14250")
            .debugEnabled(true)
            .anrDetectionEnabled(true)
            .applicationName("DisneyAndroid")
            .build()

        DisneyOTel.initialize(config, this)
    }
}
