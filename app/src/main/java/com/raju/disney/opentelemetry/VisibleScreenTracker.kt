package com.raju.disney.opentelemetry

import android.app.Activity
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import java.util.concurrent.atomic.AtomicReference

/**
 * Wherein we do our best to figure out what "screen" is visible and what was the previously visible "screen".
 *
 *
 * In general, we favor using the last fragment that was resumed, but fall back to the last resumed activity
 * in case we don't have a fragment.
 *
 *
 * We always ignore NavHostFragment instances since they aren't ever visible to the user.
 *
 *
 * We have to treat DialogFragments slightly differently since they don't replace the launching screen, and
 * the launching screen never leaves visibility.
 */
internal class VisibleScreenTracker {

    private val lastResumedActivity by lazy { AtomicReference<String?>() }
    private val previouslyLastResumedActivity by lazy { AtomicReference<String?>() }
    private val lastResumedFragment by lazy { AtomicReference<String?>() }
    private val previouslyLastResumedFragment by lazy { AtomicReference<String?>() }

    val previouslyVisibleScreen: String?
        get() {
            val previouslyLastFragment = previouslyLastResumedFragment.get()
            return previouslyLastFragment ?: previouslyLastResumedActivity.get()
        }

    val currentlyVisibleScreen: String
        get() {
            val lastFragment = lastResumedFragment.get()
            if (lastFragment != null) {
                return lastFragment
            }
            val lastActivity = lastResumedActivity.get()
            return lastActivity ?: "unknown"
        }

    fun activityResumed(activity: Activity) {
        lastResumedActivity.set(activity.javaClass.simpleName)
    }

    fun activityPaused(activity: Activity) {
        previouslyLastResumedActivity.set(activity.javaClass.simpleName)
        lastResumedActivity.compareAndSet(activity.javaClass.simpleName, null)
    }

    fun fragmentResumed(fragment: Fragment) {
//        skip the NavHostFragment since it's never really "visible" by itself.
//        if (fragment is NavHostFragment) {
//            return;
//        }
        if (fragment is DialogFragment) {
            previouslyLastResumedFragment.set(lastResumedFragment.get())
        }
        lastResumedFragment.set(fragment.javaClass.simpleName)
    }

    fun fragmentPaused(fragment: Fragment) {
//        skip the NavHostFragment since it's never really "visible" by itself.
//        if (fragment is NavHostFragment) {
//            return;
//        }
        if (fragment is DialogFragment) {
            lastResumedFragment.set(previouslyLastResumedFragment.get())
        } else {
            lastResumedFragment.compareAndSet(fragment.javaClass.simpleName, null)
        }
        previouslyLastResumedFragment.set(fragment.javaClass.simpleName)
    }
}