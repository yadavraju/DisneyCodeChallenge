package com.raju.disney.opentelemetry

import android.app.Activity
import android.app.Application.ActivityLifecycleCallbacks
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import io.opentelemetry.api.trace.Tracer
import java.util.concurrent.atomic.AtomicReference

internal class ActivityCallbacks private constructor(builder: Builder) :
    ActivityLifecycleCallbacks {

    private val tracersByActivityClassName: MutableMap<String, ActivityTracer> by lazy { HashMap() }
    private val initialAppActivity by lazy { AtomicReference<String>() }
    private val tracer: Tracer
    private val visibleScreenTracker: VisibleScreenTracker
    private val startupTimer: AppStartupTimer
    private val appStateListeners: List<AppStateListener>

    //we count the number of activities that have been "started" and not yet "stopped" here to figure out when the app goes into the background.
    private var numberOfOpenActivities = 0

    override fun onActivityPreCreated(activity: Activity, savedInstanceState: Bundle?) {
        getTracer(activity)
            .startActivityCreation()
            .addEvent("activityPreCreated")

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

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        addEvent(activity, "activityCreated")
    }

    override fun onActivityPostCreated(activity: Activity, savedInstanceState: Bundle?) {
        addEvent(activity, "activityPostCreated")
    }

    override fun onActivityPreStarted(activity: Activity) {
        getTracer(activity)
            .initiateRestartSpanIfNecessary(tracersByActivityClassName.size > 1)
            .addEvent("activityPreStarted")
    }

    override fun onActivityStarted(activity: Activity) {
        if (numberOfOpenActivities == 0) {
            for (appStateListener in appStateListeners) {
                appStateListener.appForegrounded()
            }
        }
        numberOfOpenActivities++
        addEvent(activity, "activityStarted")
    }

    override fun onActivityPostStarted(activity: Activity) {
        addEvent(activity, "activityPostStarted")
    }

    override fun onActivityPreResumed(activity: Activity) {
        getTracer(activity)
            .startSpanIfNoneInProgress("Resumed:" + activity.javaClass.simpleName)
            .addEvent("activityPreResumed")
    }

    override fun onActivityResumed(activity: Activity) {
        addEvent(activity, "activityResumed")
    }

    override fun onActivityPostResumed(activity: Activity) {
        getTracer(activity)
            .addEvent("activityPostResumed")
            .addPreviousScreenAttribute()
            .endSpanForActivityResumed()
        visibleScreenTracker.activityResumed(activity)
    }

    override fun onActivityPrePaused(activity: Activity) {
        getTracer(activity)
            .startSpanIfNoneInProgress("Paused:" + activity.javaClass.simpleName)
            .addEvent("activityPrePaused")
        visibleScreenTracker.activityPaused(activity)
    }

    override fun onActivityPaused(activity: Activity) {
        addEvent(activity, "activityPaused")
    }

    override fun onActivityPostPaused(activity: Activity) {
        getTracer(activity).addEvent("activityPostPaused").endActiveSpan()
    }

    override fun onActivityPreStopped(activity: Activity) {
        getTracer(activity)
            .startSpanIfNoneInProgress("Stopped:" + activity.javaClass.simpleName)
            .addEvent("activityPreStopped")
    }

    override fun onActivityStopped(activity: Activity) {
        if (--numberOfOpenActivities == 0) {
            for (appStateListener in appStateListeners) {
                appStateListener.appBackgrounded()
            }
        }
        addEvent(activity, "activityStopped")
    }

    override fun onActivityPostStopped(activity: Activity) {
        getTracer(activity).addEvent("activityPostStopped").endActiveSpan()
    }

    override fun onActivityPreSaveInstanceState(activity: Activity, outState: Bundle) {
        //todo: add event
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
        //todo: add event
    }

    override fun onActivityPostSaveInstanceState(activity: Activity, outState: Bundle) {
        //todo: add event
    }

    override fun onActivityPreDestroyed(activity: Activity) {
        getTracer(activity)
            .startSpanIfNoneInProgress("Destroyed:" + activity.javaClass.simpleName)
            .addEvent("activityPreDestroyed")
    }

    override fun onActivityDestroyed(activity: Activity) {
        addEvent(activity, "activityDestroyed")
    }

    override fun onActivityPostDestroyed(activity: Activity) {
        getTracer(activity).addEvent("activityPostDestroyed").endActiveSpan()
    }

    private fun addEvent(activity: Activity, eventName: String) {
        getTracer(activity).addEvent(eventName)
    }

    private fun getTracer(activity: Activity): ActivityTracer {
        var activityTracer = tracersByActivityClassName[activity.javaClass.name]
        if (activityTracer == null) {
            activityTracer = ActivityTracer(
                activity,
                initialAppActivity,
                tracer,
                visibleScreenTracker,
                startupTimer
            )
            tracersByActivityClassName[activity.javaClass.name] = activityTracer
        }
        return activityTracer
    }

    internal class Builder {
        lateinit var tracer: Tracer
        lateinit var visibleScreenTracker: VisibleScreenTracker
        lateinit var startupTimer: AppStartupTimer
        lateinit var appStateListeners: List<AppStateListener>

        fun build(): ActivityCallbacks {
            return ActivityCallbacks(this)
        }

        fun tracer(tracer: Tracer): Builder {
            this.tracer = tracer
            return this
        }

        fun visibleScreenTracker(visibleScreenTracker: VisibleScreenTracker): Builder {
            this.visibleScreenTracker = visibleScreenTracker
            return this
        }

        fun startupTimer(startupTimer: AppStartupTimer): Builder {
            this.startupTimer = startupTimer
            return this
        }

        fun appStateListeners(appStateListeners: List<AppStateListener>): Builder {
            this.appStateListeners = appStateListeners
            return this
        }
    }

    companion object {
        fun builder(): Builder = Builder()
    }

    init {
        tracer = builder.tracer
        visibleScreenTracker = builder.visibleScreenTracker
        startupTimer = builder.startupTimer
        appStateListeners = builder.appStateListeners
    }
}