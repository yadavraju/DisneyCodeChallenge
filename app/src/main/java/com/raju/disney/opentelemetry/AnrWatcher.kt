package com.raju.disney.opentelemetry

import android.os.Handler
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import java.util.function.Supplier

internal class AnrWatcher(
    private val uiHandler: Handler,
    private val mainThread: Thread,
    private val oTelSupplier: Supplier<DisneyOTel>
) : Runnable {

    private val anrCounter by lazy { AtomicInteger() }

    override fun run() {
        val response = CountDownLatch(1)
        if (!uiHandler.post { response.countDown() }) {
            //the main thread is probably shutting down. ignore and return.
            return
        }
        val success: Boolean = try {
            response.await(1, TimeUnit.SECONDS)
        } catch (e: InterruptedException) {
            return
        }
        if (success) {
            anrCounter.set(0)
            return
        }
        if (anrCounter.incrementAndGet() >= 5) {
            val stackTrace = mainThread.stackTrace
            oTelSupplier.get().recordAnr(stackTrace)
            //only report once per 5s.
            anrCounter.set(0)
        }
    }
}