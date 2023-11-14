package com.arcxp.commons.image

import android.content.Context
import android.content.res.Resources
import com.arcxp.commons.util.ArcXPResizerV1
import com.arcxp.commons.util.Constants
import com.arcxp.commons.util.DependencyFactory
import com.arcxp.commons.util.Utils
import com.arcxp.content.models.Image
import com.arcxp.content.models.PromoItem
import com.arcxp.sdk.R
import kotlin.math.max

internal class CollectionImageUtil(val baseUrl: String, val context: Context) {

    private var resizerV1: ArcXPResizerV1 = DependencyFactory.createArcXPV1Resizer(
        baseUrl = baseUrl,
        resizerKey = context.getString(R.string.resizer_key)
    )
    private var resizerV2: ArcXPResizerV2 = DependencyFactory.createArcXPV2Resizer(
        baseUrl = baseUrl
    )

    val ourScreenSize = max(
        Resources.getSystem().displayMetrics.widthPixels,
        Resources.getSystem().displayMetrics.heightPixels
    )

    fun getScreenSize() = ourScreenSize

    private fun createThumbnail(url: String) = resizerV1.createThumbnail(url)
    private fun resizeWidth(url: String, width: Int) = resizerV1.resizeWidth(url = url, width = width)
    private fun resizeHeight(url: String, height: Int) = resizerV1.resizeHeight(url = url, height = height)

    private fun resizeWidth(image: Image, width: Int) = resizerV2.resizeWidth(image = image, width = width)
    private fun resizeHeight(image: Image, height: Int) = resizerV2.resizeHeight(image = image,height = height)

    private fun createThumbnailV2(url: String) = resizerV2.createThumbnail(url)
    private fun resizeWidthV2(url: String, width: Int) = resizerV2.resizeWidth(url = url, width = width)
    private fun resizeHeightV2(url: String, height: Int) = resizerV2.resizeHeight(url = url,height = height)

    fun imageUrl(image: Image): String {
        val maxScreenSize = getScreenSize()
        val imageHeight = image.height
        val imageWidth = image.width

        //if undesired (param = false) or we don't have height/width we do not want to resize
        val weShouldResize =
            (imageHeight != null) and
                    (imageWidth != null)
        if (weShouldResize) {
            //choose whether the maximum dimension is width or height
            val maxImageSize = max(imageHeight!!, imageWidth!!)

            //we want to scale preserving aspect ratio on this dimension
            val maxIsHeight = maxImageSize == imageHeight

            ///if image is smaller than device we do not want to resize
            if (maxImageSize >= maxScreenSize) {
                if (resizerV2.isValid(image)) {
                    return if (maxIsHeight) {
                        resizeHeight(image = image, height = maxScreenSize) ?: ""
                    } else {
                        resizeWidth(image = image, width = maxScreenSize) ?: ""
                    }
                } else if (resizerV1.isValid()){
                    val finalUrl = (image.additional_properties?.get(Constants.RESIZE_URL_KEY) as? String)?.substringAfter("=/") ?:""
                    if (finalUrl.isNotEmpty()) {
                        return if (maxIsHeight) {
                            resizeHeight(url = finalUrl, height = maxScreenSize) ?: ""
                        } else {
                            resizeWidth(url = finalUrl, width = maxScreenSize) ?: ""
                        }
                    }
                }
            } else return image.url ?: ""
        }
        return image.url ?: ""
    }

    fun imageUrl(item: PromoItem.PromoItemBasic): String {
        if (item.height != null && item.width != null) {
            //choose whether the maximum dimension is width or height
            val maxImageSize = Math.max(item.height, item.width)
            val maxScreenSize = getScreenSize()

            //we want to scale preserving aspect ratio on this dimension
            val maxIsHeight = maxImageSize == item.height!!

            ///if image is smaller than device we do not want to resize
            if (maxImageSize >= maxScreenSize) {
                if (item.type == "image" && resizerV2.isValid(item)) {
                    val url = resizerV2.getV2Url(item) ?: return ""
                    return if (maxIsHeight) {
                        resizeHeightV2(url = url, height = maxScreenSize)
                    } else {
                        resizeWidthV2(url = url, width = maxScreenSize)
                    }
                } else if (resizerV1.isValid()) {
                    val finalUrl = if (item.type == "video") {
                        item.url?.substringAfter("=/")
                    } else {
                        (item.additional_properties?.get(Constants.RESIZE_URL_KEY) as? String)?.substringAfter(
                            "=/"
                        )
                    } ?: ""
                    if (finalUrl.isNotEmpty()) {
                        return if (maxIsHeight) {
                            resizeHeight(url = finalUrl, height = maxScreenSize) ?: ""
                        } else {
                            resizeWidth(url = finalUrl, width = maxScreenSize) ?: ""
                        }
                    }
                }
            } else return item.url ?: ""
        }
        return item.url ?: ""
    }

    fun thumbnail(promoItem: PromoItem) : String {
        return if (promoItem.basic?.type == "image" && resizerV2.isValid(promoItem.basic)) {
            val url = resizerV2.getV2Url(promoItem.basic) ?: ""
            resizerV2.createThumbnail(url)
        } else if (promoItem.lead_art?.type == "image" && resizerV2.isValid(promoItem.lead_art)) {
            val url = resizerV2.getV2Url(promoItem.lead_art) ?: ""
            resizerV2.createThumbnail(url)
        } else if (promoItem.basic?.type == "video" && resizerV2.isValid(promoItem.basic)) {
            promoItem.basic.url?.let { createThumbnailV2(it.substringAfter("=/")) }
                ?: ""
        } else if (promoItem.basic?.type == "video" && resizerV1.isValid()) {
            promoItem.basic.url?.let { createThumbnail(it.substringAfter("=/")) }
                ?: ""
        } else {
            val url = promoItem.basic?.url ?: promoItem.lead_art?.url
                ?: (promoItem.basic?.additional_properties?.get(Constants.THUMBNAIL_RESIZE_URL_KEY) as? String
                ?: promoItem.lead_art?.additional_properties?.get(Constants.THUMBNAIL_RESIZE_URL_KEY) as? String
                ?: promoItem.lead_art?.promo_items?.basic?.additional_properties?.get(
                    Constants.THUMBNAIL_RESIZE_URL_KEY
                ) as? String)
                ?.let { Utils.createFullImageUrl(url = it) }
            return url ?: ""
        }
    }

    fun thumbnail(url: String) : String =
        if (resizerV1.isValid()) {
            resizerV1.createThumbnail(url.substringAfter("https://")) ?: url
        } else {
            url
        }

    fun fallback(promoItem: PromoItem): String {
        if (promoItem.basic?.type == "image" && resizerV2.isValid(promoItem.basic)) {
            val url = resizerV2.getV2Url(promoItem?.basic) ?: ""
            return resizerV2.createThumbnail(url)
        } else if (promoItem.lead_art?.type == "image" && resizerV2.isValid(promoItem.lead_art!!)) {
            val url = resizerV2.getV2Url(promoItem.lead_art) ?: ""
            return resizerV2.createThumbnail(url)
        } else if (promoItem.basic?.type == "video") {
            return promoItem.basic.url ?: ""
        } else {
            val url = promoItem.basic?.url ?: promoItem.lead_art?.url
                ?: (promoItem.basic?.additional_properties?.get(Constants.THUMBNAIL_RESIZE_URL_KEY) as? String
                ?: promoItem.lead_art?.additional_properties?.get(Constants.THUMBNAIL_RESIZE_URL_KEY) as? String
                ?: promoItem.lead_art?.promo_items?.basic?.additional_properties?.get(
                    Constants.THUMBNAIL_RESIZE_URL_KEY
                ) as? String)
                ?.let { Utils.createFullImageUrl(url = it) }
            return url ?: ""
        }
    }

    companion object {

    }
}