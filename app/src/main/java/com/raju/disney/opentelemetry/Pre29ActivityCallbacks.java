package com.raju.disney.opentelemetry;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import io.opentelemetry.api.trace.Tracer;

class Pre29ActivityCallbacks implements Application.ActivityLifecycleCallbacks {
    private final Tracer tracer;
    private final VisibleScreenTracker visibleScreenTracker;
    private final Map<String, ActivityTracer> tracersByActivityClassName = new HashMap<>();
    private final AtomicReference<String> initialAppActivity = new AtomicReference<>();
    private final AppStartupTimer appStartupTimer;
    private final List<AppStateListener> appStateListeners;
    private int numberOfOpenActivities = 0;

    Pre29ActivityCallbacks(Tracer tracer, VisibleScreenTracker visibleScreenTracker, AppStartupTimer appStartupTimer, List<AppStateListener> appStateListeners) {
        this.tracer = tracer;
        this.visibleScreenTracker = visibleScreenTracker;
        this.appStartupTimer = appStartupTimer;
        this.appStateListeners = appStateListeners;
    }

    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
        getTracer(activity)
                .startActivityCreation()
                .addEvent("activityCreated");

        if (activity instanceof FragmentActivity) {
            FragmentManager fragmentManager = ((FragmentActivity) activity).getSupportFragmentManager();
            fragmentManager.registerFragmentLifecycleCallbacks(new OtelFragmentLifecycleCallbacks(tracer, visibleScreenTracker), true);
        }
    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {
        if (numberOfOpenActivities == 0) {
            for (AppStateListener appListener : appStateListeners) {
                appListener.appForegrounded();
            }
        }
        numberOfOpenActivities++;
        getTracer(activity)
                .initiateRestartSpanIfNecessary(tracersByActivityClassName.size() > 1)
                .addEvent("activityStarted");
    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {
        getTracer(activity)
                .startSpanIfNoneInProgress("Resumed:" + activity.getClass().getSimpleName())
                .addEvent("activityResumed")
                .addPreviousScreenAttribute()
                .endSpanForActivityResumed();
        visibleScreenTracker.activityResumed(activity);
    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {
        getTracer(activity)
                .startSpanIfNoneInProgress("Paused:" + activity.getClass().getSimpleName())
                .addEvent("activityPaused")
                .endActiveSpan();
        visibleScreenTracker.activityPaused(activity);
    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {
        if (--numberOfOpenActivities == 0) {
            for (AppStateListener appListener : appStateListeners) {
                appListener.appBackgrounded();
            }
        }
        getTracer(activity)
                .startSpanIfNoneInProgress("Stopped:" + activity.getClass().getSimpleName())
                .addEvent("activityStopped")
                .endActiveSpan();
    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {
        //todo: add event
    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {
        getTracer(activity)
                .startSpanIfNoneInProgress("Destroyed:" + activity.getClass().getSimpleName())
                .addEvent("activityDestroyed")
                .endActiveSpan();
    }

    private ActivityTracer getTracer(Activity activity) {
        ActivityTracer activityTracer = tracersByActivityClassName.get(activity.getClass().getName());
        if (activityTracer == null) {
            activityTracer = new ActivityTracer(activity, initialAppActivity, tracer, visibleScreenTracker, appStartupTimer);
            tracersByActivityClassName.put(activity.getClass().getName(), activityTracer);
        }
        return activityTracer;
    }

}
