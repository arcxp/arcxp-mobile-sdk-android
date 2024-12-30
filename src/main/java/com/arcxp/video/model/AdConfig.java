package com.arcxp.video.model;

import androidx.annotation.Keep;

import java.io.Serializable;
import java.util.Objects;

/**
 * AdConfig is a model class that represents the configuration settings for advertisements within the ArcXP platform.
 * It includes properties for the advertisement configuration URL and a flag indicating whether ads are enabled.
 *
 * The class defines the following properties:
 * - adConfigUrl: The URL for the advertisement configuration.
 * - adEnabled: A boolean flag indicating whether advertisements are enabled.
 *
 * Usage:
 * - Create an instance of AdConfig to store and manage advertisement settings.
 * - Use the provided getter and setter methods to access and modify the advertisement configuration.
 *
 * Example:
 *
 * AdConfig adConfig = new AdConfig();
 * adConfig.setAdConfigUrl("https://example.com/adconfig");
 * adConfig.setAdEnabled(true);
 *
 * Note: Ensure that the adConfigUrl is a valid URL before using it in the application.
 *
 * @property adConfigUrl The URL for the advertisement configuration.
 * @property adEnabled A boolean flag indicating whether advertisements are enabled.
 * @method getAdConfigUrl Returns the URL for the advertisement configuration.
 * @method setAdConfigUrl Sets the URL for the advertisement configuration.
 * @method isAdEnabled Returns whether advertisements are enabled.
 * @method setAdEnabled Sets whether advertisements are enabled.
 * @method equals Checks if two AdConfig objects are equal.
 * @method hashCode Returns the hash code for the AdConfig object.
 */
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
