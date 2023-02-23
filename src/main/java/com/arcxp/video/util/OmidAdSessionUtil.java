package com.arcxp.video.util;

import android.content.Context;

import com.arcxp.commons.util.Constants;
import com.arcxp.video.ArcMediaPlayerConfig;
import com.arcxp.video.model.AdVerification;
import com.arcxp.video.model.JavascriptResource;
import com.iab.omid.library.washpost.Omid;
import com.iab.omid.library.washpost.adsession.AdSession;
import com.iab.omid.library.washpost.adsession.AdSessionConfiguration;
import com.iab.omid.library.washpost.adsession.AdSessionContext;
import com.iab.omid.library.washpost.adsession.CreativeType;
import com.iab.omid.library.washpost.adsession.ImpressionType;
import com.iab.omid.library.washpost.adsession.Owner;
import com.iab.omid.library.washpost.adsession.Partner;
import com.iab.omid.library.washpost.adsession.VerificationScriptResource;

import java.net.MalformedURLException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * AdSessionUtil
 * @hide
 */

public final class OmidAdSessionUtil {

	public static AdSession getNativeAdSession(Context context, ArcMediaPlayerConfig config, List<AdVerification> verifications) throws MalformedURLException {
        	ensureOmidActivated(context);

		AdSessionConfiguration adSessionConfiguration =
			AdSessionConfiguration.createAdSessionConfiguration(CreativeType.VIDEO,
				ImpressionType.VIEWABLE,
				Owner.NATIVE,
				Owner.NATIVE, false);

		Partner partner = Partner.createPartner(config.getOmidPartnerName(), config.getOmidVersionName());

		List<VerificationScriptResource> verificationScripts = new ArrayList<VerificationScriptResource>();

		for (AdVerification ad: verifications) {
			for (JavascriptResource r: ad.getJavascriptResource())
				if (r.getApiFramework().equals("omid")) {
					URI uri = URI.create(r.getUri());
					VerificationScriptResource resource = VerificationScriptResource.createVerificationScriptResourceWithParameters(ad.getVendor(),
							uri.toURL(), ad.getVerificationParameters());
					verificationScripts.add(resource);
				}
		}
		AdSessionContext adSessionContext = AdSessionContext.createNativeAdSessionContext(partner, Constants.OMIDJS, verificationScripts, null, null);
		return AdSession.createAdSession(adSessionConfiguration, adSessionContext);
	}

    /**
     * Lazily activate the OMID API.
     *
     * @param context any context
     */
	private static void ensureOmidActivated(Context context) {
        Omid.activate(context.getApplicationContext());
    }
}
