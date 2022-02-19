package com.raju.disney.opentelemetry;

import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import io.opentelemetry.api.trace.TraceId;
import io.opentelemetry.sdk.common.Clock;

class SessionId {
    private static final long SESSION_LIFETIME_NANOS = TimeUnit.HOURS.toNanos(4);

    private final Clock clock;
    private final AtomicReference<String> value = new AtomicReference<>();
    private volatile long createTimeNanos;
    private volatile SessionIdChangeListener sessionIdChangeListener;

    SessionId() {
        this(Clock.getDefault());
    }

    SessionId(Clock clock) {
        this.clock = clock;
        value.set(createNewId());
        createTimeNanos = clock.now();
    }

    private static String createNewId() {
        Random random = new Random();
        //The OTel TraceId has exactly the same format as SessionId, so let's re-use it here, rather
        //than re-inventing the wheel.
        return TraceId.fromLongs(random.nextLong(), random.nextLong());
    }

    String getSessionId() {
        String currentValue = value.get();
        if (sessionExpired()) {
            String newId = createNewId();
            //if this returns false, then another thread updated the value already.
            if (value.compareAndSet(currentValue, newId)) {
                createTimeNanos = clock.now();
                if (sessionIdChangeListener != null) {
                    sessionIdChangeListener.onChange(currentValue, newId);
                }
            }
            return value.get();
        }
        return currentValue;
    }

    void setSessionIdChangeListener(SessionIdChangeListener sessionIdChangeListener) {
        this.sessionIdChangeListener = sessionIdChangeListener;
    }

    private boolean sessionExpired() {
        long elapsedTime = clock.now() - createTimeNanos;
        return elapsedTime >= SESSION_LIFETIME_NANOS;
    }

    @Override
    public String toString() {
        return value.get();
    }
}
