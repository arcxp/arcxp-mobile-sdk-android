package com.arcxp.sdk

import android.app.Application
import androidx.test.platform.app.InstrumentationRegistry
import com.arcxp.ArcXPMobileSDK
import com.arcxp.commerce.ArcXPCommerceConfig
import com.arcxp.content.ArcXPContentConfig
import org.junit.Before

class PaywallTests {

    val commerceUrl = "https://arcsales-arcsales-sandbox.api.cdn.arcpublishing.com"
    val org = "arcsales"
    val site = "arcsales"
    val baseURL = "https://arcsales-arcsales-sandbox.web.arc-cdn.net"
    val env = "sandbox"

    @Before
    fun setUp() {

        val context =
            (InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as Application)
        context.apply {
            val commerceConfig = ArcXPCommerceConfig.Builder()
                .setContext(this)
                //Base URLs provided by ArcXP admin
                .setBaseUrl(commerceUrl)
                .setBaseSalesUrl(commerceUrl)
                .setBaseRetailUrl(commerceUrl)
                //Will the users email be used as their username.
                .setUserNameIsEmail(false)
                .enableAutoCache(true)
                .usePaywallCache(true)
                .build()


            //Set the base URL for content.  Set the organization, site and environment.
            //These values can be gotten from your ArcXP admin
            ArcXPMobileSDK.initialize(
                application = this,
                site = site,
                org = org,
                environment = env,
                commerceConfig = commerceConfig,
                baseUrl = baseURL
            )
        }
    }
}