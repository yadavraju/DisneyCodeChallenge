package com.raju.disney.opentelemetry;

import java.util.function.Supplier;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.context.Scope;

class ActiveSpan {
    private final VisibleScreenTracker visibleScreenTracker;

    private Span span;
    private Scope scope;

    ActiveSpan(VisibleScreenTracker visibleScreenTracker) {
        this.visibleScreenTracker = visibleScreenTracker;
    }

    boolean spanInProgress() {
        return span != null;
    }

    void startSpan(Supplier<Span> spanCreator) {
        //don't start one if there's already one in progress
        if (span != null) {
            return;
        }
        this.span = spanCreator.get();
        scope = span.makeCurrent();
    }

    void endActiveSpan() {
        if (scope != null) {
            scope.close();
            scope = null;
        }
        if (this.span != null) {
            this.span.end();
            this.span = null;
        }
    }

    void addEvent(String eventName) {
        if (span != null) {
            span.addEvent(eventName);
        }
    }

    void addPreviousScreenAttribute(String screenName) {
        if (span == null) {
            return;
        }
        String previouslyVisibleScreen = visibleScreenTracker.getPreviouslyVisibleScreen();
        if (!screenName.equals(previouslyVisibleScreen)) {
            span.setAttribute(DisneyOtel.LAST_SCREEN_NAME_KEY, previouslyVisibleScreen);
        }
    }
}
