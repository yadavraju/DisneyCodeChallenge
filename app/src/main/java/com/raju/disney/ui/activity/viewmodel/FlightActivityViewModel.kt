package com.raju.disney.ui.activity.viewmodel

import android.util.Log
import androidx.databinding.ObservableField
import androidx.lifecycle.viewModelScope
import com.raju.disney.api.repository.FlightRepository
import com.raju.disney.base.BaseViewModel
import com.raju.disney.data.FlightData
import com.raju.disney.opentelemetry.OtelConfiguration
import com.raju.disney.opentelemetry.OtelConfiguration.createSpan
import com.raju.disney.util.SingleLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import io.opentelemetry.api.common.AttributeKey
import io.opentelemetry.api.common.Attributes
import io.opentelemetry.api.trace.Span
import io.opentelemetry.api.trace.StatusCode
import io.opentelemetry.api.trace.Tracer
import io.opentelemetry.context.Context
import io.opentelemetry.context.propagation.ContextPropagators
import io.opentelemetry.context.propagation.TextMapSetter
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

const val TAG: String = "FlightActivityViewModel"

@HiltViewModel
class FlightActivityViewModel @Inject constructor(private val repository: FlightRepository) :
    BaseViewModel() {

    private val otelConfiguration = OtelConfiguration.getOpenTelemetry()
    private val tracer: Tracer = otelConfiguration.getTracer("FlightActivity")
    private val span: Span = tracer.createSpan("FlightActivityViewModel:api:http_request")

    private val loadingObservableField: ObservableField<Boolean> = ObservableField()
    val showErrorMessage: SingleLiveEvent<String?> by lazy { SingleLiveEvent() }
    val displayFlightData: SingleLiveEvent<FlightData> by lazy { SingleLiveEvent() }

    fun fetchFlightData() {
        viewModelScope.launch {
            try {
                span.makeCurrent().use {
                    val propagators: ContextPropagators = otelConfiguration.propagators
                    val textMapPropagator = propagators.textMapPropagator

                    val map: MutableMap<String, String> = HashMap()
                    val setter1 = TextMapSetter<MutableMap<String, String>> { map, key, value ->
                        map?.set(
                            key,
                            value
                        )
                    }
                    textMapPropagator.inject(Context.current(), map, setter1)
                    Log.e("Raju", "map1 " + map.keys)
                    Log.e("Raju", "map1 " + map.values)
                    span.addEvent("Loading api data")
                    loadingObservableField.set(true)
                    repository.getFlightData(map)
                        .catch { e ->
                            handleException(TAG, e, loadingObservableField)
                            val attributes = Attributes.of(
                                AttributeKey.stringKey(StatusCode.ERROR.name),
                                "/@GET/flight"
                            )
                            span.recordException(e, attributes)
                        }
                        .collect {
                            displayFlightData.value = it
                            loadingObservableField.set(false)
                            span.addEvent("Api data loaded: $it")
                        }
                }
            } finally {
                span.end()
            }
        }
    }

    override fun showExceptionMessage(message: String?) {
        showErrorMessage.value = message
    }
}