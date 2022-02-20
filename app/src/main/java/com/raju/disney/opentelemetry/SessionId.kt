package com.raju.disney.opentelemetry

import io.opentelemetry.api.trace.TraceId
import io.opentelemetry.sdk.common.Clock
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

internal class SessionId constructor(private val clock: Clock = Clock.getDefault()) {

    private val value = AtomicReference<String>()

    @Volatile
    private var createTimeNanos: Long

    @Volatile
    private var sessionIdChangeListener: SessionIdChangeListener? = null

    //if this returns false, then another thread updated the value already.
    val sessionId: String
        get() {
            val currentValue = value.get()
            if (sessionExpired()) {
                val newId = createNewId()
                //if this returns false, then another thread updated the value already.
                if (value.compareAndSet(currentValue, newId)) {
                    createTimeNanos = clock.now()
                    sessionIdChangeListener?.onChange(currentValue, newId)
                }
                return value.get()
            }
            return currentValue
        }

    fun setSessionIdChangeListener(sessionIdChangeListener: SessionIdChangeListener?) {
        this.sessionIdChangeListener = sessionIdChangeListener
    }

    private fun sessionExpired(): Boolean {
        val elapsedTime = clock.now() - createTimeNanos
        return elapsedTime >= SESSION_LIFETIME_NANOS
    }

    override fun toString(): String {
        return value.get()
    }

    companion object {
        private val SESSION_LIFETIME_NANOS = TimeUnit.HOURS.toNanos(4)
        private fun createNewId(): String {
            val random = Random()
            //The OTel TraceId has exactly the same format as SessionId, so let's re-use it here, rather
            //than re-inventing the wheel.
            return TraceId.fromLongs(random.nextLong(), random.nextLong())
        }
    }

    init {
        value.set(createNewId())
        createTimeNanos = clock.now()
    }
}