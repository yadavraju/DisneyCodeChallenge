package com.raju.disney.opentelemetry;

import android.os.Handler;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

class AnrWatcher implements Runnable {
    private final AtomicInteger anrCounter = new AtomicInteger();
    private final Handler uiHandler;
    private final Thread mainThread;
    private final Supplier<DisneyOtel> otelSupplier;

    AnrWatcher(Handler uiHandler, Thread mainThread, Supplier<DisneyOtel> otelSupplier) {
        this.uiHandler = uiHandler;
        this.mainThread = mainThread;
        this.otelSupplier = otelSupplier;
    }

    @Override
    public void run() {
        CountDownLatch response = new CountDownLatch(1);
        if (!uiHandler.post(response::countDown)) {
            //the main thread is probably shutting down. ignore and return.
            return;
        }
        boolean success;
        try {
            success = response.await(1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            return;
        }
        if (success) {
            anrCounter.set(0);
            return;
        }
        if (anrCounter.incrementAndGet() >= 5) {
            StackTraceElement[] stackTrace = mainThread.getStackTrace();
            otelSupplier.get().recordAnr(stackTrace);
            //only report once per 5s.
            anrCounter.set(0);
        }
    }
}
