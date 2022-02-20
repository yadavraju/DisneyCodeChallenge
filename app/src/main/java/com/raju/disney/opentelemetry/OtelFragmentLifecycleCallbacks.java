package com.raju.disney.opentelemetry;

import android.content.Context;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import java.util.HashMap;
import java.util.Map;

import io.opentelemetry.api.trace.Tracer;

class OtelFragmentLifecycleCallbacks extends FragmentManager.FragmentLifecycleCallbacks {
    private final Map<String, FragmentTracer> tracersByFragmentClassName = new HashMap<>();

    private final Tracer tracer;
    private final VisibleScreenTracker visibleScreenTracker;

    OtelFragmentLifecycleCallbacks(Tracer tracer, VisibleScreenTracker visibleScreenTracker) {
        this.tracer = tracer;
        this.visibleScreenTracker = visibleScreenTracker;
    }

    @Override
    public void onFragmentPreAttached(@NonNull FragmentManager fm, @NonNull Fragment f, @NonNull Context context) {
        super.onFragmentPreAttached(fm, f, context);
        getTracer(f)
                .startFragmentCreation()
                .addEvent("fragmentPreAttached");
    }

    @Override
    public void onFragmentAttached(@NonNull FragmentManager fm, @NonNull Fragment f, @NonNull Context context) {
        super.onFragmentAttached(fm, f, context);
        addEvent(f, "fragmentAttached");
    }

    @Override
    public void onFragmentPreCreated(@NonNull FragmentManager fm, @NonNull Fragment f, @Nullable Bundle savedInstanceState) {
        super.onFragmentPreCreated(fm, f, savedInstanceState);
        addEvent(f, "fragmentPreCreated");
    }

    @Override
    public void onFragmentCreated(@NonNull FragmentManager fm, @NonNull Fragment f, @Nullable Bundle savedInstanceState) {
        super.onFragmentCreated(fm, f, savedInstanceState);
        addEvent(f, "fragmentCreated");
    }

    @Override
    public void onFragmentViewCreated(@NonNull FragmentManager fm, @NonNull Fragment f, @NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onFragmentViewCreated(fm, f, v, savedInstanceState);
        getTracer(f)
                .startSpanIfNoneInProgress("Restored" + f.getClass().getName())
                .addEvent("fragmentViewCreated");
    }

    @Override
    public void onFragmentStarted(@NonNull FragmentManager fm, @NonNull Fragment f) {
        super.onFragmentStarted(fm, f);
        addEvent(f, "fragmentStarted");
    }

    @Override
    public void onFragmentResumed(@NonNull FragmentManager fm, @NonNull Fragment f) {
        super.onFragmentResumed(fm, f);
        getTracer(f)
                .startSpanIfNoneInProgress("Resumed:" + f.getClass().getName())
                .addEvent("fragmentResumed")
                .addPreviousScreenAttribute()
                .endActiveSpan();
        visibleScreenTracker.fragmentResumed(f);
    }

    @Override
    public void onFragmentPaused(@NonNull FragmentManager fm, @NonNull Fragment f) {
        super.onFragmentPaused(fm, f);
        visibleScreenTracker.fragmentPaused(f);
        getTracer(f).startSpanIfNoneInProgress("Paused").addEvent("fragmentPaused");
    }

    @Override
    public void onFragmentStopped(@NonNull FragmentManager fm, @NonNull Fragment f) {
        super.onFragmentStopped(fm, f);
        getTracer(f)
                .addEvent("fragmentStopped")
                .endActiveSpan();
    }

    @Override
    public void onFragmentSaveInstanceState(@NonNull FragmentManager fm, @NonNull Fragment f, @NonNull Bundle outState) {
        super.onFragmentSaveInstanceState(fm, f, outState);
    }

    @Override
    public void onFragmentViewDestroyed(@NonNull FragmentManager fm, @NonNull Fragment f) {
        super.onFragmentViewDestroyed(fm, f);
        getTracer(f)
                .startSpanIfNoneInProgress("ViewDestroyed:" + f.getClass().getName())
                .addEvent("fragmentViewDestroyed")
                .endActiveSpan();
    }

    @Override
    public void onFragmentDestroyed(@NonNull FragmentManager fm, @NonNull Fragment f) {
        super.onFragmentDestroyed(fm, f);
        //note: this might not get called if the dev has checked "retainInstance" on the fragment
        getTracer(f)
                .startSpanIfNoneInProgress("Destroyed:" + f.getClass().getName())
                .addEvent("fragmentDestroyed");
    }

    @Override
    public void onFragmentDetached(@NonNull FragmentManager fm, @NonNull Fragment f) {
        super.onFragmentDetached(fm, f);
        // this is a terminal operation, but might also be the only thing we see on app getting killed, so
        getTracer(f)
                .startSpanIfNoneInProgress("Detached:" + f.getClass().getName())
                .addEvent("fragmentDetached")
                .endActiveSpan();
    }

    private void addEvent(@NonNull Fragment fragment, String eventName) {
        FragmentTracer fragmentTracer = tracersByFragmentClassName.get(fragment.getClass().getName());
        if (fragmentTracer != null) {
            fragmentTracer.addEvent(eventName);
        }
    }

    private FragmentTracer getTracer(Fragment fragment) {
        FragmentTracer activityTracer = tracersByFragmentClassName.get(fragment.getClass().getName());
        if (activityTracer == null) {
            activityTracer = new FragmentTracer(fragment, tracer, visibleScreenTracker);
            tracersByFragmentClassName.put(fragment.getClass().getName(), activityTracer);
        }
        return activityTracer;
    }
}
