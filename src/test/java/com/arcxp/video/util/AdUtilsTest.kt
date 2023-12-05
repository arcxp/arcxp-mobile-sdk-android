package com.arcxp.video.util

import com.arcxp.video.model.ArcAd
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull
import org.junit.Test

class AdUtilsTest {

    @Test
    fun `all not null`() {
        val arcAd = ArcAd()
        arcAd.clickthroughUrl = "url"
        arcAd.adTitle = "title"
        arcAd.adDuration = 100.0
        arcAd.adId = "id"

        val result = AdUtils.createArcAd(arcAd)

        assertNotNull(result)
    }

    @Test
    fun `clickthroughUrl null`() {
        val arcAd = ArcAd()
        arcAd.clickthroughUrl = null
        arcAd.adTitle = "title"
        arcAd.adDuration = 100.0
        arcAd.adId = "id"

        val result = AdUtils.createArcAd(arcAd)

        assertNull(result)
    }

    @Test
    fun `title null`() {
        val arcAd = ArcAd()
        arcAd.clickthroughUrl = "url"
        arcAd.adTitle = null
        arcAd.adDuration = 100.0
        arcAd.adId = "id"

        val result = AdUtils.createArcAd(arcAd)

        assertNull(result)
    }

    @Test
    fun `duration null`() {
        val arcAd = ArcAd()
        arcAd.clickthroughUrl = "url"
        arcAd.adTitle = "title"
        arcAd.adDuration = null
        arcAd.adId = "id"

        val result = AdUtils.createArcAd(arcAd)

        assertNull(result)
    }

    @Test
    fun `id null`() {
        val arcAd = ArcAd()
        arcAd.clickthroughUrl = "url"
        arcAd.adTitle = "title"
        arcAd.adDuration = 100.0
        arcAd.adId = null

        val result = AdUtils.createArcAd(arcAd)

        assertNull(result)
    }
}