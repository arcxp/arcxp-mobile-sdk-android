# Admob Implementation

To make the Arc XP News sample app as robust as possible, we include Google AdMob, an implementation of in-app advertising. Google AdMob provides a robust set of capabilities, including multiple ad types, easy implementation, and integration with a large number of third-party vendors. By providing this implementation, the intent is to allow immediate access to a monetization platform.

Google AdMob provides six types of ads, though the Arc XP News app implements only two: native and banner. Native and banner are the most common for news apps. If you need other ad types, you must provide the implementation.

Native ads appear in the section fronts at periodic intervals that you can customize. Banner ads appear at the top and bottom of each article. You can turn the ads on and off through a Boolean setting in the XML files. Also, the app does not show ads for user who log in through the Commerce Module of the SDK.

You are responsible for creating an AdMob account and managing the ads through that account.

## Creating an AdMob account

An AdMob account is required to use the implementation within The Arc XP News app. You can create an AdMob account at [Google's AdMob site](https://admob.google.com/home/get-started/).

After you create an account, you must verify the account in order to activate it.

You must register each app in the account. Android and iOS versions of an app are considered separate apps. After each app is registered, the app has an app ID. The ID value is necessary to integrate with the app.

### Integrating with the Arc XP sample app

Now that you created an account and you have an app ID, you can integrate the account into the baseline code.

Note:
For testing and development, you must use test IDs for the native and banner ads. You must still use the account app ID as there is no test ID at the app level. This applies only to the banner and native ads. You can find the test ID values at Google's [Enabling test ads](https://developers.google.com/admob/android/test-ads) documentation. These values cannot be checked into a version control repository for security reasons.

### Android platform

Put the three ID values into the `local.properties` file in the following format:

```
admob_app_id=<app id>
admob_banner_id=<banner id>
admob_native_id=<native id>
```


## Enabling ads in the app

### Android platform

In the `ads.xml` file toggle, the Boolean value for `show_ads` to `true`.

Set the `section_ad_frequency` value.


## GDPR compliance

The Arc XP sample app has code to implement GDPR and CCPA compliance. Create and download the consent form according to the instructions at Google's [Create GDPR messages for app](https://support.google.com/admob/answer/10113207?hl=en) documentation.

## Handling GDPR


Put the consent form into the `app/src/main/assets` folder.


## Disable AdMob

Android:

There is a commented section in the manifest you can uncomment to disable AdMob. There are supporting comments to signify what to do.  
You can now run the sample app without setting up AdMob credentials.
