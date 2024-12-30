package com.arcxp.video.util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * PrefManager is a utility class responsible for managing shared preferences within the ArcXP platform.
 * It provides methods to save and retrieve string and boolean values from the shared preferences.
 *
 * The class defines the following methods:
 * - saveString: Saves a string value to the shared preferences.
 * - getString: Retrieves a string value from the shared preferences.
 * - saveBoolean: Saves a boolean value to the shared preferences.
 * - getBoolean: Retrieves a boolean value from the shared preferences.
 *
 * Usage:
 * - Use the provided static methods to manage shared preferences.
 *
 * Example:
 *
 * val isEnabled = PrefManager.getBoolean(context, PrefManager.IS_CAPTIONS_ENABLED, false)
 * PrefManager.saveBoolean(context, PrefManager.IS_CAPTIONS_ENABLED, true)
 *
 * Note: This class is intended for internal use only and should not be exposed publicly.
 *
 * @method saveString Saves a string value to the shared preferences.
 * @method getString Retrieves a string value from the shared preferences.
 * @method saveBoolean Saves a boolean value to the shared preferences.
 * @method getBoolean Retrieves a boolean value from the shared preferences.
 */
public class PrefManager {
    private static final String PREFERENCE = "mapPreference";
    public static final String IS_CAPTIONS_ENABLED = "prefIsCaptionsEnabled";

    public static String saveString(Context context, String key, String value) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFERENCE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.commit();
        return value;
    }

    public static String getString(Context context, String key, String defaultValue) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFERENCE, Context.MODE_PRIVATE);
        return sharedPreferences.getString(key, defaultValue);
    }

    public static boolean saveBoolean(Context context, String key, boolean value) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFERENCE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(key, value);
        editor.commit();
        return value;
    }

    public static boolean getBoolean(Context context, String key, boolean defaultValue) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFERENCE, Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean(key, defaultValue);
    }
}