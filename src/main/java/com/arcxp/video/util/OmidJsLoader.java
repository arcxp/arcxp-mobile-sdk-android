package com.arcxp.video.util;

import android.content.Context;
import android.content.res.Resources;


import com.arc.flagship.features.arcvideo.R;

import java.io.IOException;
import java.io.InputStream;

/**
 * OmidJsLoader - utility for loading the Omid JavaScript resource
 * @hide
 */

public final class OmidJsLoader {

	/**
	 * getOmidJs - gets the Omid JS resource as a string
	 * @param context - used to access the JS resource
	 * @return - the Omid JS resource as a string
	 */
	public static String getOmidJs(Context context) {
		Resources res = context.getResources();
		try (InputStream inputStream = res.openRawResource(R.raw.omsdk_v1)) {
			byte[] b = new byte[inputStream.available()];
			final int bytesRead = inputStream.read(b);
			return new String(b, 0, bytesRead, "UTF-8");
		} catch (IOException e) {
			throw new UnsupportedOperationException("Yikes, omid resource not found", e);
		}
	}
}
