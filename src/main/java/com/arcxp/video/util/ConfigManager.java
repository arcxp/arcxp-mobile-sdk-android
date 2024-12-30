package com.arcxp.video.util;

import android.content.Context;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.InputStream;

/**
 * ConfigManager is a utility class responsible for loading and updating configuration settings from resource files within the ArcXP platform.
 * It provides methods to read configuration data from raw resource files and convert them into JSON objects.
 *
 * The class defines the following methods:
 * - updateAndLoadConfig: Updates and loads the configuration from a specified raw resource file.
 * - readConfigFromResources: Reads the configuration data from a raw resource file.
 * - readConfigFromStream: Reads the configuration data from an input stream and converts it to a JSON object.
 *
 * Usage:
 * - Use the provided static methods to manage configuration settings.
 *
 * Example:
 *
 * val config = ConfigManager.updateAndLoadConfig(context, R.raw.config)
 *
 * Note: This class is intended for internal use only and should not be exposed publicly.
 *
 * @method updateAndLoadConfig Updates and loads the configuration from a specified raw resource file.
 * @method readConfigFromResources Reads the configuration data from a raw resource file.
 * @method readConfigFromStream Reads the configuration data from an input stream and converts it to a JSON object.
 */
public class ConfigManager {
    private final static String TAG = ConfigManager.class.getSimpleName();

    public static JSONObject updateAndLoadConfig(Context context, int configRawFileResId) throws JSONException {
        JSONObject jsonObject = null;

        jsonObject = readConfigFromResources(context, configRawFileResId);

        return jsonObject;
    }


    private static JSONObject readConfigFromResources(Context context, int resId) {

        if(resId == -1) {
            // Return null if there is no local res config file.
            return null;
        }

        try {
            InputStream is = context.getResources().openRawResource(resId);
            return readConfigFromStream(context, is);
        } catch (JSONException e) {
            throw new RuntimeException("Local(raw) config parse error", e);
        }
    }

    private static JSONObject readConfigFromStream(Context context, InputStream is) throws JSONException {
        String configString = Utils.inputStreamToString(is);

        // Decrypt content if config is of type secure before passing to jsonObject.

        return new JSONObject(configString);
    }

}
