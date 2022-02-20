package com.raju.disney.opentelemetry

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import io.opentelemetry.api.trace.Tracer

internal class FragmentLifecycleCallbacks(
    private val tracer: Tracer,
    private val visibleScreenTracker: VisibleScreenTracker
) : FragmentManager.FragmentLifecycleCallbacks() {

    private val tracersByFragmentClassName: MutableMap<String, FragmentTracer> by lazy { HashMap() }

    override fun onFragmentPreAttached(fm: FragmentManager, f: Fragment, context: Context) {
        super.onFragmentPreAttached(fm, f, context)
        getTracer(f)
            .startFragmentCreation()
            .addEvent("fragmentPreAttached")
    }

    override fun onFragmentAttached(fm: FragmentManager, f: Fragment, context: Context) {
        super.onFragmentAttached(fm, f, context)
        addEvent(f, "fragmentAttached")
    }

    override fun onFragmentPreCreated(
        fm: FragmentManager,
        f: Fragment,
        savedInstanceState: Bundle?
    ) {
        super.onFragmentPreCreated(fm, f, savedInstanceState)
        addEvent(f, "fragmentPreCreated")
    }

    override fun onFragmentCreated(fm: FragmentManager, f: Fragment, savedInstanceState: Bundle?) {
        super.onFragmentCreated(fm, f, savedInstanceState)
        addEvent(f, "fragmentCreated")
    }

    override fun onFragmentViewCreated(
        fm: FragmentManager,
        f: Fragment,
        v: View,
        savedInstanceState: Bundle?
    ) {
        super.onFragmentViewCreated(fm, f, v, savedInstanceState)
        getTracer(f)
            .startSpanIfNoneInProgress("Restored" + f.javaClass.name)
            .addEvent("fragmentViewCreated")
    }

    override fun onFragmentStarted(fm: FragmentManager, f: Fragment) {
        super.onFragmentStarted(fm, f)
        addEvent(f, "fragmentStarted")
    }

    override fun onFragmentResumed(fm: FragmentManager, f: Fragment) {
        super.onFragmentResumed(fm, f)
        getTracer(f)
            .startSpanIfNoneInProgress("Resumed:" + f.javaClass.name)
            .addEvent("fragmentResumed")
            .addPreviousScreenAttribute()
            .endActiveSpan()
        visibleScreenTracker.fragmentResumed(f)
    }

    override fun onFragmentPaused(fm: FragmentManager, f: Fragment) {
        super.onFragmentPaused(fm, f)
        visibleScreenTracker.fragmentPaused(f)
        getTracer(f).startSpanIfNoneInProgress("Paused").addEvent("fragmentPaused")
    }

    override fun onFragmentStopped(fm: FragmentManager, f: Fragment) {
        super.onFragmentStopped(fm, f)
        getTracer(f)
            .addEvent("fragmentStopped")
            .endActiveSpan()
    }

    override fun onFragmentViewDestroyed(fm: FragmentManager, f: Fragment) {
        super.onFragmentViewDestroyed(fm, f)
        getTracer(f)
            .startSpanIfNoneInProgress("ViewDestroyed:" + f.javaClass.name)
            .addEvent("fragmentViewDestroyed")
            .endActiveSpan()
    }

    override fun onFragmentDestroyed(fm: FragmentManager, f: Fragment) {
        super.onFragmentDestroyed(fm, f)
        //note: this might not get called if the dev has checked "retainInstance" on the fragment
        getTracer(f)
            .startSpanIfNoneInProgress("Destroyed:" + f.javaClass.name)
            .addEvent("fragmentDestroyed")
    }

    override fun onFragmentDetached(fm: FragmentManager, f: Fragment) {
        super.onFragmentDetached(fm, f)
        // this is a terminal operation, but might also be the only thing we see on app getting killed, so
        getTracer(f)
            .startSpanIfNoneInProgress("Detached:" + f.javaClass.name)
            .addEvent("fragmentDetached")
            .endActiveSpan()
    }

    private fun addEvent(fragment: Fragment, eventName: String) {
        val fragmentTracer = tracersByFragmentClassName[fragment.javaClass.name]
        fragmentTracer?.addEvent(eventName)
    }

    private fun getTracer(fragment: Fragment): FragmentTracer {
        var activityTracer = tracersByFragmentClassName[fragment.javaClass.name]
        if (activityTracer == null) {
            activityTracer = FragmentTracer(fragment, tracer, visibleScreenTracker)
            tracersByFragmentClassName[fragment.javaClass.name] = activityTracer
        }
        return activityTracer
    }
}