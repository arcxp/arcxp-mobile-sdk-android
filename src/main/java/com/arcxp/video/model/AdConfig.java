package com.arcxp.video.model;

import androidx.annotation.Keep;

import java.io.Serializable;
import java.util.Objects;

@Keep
public class AdConfig implements Serializable {
    public String adConfigUrl;
    public boolean adEnabled;

    public String getAdConfigUrl() {
        return adConfigUrl;
    }

    public void setAdConfigUrl(String adConfigUrl) {
        this.adConfigUrl = adConfigUrl;
    }

    public boolean isAdEnabled() {
        return adEnabled;
    }

    public void setAdEnabled(boolean adEnabled) {
        this.adEnabled = adEnabled;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AdConfig adConfig = (AdConfig) o;
        return adEnabled == adConfig.adEnabled && Objects.equals(adConfigUrl, adConfig.adConfigUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(adConfigUrl, adEnabled);
    }
}
