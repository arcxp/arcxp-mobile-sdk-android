package com.arcxp.video.util

import com.arcxp.video.service.ArcMediaClientService
import com.arcxp.video.service.GeoRestrictionService
import com.arcxp.video.service.VirtualChannelService
import com.arcxp.video.util.MoshiController.moshi
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory


/**
 * @suppress
 */
object RetrofitController {
    /**
     * @param orgName org string can be blank if using baseUrl
     * @param environmentName environment string (prod, sandbox, can be blank)
     * @param baseUrl should be formatted to org-env or org
     */
    fun baseService(
        orgName: String,
        environmentName: String,
        baseUrl: String
    ): ArcMediaClientService =
        Retrofit.Builder()
            .baseUrl(
                if (orgName.isNotBlank() and environmentName.isBlank()) {
                    //this logic is for 'staging' and any other org without environment
                    "https://video-api.$orgName.arcpublishing.com"
                } else {
                    "https://$baseUrl.video-api.arcpublishing.com"
                }
            )
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(ArcMediaClientService::class.java)
    /**
     * @param orgName org string can be blank if using baseUrl
     * @param environmentName environment string (prod, sandbox, can be blank)
     * @param baseUrl should be formatted to org-env
     */
    fun geoRestrictedService(
        orgName: String,
        environmentName: String,
        baseUrl: String
    ): GeoRestrictionService =
        Retrofit.Builder()
            .baseUrl(
                if (orgName.isBlank() and environmentName.isBlank()) {
                    //case of legacy creation using base url only ie tmg-prod
                    val baseUrlSplit = baseUrl.split("-")
                    val org = baseUrlSplit[0]
                    //this is to avoid npe if entered incorrectly without hyphen
                    val env = if (baseUrlSplit.size == 2) baseUrlSplit[1] else ""
                    "https://$org-config-$env.api.cdn.arcpublishing.com"
                } else {
                    //url is constructed with orgName and environmentName
                    "https://$orgName-config-$environmentName.api.cdn.arcpublishing.com"
                }
            )
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GeoRestrictionService::class.java)
    /**
     * @param orgName org string can be blank if using baseUrl
     * @param environmentName environment string (prod, sandbox, can be blank)
     * @param baseUrl should be formatted to org-env or org
     */
    fun virtualChannelService(
        orgName: String,
        environmentName: String,
        baseUrl: String
    ): VirtualChannelService {


        val baseUrlString = if (orgName.isBlank() and environmentName.isBlank()) {
            //case of legacy creation using base url only ie tmg-prod
            val baseUrlSplit = baseUrl.split("-")
            val org = baseUrlSplit[0]
            //this is to avoid npe if entered incorrectly without hyphen
            val env = if (baseUrlSplit.size == 2) baseUrlSplit[1] else ""
            //this logic is for 'staging' and any other org without environment
            "https://$org${if (env.isNotBlank()) "-$env" else ""}-vcx.video-api.arcpublishing.com"
        } else {
            //url is constructed with orgName and environmentName
            "https://$orgName-$environmentName-vcx.video-api.arcpublishing.com"
        }

        return Retrofit.Builder()
            .baseUrl(baseUrlString)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(VirtualChannelService::class.java)
    }
}
