package com.raju.disney.opentelemetry;

public class Config {

    private final boolean debugEnabled;
    private final String applicationName;
    private final boolean anrDetectionEnabled;
    private final String oltpExporterEndPoint;
    private final String jaegerExporterEndPoint;

    private Config(Builder builder) {
        this.debugEnabled = builder.debugEnabled;
        this.applicationName = builder.applicationName;
        this.anrDetectionEnabled = builder.anrDetectionEnabled;
        this.oltpExporterEndPoint = builder.oltpExporterEndPoint;
        this.jaegerExporterEndPoint = builder.jaegerExporterEndPoint;
    }

    public boolean isDebugEnabled() {
        return debugEnabled;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public static Builder builder() {
        return new Builder();
    }

    public boolean isAnrDetectionEnabled() {
        return anrDetectionEnabled;
    }

    public String getOltpExporterEndPoint() {
        return oltpExporterEndPoint;
    }

    public String getJaegerExporterEndPoint() {
        return jaegerExporterEndPoint;
    }


    public static class Builder {
        public boolean slowRenderingDetectionEnabled = true;
        public boolean anrDetectionEnabled = true;
        private boolean debugEnabled = false;
        private String applicationName;
        private String oltpExporterEndPoint;
        private String jaegerExporterEndPoint;

        public Config build() {
            return new Config(this);
        }

        public Builder debugEnabled(boolean enable) {
            this.debugEnabled = enable;
            return this;
        }

        public Builder applicationName(String applicationName) {
            this.applicationName = applicationName;
            return this;
        }

        public Builder anrDetectionEnabled(boolean enable) {
            this.anrDetectionEnabled = enable;
            return this;
        }

        public Builder oltpExporterEndPoint(String oltpExporterEndPoint) {
            this.oltpExporterEndPoint = oltpExporterEndPoint;
            return this;
        }

        public Builder jaegerExporterEndPoint(String jaegerExporterEndPoint) {
            this.jaegerExporterEndPoint = jaegerExporterEndPoint;
            return this;
        }
    }
}
