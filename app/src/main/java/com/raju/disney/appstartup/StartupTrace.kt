package com.raju.disney.appstartup

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import com.raju.disney.opentelemetry.OtelConfiguration
import com.raju.disney.opentelemetry.OtelConfiguration.createChildSpan
import com.raju.disney.opentelemetry.OtelConfiguration.createSpan
import com.raju.disney.ui.activity.DashboardActivity
import com.raju.disney.ui.activity.MainActivity
import io.opentelemetry.api.trace.Span
import io.opentelemetry.api.trace.Tracer
import java.util.concurrent.TimeUnit


/**
 * A class to capture the Android AppStart Trace information.
 * https://github.com/firebase/firebase-android-sdk/blob/master/firebase-perf/src/main/java/com/google/firebase/perf/provider/FirebasePerfProvider.java
 */
object StartupTrace : Application.ActivityLifecycleCallbacks, LifecycleObserver {

    private val TAG = StartupTimeProvider::class.simpleName
    private val tracer: Tracer = OtelConfiguration.getTracer("app:startup")
    private val MAX_LATENCY_BEFORE_UI_INIT = TimeUnit.MINUTES.toMillis(1)

    private var appStartTime: Long? = null
    private var onCreateTime: Long? = null
    var isStartedFromBackground = false
    private var atLeastOnTimeOnBackground = false

    private var isRegisteredForLifecycleCallbacks = false
    private var appContext: Context? = null

    private var trace: Span? = null

    /**
     * If the time difference between app starts and creation of any Activity is larger than
     * MAX_LATENCY_BEFORE_UI_INIT, set isTooLateToInitUI to true and we don't send AppStart Trace.
     */
    var isTooLateToInitUI = false

    fun onColdStartInitiated(context: Context) {
        trace = tracer.createSpan("cold_startup_initiated")
        try {
            trace?.makeCurrent().use {
                // Set the Semantic Convention
                trace?.addEvent("onColdStartInitiated")
                trace?.setAttribute("StartupTrace.onColdStartInitiated", "Starting the work.")
                appStartTime = System.currentTimeMillis()
                val appContext = context.applicationContext
                if (appContext is Application) {
                    appContext.registerActivityLifecycleCallbacks(this)
                    ProcessLifecycleOwner.get().lifecycle.addObserver(this)
                    isRegisteredForLifecycleCallbacks = true
                    this.appContext = appContext
                }

            }
        } finally {
            trace?.end()
        }
    }

    /** Unregister this callback after AppStart trace is logged.  */
    private fun unregisterActivityLifecycleCallbacks() {
        if (!isRegisteredForLifecycleCallbacks) {
            return
        }
        (appContext as Application).unregisterActivityLifecycleCallbacks(this)
        isRegisteredForLifecycleCallbacks = false
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        if (isStartedFromBackground || onCreateTime != null) {
            return
        }
        onCreateTime = System.currentTimeMillis()

        if ((onCreateTime!! - appStartTime!!) > MAX_LATENCY_BEFORE_UI_INIT) {
            isTooLateToInitUI = true
        }
    }

    override fun onActivityStarted(activity: Activity) {}

    override fun onActivityResumed(activity: Activity) {
        if (isStartedFromBackground || isTooLateToInitUI || atLeastOnTimeOnBackground) {
            unregisterActivityLifecycleCallbacks()
            return
        }

        if (activity is DashboardActivity) {
            val childSpan = tracer.createChildSpan("cold_startup_mainActivity_visible", trace)
            childSpan.setAttribute("StartupTrace:onActivityResumed", "onActivityResumed")
            childSpan.addEvent("Finished working.")
            childSpan.end()
            Log.d(TAG, "Cold start finished after ${System.currentTimeMillis() - appStartTime!!}ms")
            if (isRegisteredForLifecycleCallbacks) {
                // After AppStart trace is logged, we can unregister this callback.
                unregisterActivityLifecycleCallbacks()
            }
        }
    }

    override fun onActivityPaused(activity: Activity) {}

    override fun onActivityStopped(activity: Activity) {}

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

    override fun onActivityDestroyed(activity: Activity) {}

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onEnterBackground() {
        atLeastOnTimeOnBackground = true
        ProcessLifecycleOwner.get().lifecycle.removeObserver(this)
    }

    /**
     * We use StartFromBackgroundRunnable to detect if app is started from background or foreground.
     * If app is started from background, we do not generate AppStart trace. This runnable is posted
     * to main UI thread from StartupTimeProvider. If app is started from background, this runnable
     * will be executed before any activity's onCreate() method. If app is started from foreground,
     * activity's onCreate() method is executed before this runnable.
     */
    object StartFromBackgroundRunnable : Runnable {
        override fun run() {
            // if no activity has ever been created.
            if (onCreateTime == null) {
                isStartedFromBackground = true
            }
        }
    }
}