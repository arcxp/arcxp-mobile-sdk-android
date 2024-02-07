package com.arcxp.video.util;

import android.content.Context;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.InputStream;

/**
 * @hide
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
