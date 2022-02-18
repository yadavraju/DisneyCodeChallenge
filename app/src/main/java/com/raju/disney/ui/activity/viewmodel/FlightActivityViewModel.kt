package com.raju.disney.ui.activity.viewmodel

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
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

const val TAG: String = "FlightActivityViewModel"

@HiltViewModel
class FlightActivityViewModel @Inject constructor(private val repository: FlightRepository) :
    BaseViewModel() {

    private val tracer: Tracer = OtelConfiguration.getTracer("FlightActivity")

    private val loadingObservableField: ObservableField<Boolean> = ObservableField()
    val showErrorMessage: SingleLiveEvent<String?> by lazy { SingleLiveEvent() }
    val displayFlightData: SingleLiveEvent<FlightData> by lazy { SingleLiveEvent() }

    fun fetchFlightData() {
        viewModelScope.launch {
            val span: Span = tracer.createSpan("FlightActivityViewModel:api:http_request")
            try {
                span.makeCurrent().use {
                    span.addEvent("Loading api data")
                    loadingObservableField.set(true)
                    repository.getFlightData(OtelConfiguration.injectSpanContext())
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