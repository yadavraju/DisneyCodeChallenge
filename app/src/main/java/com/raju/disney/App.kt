package com.raju.disney

import android.app.Application
import com.raju.disney.opentelemetry.DisneyOtel
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class App : Application() {

    override fun onCreate() {
        super.onCreate()

        val config = DisneyOtel.newConfigBuilder()
            .debugEnabled(true)
            .anrDetectionEnabled(true)
            .applicationName("DisneyAndroid")
            .build()

        DisneyOtel.initialize(config, this)
    }
}
