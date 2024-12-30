package com.arcxp.video.util

import com.arcxp.commons.retrofit.NetworkController.client
import com.arcxp.commons.retrofit.NetworkController.moshiConverter
import com.arcxp.commons.util.MoshiController.moshi
import com.arcxp.video.service.AkamaiService
import com.arcxp.video.service.ArcMediaClientService
import com.arcxp.video.service.VirtualChannelService
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory


/**
 * RetrofitController is a utility object responsible for creating and configuring Retrofit service instances within the ArcXP platform.
 * It provides methods to create instances of various services such as ArcMediaClientService, AkamaiService, and VirtualChannelService.
 * The base URLs for these services are dynamically constructed based on the provided organization name, environment name, and base URL.
 *
 * The object defines the following methods:
 * - baseService: Creates an instance of ArcMediaClientService with the specified parameters.
 * - akamaiService: Creates an instance of AkamaiService with the specified parameters.
 * - virtualChannelService: Creates an instance of VirtualChannelService with the specified parameters.
 *
 * Usage:
 * - Use the provided methods to obtain service instances for making network calls.
 *
 * Example:
 *
 * val arcMediaClientService = RetrofitController.baseService("orgName", "environmentName", "baseUrl")
 * val akamaiService = RetrofitController.akamaiService("orgName", "environmentName", "baseUrl")
 * val virtualChannelService = RetrofitController.virtualChannelService("orgName", "environmentName", "baseUrl")
 *
 * Note: Ensure that the Retrofit instance is properly configured before using the service instances.
 *
 * @method baseService Creates an instance of ArcMediaClientService with the specified parameters.
 * @method akamaiService Creates an instance of AkamaiService with the specified parameters.
 * @method virtualChannelService Creates an instance of VirtualChannelService with the specified parameters.
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
    ): ArcMediaClientService {
        return Retrofit.Builder()
            .baseUrl(
                if (orgName.isNotBlank() and environmentName.isBlank()) {
                    //this logic is for 'staging' and any other org without environment
                    "https://video-api.$orgName.arcpublishing.com"
                } else {
                    "https://$baseUrl.video-api.arcpublishing.com"
                }
            )
            .validateEagerly(true)
            .addConverterFactory(moshiConverter)
            .client(client)
            .build()
            .create(ArcMediaClientService::class.java)
    }
    /**
     * @param orgName org string can be blank if using baseUrl
     * @param environmentName environment string (prod, sandbox, can be blank)
     * @param baseUrl should be formatted to org-env
     */
    fun akamaiService(
        orgName: String,
        environmentName: String,
        baseUrl: String
    ): AkamaiService {
        return Retrofit.Builder()
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
            .validateEagerly(true)
            .addConverterFactory(moshiConverter)
            .client(client)
            .build()
            .create(AkamaiService::class.java)
    }

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
            .addConverterFactory(moshiConverter)
            .client(client)
            .build()
            .create(VirtualChannelService::class.java)
    }
}
