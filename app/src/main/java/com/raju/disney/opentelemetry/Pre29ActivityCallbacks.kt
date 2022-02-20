package com.raju.disney.opentelemetry

import android.app.Activity
import android.app.Application.ActivityLifecycleCallbacks
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import io.opentelemetry.api.trace.Tracer
import java.util.concurrent.atomic.AtomicReference

internal class Pre29ActivityCallbacks(
    private val tracer: Tracer,
    private val visibleScreenTracker: VisibleScreenTracker,
    private val appStartupTimer: AppStartupTimer,
    private val appStateListeners: List<AppStateListener>
) : ActivityLifecycleCallbacks {

    private val tracersByActivityClassName: MutableMap<String, ActivityTracer> = HashMap()
    private val initialAppActivity = AtomicReference<String>()
    private var numberOfOpenActivities = 0

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        getTracer(activity)
            .startActivityCreation()
            .addEvent("activityCreated")
        if (activity is FragmentActivity) {
            val fragmentManager = activity.supportFragmentManager
            fragmentManager.registerFragmentLifecycleCallbacks(
                FragmentLifecycleCallbacks(
                    tracer,
                    visibleScreenTracker
                ), true
            )
        }
    }

    override fun onActivityStarted(activity: Activity) {
        if (numberOfOpenActivities == 0) {
            for (appListener in appStateListeners) {
                appListener.appForegrounded()
            }
        }
        numberOfOpenActivities++
        getTracer(activity)
            .initiateRestartSpanIfNecessary(tracersByActivityClassName.size > 1)
            .addEvent("activityStarted")
    }

    override fun onActivityResumed(activity: Activity) {
        getTracer(activity)
            .startSpanIfNoneInProgress("Resumed:" + activity.javaClass.simpleName)
            .addEvent("activityResumed")
            .addPreviousScreenAttribute()
            .endSpanForActivityResumed()
        visibleScreenTracker.activityResumed(activity)
    }

    override fun onActivityPaused(activity: Activity) {
        getTracer(activity)
            .startSpanIfNoneInProgress("Paused:" + activity.javaClass.simpleName)
            .addEvent("activityPaused")
            .endActiveSpan()
        visibleScreenTracker.activityPaused(activity)
    }

    override fun onActivityStopped(activity: Activity) {
        if (--numberOfOpenActivities == 0) {
            for (appListener in appStateListeners) {
                appListener.appBackgrounded()
            }
        }
        getTracer(activity)
            .startSpanIfNoneInProgress("Stopped:" + activity.javaClass.simpleName)
            .addEvent("activityStopped")
            .endActiveSpan()
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
        //todo: add event
    }

    override fun onActivityDestroyed(activity: Activity) {
        getTracer(activity)
            .startSpanIfNoneInProgress("Destroyed:" + activity.javaClass.simpleName)
            .addEvent("activityDestroyed")
            .endActiveSpan()
    }

    private fun getTracer(activity: Activity): ActivityTracer {
        var activityTracer = tracersByActivityClassName[activity.javaClass.name]
        if (activityTracer == null) {
            activityTracer = ActivityTracer(
                activity,
                initialAppActivity,
                tracer,
                visibleScreenTracker,
                appStartupTimer
            )
            tracersByActivityClassName[activity.javaClass.name] = activityTracer
        }
        return activityTracer
    }
}