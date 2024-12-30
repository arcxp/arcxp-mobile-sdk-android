# Content Getting Started

This article covers getting started with Arc XP’s Android SDK for Arc XP Content module.

Content Module is used to provide the reader-facing data from Arc XP. The SDK returns the denormalized view of all the documents created in Arc XP applications such as Photo Center, WebSked, and Video Center.

## Prerequisites

The SDK accesses data using outbound feeds. The URL to access this is built using your companies assigned Arc organization name, site name and the environment you are trying to access (sandbox or production). These values can be obtained from your ArcXP contact.

The outbound feeds setup can be tested by navigating to the following URL using a web browser:

_{base Url provided}/arc/outboundfeeds/navigation/default_

If this URL returns JSON then the outbound feeds is set up. If it returns an error then the outbound feeds are not set up or are set up incorrectly and the Content Module will not work.

If they are not set up then contact your ArcXP contact to create an ACS ticket

If they are setup continue and follow both of these instructions closely:

[Backend Setup For Mobile SDK](back-end-setup-for-mobile-sdk.md)

[Resolver Setup For Mobile SDK](mobile-sdk-resolver-setup.md)

after following all steps there, you can continue on:

## Getting access to the sample apps

the sample app repositories can be found here:

[https://github.com/arcxp/the-arcxp-android](https://github.com/arcxp/the-arcxp-android)

[https://github.com/arcxp/the-arcxp-tv-android](https://github.com/arcxp/the-arcxp-tv-android)


## Initialize/Install SDK

Visit the [Mobile SDK - Android Initialization](getting-started-initialization.md) article and ensure to configure the content config object described there during initialization.

## Using the SDK

The Content Module allows access to content data through the contentManager() method in the SDK. A call to get a story using a given story ID would be done as follows:

_ArcXPMobileSDK.contentManager().getStory(id = <story id>)_

All contentManager calls are made using a similar pattern, but have a few optional parameters such as from, size, and shouldIgnoreCache.

We have suspend overloads if you prefer to use that rather than live data.

Results are returned through three different implementations giving the client code versatility in how it is architected. The three implementations are:

_**1 - Callback**_

Each non-suspend method has an optional final listener parameter of interface type ArcXPContentCallback that will return the results of the call.

The interface has the following definition:

```Kotlin
interface ArcXPContentCallback {
    fun onGetCollectionSuccess(response: Map<Int, ArcXPCollection>) {}
    fun onGetJsonSuccess(response: String) {}
    fun onGetContentSuccess(response: ArcXPContentElement) {}
    fun onGetStorySuccess(response: ArcXPStory) {}
    fun onSearchSuccess(response: Map<Int, ArcXPContentElement>) {}
    fun onGetSectionsSuccess(response: List<ArxXPSection>) {}
    fun onError(error: ArcXPContentError) {}
}
```

Each of these methods returns data in the following situations:

- onGetCollectionSuccess - Returns a generic collection.
- onGetJsonSuccess - Returns raw JSON of any call such as getStory(), getGallery(), getVideo(), getCollection().
- onGetStorySuccess - Returns single result for story content call.
- onGetContentSuccess - Returns single result for any content call such as getGallery(), getVideo() as an ArcXPContentElement.
- onSearchSuccess - Returns results for a search call.
- onGetSectionSuccess - Returns the results from a call to getSectionList() fetching your navigation from the provided site service hierarchy endpoint during configuration.
- onError - Returns if any of the call ends with an error condition.

Implementation will be as follows:

```kotlin
ArcXPMobileSDK.contentManager().getArcXPStory(id, object: ArcXPContentCallback {
    override fun onGetStorySuccess(response: Map<Int, ArcXPContentElement>) {
        //display the story
    }    
    
    override fun onError(error: ArcXPContentError) {
        //handle error
    }

})
```

_**2 - LiveData**_

Data can also be returned through the observer pattern. Any content call returns an instance of a LiveData object containing the results. The signature for this object is

_LiveData<Either<ArcXPException, return type>>_

where the return type is the same as what would be returned through the corresponding callback method.

Implementation will be as follows:

```kotlin
ArcXPMobileSDK.contentManager().getArcXPStory(id).observe(viewLifecycleOwner) {
    when (it) {
        is Success -> {
            //display story
        }
        is Failure -> {
            //handle error
        }
    }
}
```

_**3 - Coroutines**_

The SDK also supports the use of coroutines. Separate methods exist within the SDK that are coroutine equivalents of the other content retrieval methods getStory(), getGallery(), and others. These are all suffixed with _Suspend_ for instance the coroutine version of getCollection() would be getCollectionSuspend().

An example implementation is

```kotlin
viewLifecycleOwner.lifecycleScope.launch {
       val result = vm.getStorySuspend(id = path!!, from = from, size = 20)
        when (result) {
             is Success -> {
                   //show story
             }
             is Failure -> {
                  //handle error
            }
         }
}
```

_**Caching**_

The Content Module will provide caching of content data using the local SQL lite database. When data is retrieved it is automatically stored in the cache. When data is requested, the cache is checked for the data before requesting it from the server.

The client will be able to control the following:

- Cache max size - Maximum amount of memory to allocate for the cache. Default is 1024 MB. We perform a size check per insertion, and purge oldest records as needed.
- Time until item in cache tries to update - The amount of time an item can be in the cache before the SDK will retrieve an updated version. This value is specified in minutes and defaulted to 5 minutes. This doesn't happen automatically but when the content is accessed.
- Per call the cache can be disabled, using an optional parameter available on each call.

_**Preloading**_

The SDK has the ability to preload all elements returned as part of a collection when the collection is retrieved. The default value is true. The effect here is all of articles fetched with each collection-full call are stored in the cache for offline usage. Does not download images and videos of preloaded articles.

_**Pagination**_

The SDK returns data in pages as to not return too much data at a single time. Search and Collection calls have optional parameters to return a starting value and a page size. The user can specify the from parameter which indicates which record to start returning and then a size parameter to tell how many records to return starting with the _from_ value. Data that is returned in pages will return a Map of data instead of a list where the key value is the index of the result. This will allow the client code to know the index of the last value returned in order to specify a starting index for the next query.

_**Method Parameters**_

Each method will the optional defaulted parameters shouldIgnoreCache. The default value is set to false which means that if content has been stored in the cache then that will be returned instead of loading fresh content from the server. If the value is set to true then any cached objects will be ignored and all data returned will be retrieved from the ArcXP backend. Data fetched when this is enabled will not be stored in cache as well. Methods that return JSON do not have this parameter.

If the SDK detects that the app is offline, the app should serve cache content for the item being requested. If the item does not exist in the cache an error is returned.

If the SDK detects that the app is online and the request to the backend times out, the SDK returns cached data. If the item does not exist in the cache then an error will be returned. If cached data is returned an log item will also returned indicating the problem

_**Content Methods**_

There are multiple version of each content retrieval method based upon how the data is returned or in what format the data is returned. These different return types are:

- Object - These methods return the content data as a fully deserialized object.
- JSON string - Methods with the “AsJson” suffix attached to the name will return the content data as a raw JSON string. This puts the burden of deserialization on the client code and is unlikely to be used except for testing.

The different ways in which the data can be returned are:

- Callback - These methods will return the data as a fully deserialized object either through the callback listener provided in the parameter list or through the LiveData object returned as a result of the method call.
- Observer - These methods will return the data through the LiveData object returned as a result of the method call.
- Coroutines - Methods have overloads, suffixed with “Suspend”, which use suspend functions to return the data directly. It will be up to the client code to properly set up the coroutine scope. SDK suspend functions do not block the calling thread, as is convention.

**getSectionList / getSectionListSuspend / getSectionListAsJson / getSectionListAsJsonSuspend**

Returns a list of section headers used for navigation. The section list will use the base URL appended with the navigation endpoint value specified in the ArcXPContentConfig object to retrieve the data. This call returns an ArcXPSection object that has the following definitions:

```kotlin
data class ArcXPSection(
    @Json(name = "_id") val id: String,
    @Json(name = "_website") val website: String,
    @Json(name = "node_type") val type: String,
    val name: String?,
    @Json(name = "navigation") val navigation: Navigation,
    @Json(name = "children") val sections: List<ArcXPSection>?
) 
```

```kotlin
data class Navigation(
    @Json(name = "nav_title") val nav_title: String
)
```

Parameters: None

Returns: Either<ArcXPException, List<ArcXPSection>> or Either<ArcXPException, String>

**getCollection / getCollectionSuspend / getCollectionAsJson / getCollectionAsJsonSuspend**

Returns a collection of results. The values are returned in pages so the from and size values can be specified in the call. It returns a map of ArcXPCollection objects which have the following definition:

```kotlin
data class ArcXPCollection(
    @Json(name = "_id") val id: String,
    @Json(name = "headlines") val headlines: Headlines,
    val description: Description?,
    val credits: Credits?,
    @Json(name = "promo_items") val promoItem: PromoItem?,
    @Json(name = "display_date") val modified_on: Date?,
    @Json(name = "publish_date") val publishedDate: Date?
)
```

Parameters: id - String value of the collection ID.

Returns: Either<ArcXPException, Map<Int, ArcXPCollection>>> or Either<ArcXPException, Map<Int, String>> where the map key is the index value of the map value object.

**getStory**

Returns a story result. The returned object is an ArcXPStory object which has the following definition:

```kotlin
data class ArcXPStory{
    val _id: String?,
    val type: String,
    val version: String?,
    val alignment: String?, 

     // dates
    val created_date: Date?,
    val last_updated_date: Date?,
    val publish_date: Date?,
    val first_publish_date: Date?,
    val display_date: Date?, 

     // location / language
    val geo: Geo?,
    val language: String?,
    val location: String?,
    val address: Address?, 
    
    val content_elements: List<StoryElement>?,

    val related_content: Map<String, *>?,
    val publishing: Publishing?,
    val revision: Revision?,
    val website: String?,
    val websites: Map<String, SiteInfo>?,
    val website_url: String?,
    val short_url: String?,
    val channels: List<String>?,
    val owner: Owner?,
    val credits: Credits?,
    val vanity_credits: Credits?,
    val editor_note: String?,
    val taxonomy: Taxonomy?,
    val copyright: String?,
    val description: Description?,
    val headlines: Headline?,
    val subheadlines: Headline?,
    val label: Label?,
    @Json(name = "promo_items") val promoItem: PromoItem?,
    val content: String?,
    val canonical_url: String?,
    val canonical_website: String?,
    val source: Source?,
    val subtype: String?,
    val planning: Planning?,
    val pitches: Pitches?,
    val syndication: Syndication?,
    val distributor: Distributor?,
    val tracking: Any?,
    val comments: Comments?,
    val slug: String?,
    val content_restrictions: ContentRestrictions?,
    val content_aliases: List<String>?,
    val corrections: List<Correction>?,
    val rendering_guides: List<RenderingGuide>?,
    val status: String?,
    val workFlow: WorkFlow?,
    val additional_properties: Map<String, *>?
}
```

Parameters: id- String value of the story ID, listener- optional callback, shouldIgnoreCache optional, default false

Returns: Either<ArcXPException, ArcXPStory>

Particularly of note here is the content_elements field, which contains the nested story elements. These classes can be one of:

Gallery, Video, Image, Text, Code, Correction, CustomEmbed, Divider, ElementGroup, Endorsement, Header, InterstitialLink, LinkList, StoryList, NumericRating, Quote, RawHTML, Table, OembedResponse

_The Following Calls return a generic ArcXPContentElement, which the ANS type can be determined from the ‘type’ field:_

```kotlin
data class ArcXPContentElement(
    val additional_properties: AdditionalProperties?,
    val created_date: String?,
    val display_date: String?,
    val first_publish_date: String?,
    val last_updated_date: String?,
    val owner: Owner?,
    val publish_date: Date?,
    val publishing: Publishing?,
    val revision: Revision?,
    val type: String,
    val version: String?,
    val _id: String?,
    val website: String?,
    val address: Address?,
    val content_elements: List<ArcXPContentElement>?,
    val caption: String?,
    val credits: Credits?,
    val geo: Geo?,
    val height: Int?,
    val width: Int?,
    val licensable: Boolean?,
    val newKeywords: String?,
    val referent_properties: ReferentProperties?,
    val selectedGalleries: List<String>?,
    val subtitle: String?,
    val taxonomy: Taxonomy?,
    val url: String?,
    val copyright: String?,
    val description: Description?,
    val headlines: Headline?,
    val language: String?,
    val location: String?,
    @Json(name = "promo_items") val promoItem: PromoItem?,
    val video_type: String?,
    val canonical_url: String?,
    val subtype: String?,
    val content: String?,
    val embed_html: String?,
    val subheadlines: Subheadlines?,
    val streams: List<Streams>?
)
```

**getVideo**

Returns a video result. The returned object is an ArcXPContentElement object with type=“video”

Parameters: id- String value of the video ID, listener- optional callback, shouldIgnoreCache optional, default false

Returns: Either<ArcXPException, ArcXPContentElement>

**getGallery**

Returns a gallery result. The returned object is an ArcXPContentElement object with type=“gallery”

Parameters: id- String value of the gallery ID, listener- optional callback, shouldIgnoreCache optional, default false

Returns: Either<ArcXPException, ArcXPContentElement>

**search / searchSuspend / searchVideo / searchVideoSuspend**

Returns search results based on a single tag(default, or however you set up search resolver). The object returned is an ArcXPContentElement. Returns any ANS type

Parameters: query- String keyword, listener- optional callback, shouldIgnoreCache optional, default false

Returns: Either<ArcXPException, Map<Int, ArcXPContentElement>>

**getContentSuspend / getContentAsJsonSuspend**

This returns an ArcXPContentElement object of any type.

Parameters: id- String value of the gallery ID, shouldIgnoreCache optional, default false

Returns: Either<ArcXPException, ArcXPContentElement>

_**Extension Functions**_

There are functions built-in to some classes that allow you quick access to data without needing to go through string manipulation or converting data types. You can still grab these properties manually if you want to manipulate the data your own way. The 3 classes that contain extension functions are ArcXPCollection, ArcXPStory, ArcXPContentElement. These functions are:

- date() - Returns 'MMM dd, yyyy' (Jan 31, 2022)
- title() - Returns the basic property value inside of Headlines
- description() - Returns the basic property value inside of Description
- author() - Returns the name inside the first element of credits
- fallback() - Returns the lowest dimensions of an image available
- imageUrl() - Returns a resized url if image is larger than device.
- thumbnail() - Returns a thumbnail version of the image. _(ArcXPCollection & ArcXPContentElement Only)_
- subheadline() - Returns basic property value inside Subheadlines. _(ArcXPStory Only)_
- url() - Returns a url that can be shared / open in a browser
