

# Using the Commerce Module Paywall

The Arcxp Mobile SDK Commerce module includes a Paywall evaluator that allows you to easily manage how content your readers can consume before they need to register or subscribe. The paywall is accessed through an evaluation function that can be called to determine if a page should be shown given the page parameters and the currently active paywall rules. It isn’t a requirement that paywall evaluation be called on a page.

The paywall evaluator considers all available facts, such as the article consumption history and properties of the current story, to determine if any of the rules are currently exhausted. The paywall script also updates the content consumption history and saves it to shared preferences. If any of the rules are exhausted, the evaluator will return the first exhausted rule and other information. It’s up you to design and build what happens next — Arc Commerce paywall simply returns a summation based on the facts at hand and you have complete control over the rest of the user experience.

To evaluate a page using the Paywall use one of the following methods:

```kotlin
evaluatePage(pageId: String, 
             contentType: String?, 
             contentSection: String?, 
             deviceClass: String?, 
             otherConditions: HashMap<String, String>?, 
             entitlements: EntitlementsResponse? = null, 
             listener: ArcxpPageviewListener)
             
evaluatePage(pageviewData: ArcxpPageviewData, 
             entitlements: EntitlementResponse? = null, 
             currentTime: Long? = null, 
             listener: ArcxpPageviewListener)
             
evaluatePage(pageviewData: ArcxpPageviewData, 
             listener: ArcxpPageviewListener)
```

The parameters for each of these calls are:

- **pageId** - This is a unique page identifier string. Once a page has passed a paywall rule it can be viewed again even if the page counter is above the budget specified in the rule. Therefore each page id must be unique otherwise if a page with the same ID has passed a rule the passed in page will pass the same rule.

- **contentType** - This is the Content Criteria condition specification. If it is null then this criteria will not be part of this page and the rules specifying this will note be flagged. It must be a string that matches the values specified in the Content Criteria section of the rule builder. Only a single entry can be specified in this field. Do not concatenate multiple values into a single string.

- **contentSection** - This is the Content Criteria condition specification. If it is null then this criteria will not be part of this page and the rules specifying this will note be flagged. It must be a string that matches the values specified in the Content Criteria section of the rule builder. Only a single entry can be specified in this field. Do not concatenate multiple values into a single string.

- **deviceClass** - This is the Audience Criteria condition specification. If it is null then this criteria will not be part of this page and the rules specifying this will note be flagged. It must be a string that matches the values specified in the Audience Criteria section of the rule builder. Only a single entry can be specified in this field. Do not concatenate multiple values into a single string.

- **otherConditions** - This field allows for the entry of any other conditions other than contentType, contentSection or deviceClass. For each entry in the hashmap, the key/value is the name of the condition and the value for the specified condition.

- **entitlements** - This is the list of entitlements for the logged in user. This parameter is optional. If it is null or omitted then the entitlements will be loaded from using the server during processing of the paywall algorithm. A value can be passed in as an EntitlementResponse object which has the following format:

```kotlin
data class EntitlementsResponse(val skus: List<Sku>, val edgescape: Edgescape?)
data class Sku(val sku: String)
data class Edgescape(val city: String?, val continent: String?, val georegion: String?, val dma: String?, val country_code: String?)
```

The only value that needs to be populated in the EntitlementsResponse object is the `skus` field. All others will be ignored. It is recommended that this field be left as null. The only reason to pass in entitlements information is if the app wants to do its own entitlement management rather than having it managed by the SDK or if the app wants to grant the user an entitlement that would not be returned by a call to the Entitlements API.

- **currentTime** - This optional field is provided so that the client app can pass in a time other than the current system time. This is an unlikely scenario and would most likely only occur if the app was implementing some sort of test scenario. If this value is not passed in or set to null then the current system time will be used.

- **pageviewData** - This is an ArcxpPageviewData object that encapsulates the page ID and condition data for a page. The format for this object is:

```kotlin
data class ArcxpPageviewData(val pageId: String, val conditions: HashMap<String, String>)
```

The condition object will be a hashmap with each entry key/value being the name of the condition and the value for the specified condition the same as is done in the otherConditions parameter.

- **listener** - This is an ArcxpPageviewDataListener object used to return results of the evaluation method. The format of this object is:

```kotlin
abstract class ArcxpPageviewListener {
    open fun onInitializationResult(success: Boolean) {}
    open fun onEvaluationResult(response: ArcxpPageviewEvaluationResult) {} 
}
```

Since this is an abstract class rather than an interface it is not necessary to implement both methods. Only implement onInitializationResult() if the client is interested in the initialization status of the paywall, which will involve successfully loading the paywall rules and the user entitlements. The results of the page evaluation will be returned through the onEvaluationResult() method. The format of this object is:

```kotlin
data class ArcxpPageviewEvaluationResult(val pageId: String, val show: Boolean, val campaign: String? = null)
```

The fields of this object are:

- **pageId** - The ID of the page being evaluated.

- **show** - A true returned means that the page can be shown to the user. A false indicates that one or more paywall rules has determined that the page should not be shown.

- **campaign** - The campaign code of the paywall rule that triggered the false value for show. If the show value is true then the campaign code will be null. If more than 1 paywall rule was triggered then the campaign code of the first rule will be returned.

### Example

```kotlin
ARcXPMobileSDK.commerceManager.evaluatePage(pageId = myPageId, 
                                  contentType = "story",
                                  contentSection = "business",
                                  deviceClass = null,
                                  otherConditions = null,
                                  entitlements = null,
                                  listener = object: ArcxpPageviewListener() {
    override fun onEvaluationResult(response: ArcxpPageviewEvaluationResult) {
        if (show) {
            //show the page 
        } else {
            //do not show the page
        }
    }
})


val page = ArcxpPageviewData(myPageId, conditions)
val conditions = hashMapOf<String, String>(Pair("contentType", "story"), Pair("contentSection", "business"))
ArcXPMobileSDK.commerceManager.evaluatePage(page,
                                  listener = object: ArcxpPageviewListener() {
    override fun onEvaluationResult(response: ArcxpPageviewEvaluationResult) {
        if (show) {
            //show the page 
        } else {
            //do not show the page
        }
    }
})
```

:::note
After the single SDK release, it can be accessed by **ArcXPMobileSDK.commerceManager()**.
:::

## Paywall Conditions

Paywall rule evaluation takes many data points into consideration, but one of the primary types of data it considers is “conditions.” One condition that we’ve provided is `deviceClass`, but given the flexible nature of conditions, any name can be supplied and used. If there is a desire to consider `deviceClass`, a value for it will need to be provided through the Paywall rules. During Paywall rule evaluation, page view data will be considered. Page view data wraps a page ID and conditions.

Conditions are values that are compared against each other during Paywall evaluation. For example, in the Paywall rules, a condition may have been defined with a key of `deviceClass` and a value of `mobile`. And page view data for a specific page ID may have defined a matching condition with the same key and value pair. In that case, those conditions would contribute to a passing evaluation. However, if the same keys exist with differing values, the evaluation will fail. Additionally, no matching key exists, the evaluation will still fail. Here are some examples.

Example 1:

``Paywall rule: `[“deviceClass”: “mobile”]` \``

``Page view data: `“[”deviceClass”: “mobile”]`\``

\*\*PASSES\*\*

Example 2

``Paywall rule: `[“deviceClass”: “mobile”]` \``

``Page view data: `“[”deviceClass”: “web”]` \``

\*\*FAILS\*\*

Example 3:

``Paywall rule: `[“deviceClass”: “mobile”]` \``

``Page view data: `“[”contentType”: “story”]` \``

\*\*FAILS\*\*
