package com.raju.disney.opentelemetry;

public class Config {

    private final boolean debugEnabled;
    private final String applicationName;
    private final boolean anrDetectionEnabled;
    private final boolean slowRenderingDetectionEnabled;

    private Config(Builder builder) {
        this.debugEnabled = builder.debugEnabled;
        this.applicationName = builder.applicationName;
        this.anrDetectionEnabled = builder.anrDetectionEnabled;
        this.slowRenderingDetectionEnabled = builder.slowRenderingDetectionEnabled;
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

    public boolean isSlowRenderingDetectionDisabled() {
        return !isSlowRenderingDetectionEnabled();
    }

    public boolean isSlowRenderingDetectionEnabled() {
        return slowRenderingDetectionEnabled;
    }

    public static class Builder {
        public boolean slowRenderingDetectionEnabled = true;
        public boolean anrDetectionEnabled = true;
        private boolean debugEnabled = false;
        private String applicationName;

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

        public Builder disableSlowRenderingDetection() {
            slowRenderingDetectionEnabled = false;
            return this;
        }
    }
}
