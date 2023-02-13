package com.arcxp.video.util;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.webkit.WebView;

import com.arcxp.video.ArcMediaPlayerConfig;
import com.arcxp.video.model.AdVerification;
import com.arcxp.video.model.JavascriptResource;
import com.arcxp.video.service.AdUtils;
import com.iab.omid.library.washpost.Omid;
import com.iab.omid.library.washpost.adsession.AdSession;
import com.iab.omid.library.washpost.adsession.AdSessionConfiguration;
import com.iab.omid.library.washpost.adsession.AdSessionContext;
import com.iab.omid.library.washpost.adsession.CreativeType;
import com.iab.omid.library.washpost.adsession.ImpressionType;
import com.iab.omid.library.washpost.adsession.Owner;
import com.iab.omid.library.washpost.adsession.Partner;
import com.iab.omid.library.washpost.adsession.VerificationScriptResource;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.Buffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

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
