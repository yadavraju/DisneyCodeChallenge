package com.raju.disney.opentelemetry

class Config private constructor(builder: Builder) {

    val isDebugEnabled: Boolean by lazy { builder.debugEnabled }
    val applicationName: String by lazy { builder.applicationName }
    val isAnrDetectionEnabled: Boolean by lazy { builder.anrDetectionEnabled }
    val oltpExporterEndPoint: String by lazy { builder.oltpExporterEndPoint }
    val jaegerExporterEndPoint: String by lazy { builder.jaegerExporterEndPoint }

    class Builder {
        var anrDetectionEnabled = true
        var debugEnabled = false
        lateinit var applicationName: String
        lateinit var oltpExporterEndPoint: String
        lateinit var jaegerExporterEndPoint: String

        fun build(): Config {
            return Config(this)
        }

        fun debugEnabled(enable: Boolean): Builder {
            debugEnabled = enable
            return this
        }

        fun applicationName(applicationName: String): Builder {
            this.applicationName = applicationName
            return this
        }

        fun anrDetectionEnabled(enable: Boolean): Builder {
            anrDetectionEnabled = enable
            return this
        }

        fun oltpExporterEndPoint(oltpExporterEndPoint: String): Builder {
            this.oltpExporterEndPoint = oltpExporterEndPoint
            return this
        }

        fun jaegerExporterEndPoint(jaegerExporterEndPoint: String): Builder {
            this.jaegerExporterEndPoint = jaegerExporterEndPoint
            return this
        }
    }

    companion object {
        fun builder(): Builder = Builder()
    }
}