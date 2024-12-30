# Subscriptions Getting Started

This article covers getting started with Arc XP’s Android SDKs for Arc XP Subscriptions. Though the method names and descriptions all reference Commerce, the SDK also works with Arc XP Subscriptions and Arc XP Identity.

This SDK Module offers a convenient wrapper around Arc XP Identity’s public APIs. The SDK supports account management, including creating new accounts, logging in and out, deleting accounts, and profile changes. Unless otherwise noted, all potential error responses are cataloged in the Swagger documentation of the various endpoints.

To initialize your SDK, see [Mobile SDK - Android Initialization](getting-started-initialization.md).

## Using the SDK

```kotlin
ArcXPMobileSDK.commerceManager().validateSession("token", object: ArcIdentityListener() {
    override fun onValidateSessionSuccess() {

    }
    override fun onValidateSessionError(error: ArcError) {

    }
})
```

All SDK functionality is accessed through the `ArcxpCommerceManager` class. Each method must pass in an instance of a callback object in its parameter list. The type of this object depends on the category of method being called. The callbacks are implemented as abstract classes rather than interfaces so the calling method needs to implement the methods that return values based on the method being called.

**Example:**

```kotlin
private lateinit var arcCommerceManager: ArcxpCommerceManager

override fun onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)

    val arcCommerceConfig = ArcxpCommerceConfig.Builder()
            .setContext(activity?.applicationContext!!)
            .setRecaptchaSiteKey(getString(R.string.site_key))
            .setFacebookAppId(getString(R.string.facebook_app_id))
            .setGoogleClientId(getString(R.string.web_client_key))
            .enableRecaptchaForOneTimeAccess(true)
            .enableRecaptchaForSignin(true)
            .enableRecaptchaForSignup(true)
            .setBaseUrl("baseUrl")
            .build()

   ArcXPMobileSDK.initialize(
              ...
            commerceConfig = arcCommerceConfig,
             ....)

    arcCommerceManager = ArcXPMobileSDK.commerceManager()

    loginButton.setOnClickListener {
        login(usernameEditText.text.toString(), passwordEditText.text.toString())
    }

    logoutButton.setOnClickListener {
        logout()
    }
}

fun login(username: String, password: String) {
    arcCommerceManager.login(username, password, object: ArcIdentityListener() {
        override fun onLoginSuccess(response: AuthResponse) {
            startActivity(Intent(this, MainActivity::class.java))
        }

        override fun onLoginError(error:ArcError) {
            showLoginError(error.message)
        }
    })
}

fun logout() {
    arcCommerceManager.logout(object: ArcIdentityListener() {
        override fun onLogoutSuccess() {
            startActivity(Intent(this, LoggedOutActivity::class.java))
        }
        override fun onLogoutError(error: ArcError) {
            showLogoutError(error.message)
        }
    })
}
```

In this example, both the log in and log out implementations use an implementation of ArcIdentityListener for the callback object but implement different methods. This is the case with all SDK methods. The specific methods that need to be implemented for each action are documented in the Identity Methods section.

### reCAPTCHA

You can implement reCAPTCHA in two ways: The first is to have the SDK run reCAPTCHA during registration, login, or getting a one-time access link. This option is turned on in the configuration object. The other is to call the SDK `checkRecaptcha` method and then respond appropriately based on the response.

Use the first way if you want reCAPTCHA to run automatically. Use the second way if you want reCAPTCHA to run based on a user action, such as an “I am not a robot” checkbox.

**Example 1:**

```kotlin
private lateinit var arcCommerceManager: ArcxpCommerceManager

override fun onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)

    val arcCommerceConfig = ArcxpCommerceConfig.Builder()
            .setContext(activity?.applicationContext!!)
            .setRecaptchaSiteKey(getString(R.string.site_key))
            .enableRecaptchaForOneTimeAccess(true)
            .enableRecaptchaForSignin(true)
            .enableRecaptchaForSignup(true)
            .setBaseUrl("base")
            .build()

   ArcXPMobileSDK.initialize(
              ...
            commerceConfig = arcCommerceConfig,
             ....)

    arcCommerceManager = ArcXPMobileSDK.commerceManager()

    loginButton.setOnClickListener {
        //Recaptcha will automatically be run and the token stored in the SDK
        arcCommerceManager.login(username, password, object: ArcIdentityListener() {
            override fun onLoginSuccess(response: AuthResponse) {
                startActivity(Intent(this, MainActivity::class.java))
            }

            override fun onLoginError(error:ArcError) {
                showLoginError(error.message)
            }
        })
    }

}
```

**Example 2:**

```kotlin
private lateinit var arcCommerceManager: ArcxpCommerceManager

override fun onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)

    val arcCommerceConfig = ArcxpCommerceConfig.Builder()
            .setContext(activity?.applicationContext!!)
            .setRecaptchaSiteKey(getString(R.string.site_key))
            .enableRecaptchaForOneTimeAccess(false)
            .enableRecaptchaForSignin(false)
            .enableRecaptchaForSignup(false)
            .setUrlComponents("org", "site", "env")
            .build()

   ArcXPMobileSDK.initialize(
              ...
            commerceConfig = arcCommerceConfig,
             ....)

    arcCommerceManager = ArcXPMobileSDK.commerceManager()

    loginButton.enabled = false

    recaptchaCheckBox.setOnCheckedChangeListener { _, isChecked ->
        if (isChecked) {
            arcCommerceManager.runRecaptcha(object: ArcIdentityListener() {
                override fun onRecaptchaSuccess(token: String) {
                    //make sure to set the recaptcha token manually
                    arcCommerceManager.setRecaptchaToken(token)
                    loginButton.enabled = true
                }

                override fun onRecaptchaFailure(error: ArcError) {
                    loginButton.enabled = false
                }

                override fun onRecaptchaCancel() {

                }
            })
        }
    }

    loginButton.setOnClickListener {
        //Recaptcha will automatically be run and the token stored in the SDK
        arcCommerceManager.login(username, password, object: ArcIdentityListener() {
            override fun onLoginSuccess(response: AuthResponse) {
                startActivity(Intent(this, MainActivity::class.java))
            }

            override fun onLoginError(error:ArcError) {
                showLoginError(error.message)
            }
        })
    }

}
```

### Session Management

The SDK caches the user-session information. This is turned off by default. If it is turned on, encrypted shared preferences are used to store the UUID, access token, and refresh token.

To turn on user session caching, use the method `rememberUser(true)`.

When the login completes successfully, the UUID, access token, and refresh token are stored.

When the logout completes successfully, the information is deleted from shared preferences.

Between login and logout, the session state can be checked using the method validateSession(). If the method returns a call to onValidateSessionSuccess then the session is still active and the user is logged in.

If the session needs to be refreshed then a call can be made to refreshSession().

### Third-Party Login

The SDK is capable of logging in using a token from a third party. It allows the client code to implement logins by Apple, Facebook, and Google.

Facebook Example:

Add the following code to your layout XML file

```xml

    <com.facebook.login.widget.LoginButton
            android:id="@+id/btn_facebook"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_marginTop="10dp" />
```

And the corresponding code

```kotlin
    btn_facebook.setOnClickListener {
    arcCommerceManager.loginWithFacebook(btn_facebook, object: ArcIdentityListener() {
        override fun onLoginSuccess(result: AuthResponse) {

        }
        override fun onLoginError(error: ArcError) {

        }
    })
}
```

The following string must also be defined

```xml

    <string name="facebook_app_id"></string>
```

Google Example:

Add the following code to your layout XML

```xml
<com.google.android.gms.common.SignInButton
            android:id="@+id/btn_google"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_marginTop="10dp" />
```

And the corresponding code

```kotlin
   btn_google.setOnClickListener {
    arcxpCommerceManager.loginWithGoogle(activity!!, object: ArcIdentityListener() {
        override fun onLoginSuccess(response: AuthResponse) {

        }

        override fun onLoginError(error: ArcError) {

        }
    })
}
```

The following XML must also be defined

```xml
<string name="google_key"></string>
```

Apple Login:

Add the following to your layout XML file

```xml
<com.arcxp.subscription.models.applesignin.view.SignInWithAppleButton
        android:id="@+id/btn_apple"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_marginTop="10dp"
        style="@style/SignInWithAppleButton.Black"
        app:sign_in_with_apple_button_textType="signInWithApple"
        app:sign_in_with_apple_button_cornerRadius="4dp" />
```

The following XML must also be defined

```xml
<string name="apple_clientID"></string>
<string name="apple_redirectUri"></string>
<string name="apple_auth_token_url"></string>
<string name="apple_scope">email</string>
```

## Creating An Account

An account can be created using the `signUp()` method. This method creates a new user, identity, profile and returns the UUID, identity, and profile of the user.

The request will look like this:

```json
{
  "identity": {
    "userName": "jdoe123",
    "password": "string"
  },
  "profile": {
    "firstName": "John",
    "lastName": "Doe",
    "secondLastName": "Doey",
    "displayName": "john_doe",
    "gender": "MALE, FEMALE, NON_CONFORMING, PREFER_NOT_TO_SAY",
    "email": "john.doe@donotreply.com",
    "picture": "KEY1",
    "birthYear": "1999",
    "birthMonth": "11",
    "birthDay": "15",
    "legacyId": "AA132CF97",
    "deletionRule": 1,
    "contacts": [
      {
        "phone": "555-555-5555",
        "type": "WORK, HOME, PRIMARY, OTHER"
      }
    ],
    "addresses": [
      {
        "line1": "123 Main St.",
        "line2": "Apt 123",
        "locality": "Springfield",
        "region": "CA",
        "postal": "60010",
        "country": "US",
        "type": "WORK, HOME, PRIMARY, OTHER"
      }
    ],
    "attributes": [
      {
        "name": "KEY1",
        "value": "VALUE 1",
        "type": "String, Number, Date, Boolean"
      }
    ]
  },
  "recaptchaToken": "string"
}
```

and the response will look like this:

```json
{
  "createdOn": "2021-04-21T15:50:46.560Z",
  "createdBy": "string",
  "modifiedOn": "2021-04-21T15:50:46.560Z",
  "modifiedBy": "string",
  "uuid": "001ef3c3-35c3-45a9-a4bf-1b2df0e31514",
  "userState": true,
  "identities": [
    {
      "createdOn": "2021-04-21T15:50:46.560Z",
      "createdBy": "string",
      "modifiedOn": "2021-04-21T15:50:46.560Z",
      "modifiedBy": "string",
      "id": 1,
      "userName": "jdoe123",
      "passwordReset": true,
      "type": "Identity",
      "lastLoginDate": "2021-04-21T15:50:46.560Z",
      "locked": true
    }
  ],
  "profile": {
    "createdOn": "2021-04-21T15:50:46.560Z",
    "createdBy": "string",
    "modifiedOn": "2021-04-21T15:50:46.560Z",
    "modifiedBy": "string",
    "firstName": "John",
    "lastName": "Doe",
    "secondLastName": "Doey",
    "displayName": "john_doe",
    "gender": "MALE, FEMALE",
    "email": "john.doe@donotreply.com",
    "unverifiedEmail": "john.doe2@donotreply.com",
    "emailVerified": true,
    "picture": "KEY1",
    "birthYear": "1999",
    "birthMonth": "11",
    "birthDay": "15",
    "legacyId": "FGE234UIR184",
    "contacts": [
      {
        "phone": "555-555-5555",
        "type": "WORK, HOME, PRIMARY, OTHER"
      }
    ],
    "addresses": [
      {
        "line1": "123 Main St.",
        "line2": "string",
        "locality": "string",
        "region": "string",
        "postal": "string",
        "country": "string",
        "type": "WORK, HOME, PRIMARY, OTHER"
      }
    ],
    "attributes": [
      {
        "name": "KEY1",
        "value": "VALUE 1",
        "type": "String, Number, Date, Boolean"
      }
    ],
    "identities": [
      {
        "createdOn": "2021-04-21T15:50:46.560Z",
        "createdBy": "string",
        "modifiedOn": "2021-04-21T15:50:46.560Z",
        "modifiedBy": "string",
        "id": 1,
        "userName": "jdoe123",
        "passwordReset": true,
        "type": "Identity",
        "lastLoginDate": "2021-04-21T15:50:46.560Z",
        "locked": true
      }
    ],
    "status": "Active/Disabled",
    "deletionRule": 1,
    "profileNotificationEventResponse": {
      "createdOn": "2021-04-21T15:50:46.560Z",
      "createdBy": "string",
      "modifiedOn": "2021-04-21T15:50:46.560Z",
      "modifiedBy": "string",
      "id": 1,
      "rule": {
        "createdOn": "2021-04-21T15:50:46.560Z",
        "createdBy": "string",
        "modifiedOn": "2021-04-21T15:50:46.560Z",
        "modifiedBy": "string",
        "id": 1,
        "name": "Rule-1",
        "notificationTriggerDays": 180,
        "actionTriggerDays": 60,
        "notificationRecurrenceDays": 10,
        "notificationLimit": 180,
        "typeId": "EMAIL_NEVER_VERIFIED"
      },
      "uuid": "abcd123",
      "status": "Scheduled",
      "notificationDate": "06/01/2020",
      "actionDate": "08/01/2020",
      "notificationSentCount": 10
    }
  }
}
```

## Error Reporting

When a method fails or an error occurs the resulting information is returned in an `ArcError` object. This object has the following parameters:

| Parameter | Value | Description |
| --- | ---- | --- |
| type | One of: <br> `INIT_ERROR` <br> `CONFIG_ERROR` <br> `SERVER_ERROR` <br> `INVALID_SESSION` <br> `RECAPTCHA_ERROR` <br> `LOGIN_ERROR` <br> `ONE_TIME_ACCESS_LINK_ERROR` <br> `REGISTRATION_ERROR` <br> `APPLE_CONFIG_ERROR` <br> `APPLE_LOGIN_ERROR` <br> `APPLE_LOGIN_CANCEL` <br> `FACEBOOK_LOGIN_ERROR` <br> `FACEBOOK_LOGIN_CANCEL` <br> `GOOGLE_LOGIN_ERROR`  <br> `GOOGLE_LOGIN_CANCEL` <br> | Describe the general category of the error. |
| code | See API documentation for error codes. | Contains the error code returned by the Arc XP server. This will be null if the error is not caused by a problem with the call to the Arc XP server. |
| message | String | A description of the error. If it a server error it will be one of the descriptions associated with the code listed above. |
| value | Any | An object associated with the error |

### Identity Methods

**login(email, password, listener)** - Login using username and password.

| Parameter | Type | Description |
| --- | --- | --- |
| `email` | String | Email address of the user |
| `password` | String | Password of user |
| `listener` | ArcIdentityListener | Implement onLoginSuccess and onLoginError |

Usage:

```kotlinarcCommerceManager.login(”email”, “password” object: ArcIdentityListener() {
    override fun onLoginSuccess(response: AuthResponse) {

    }

    override fun onLoginError(error:ArcError) {

    }
})
```

**thirdPartyLogin(token, grantType, listener)** \- Login using a third-party token such as Apple, Google, or Facebook.

| Parameter | Type | Description |
| --- | --- | --- |
| `token` | String | The token provided by the third party login service. |
| `grantType` | GrantType | Specifies the type of login. One of GOOGLE, FACEBOOK, APPLE |
| `listener` | ArcIdentityListener | Implement onLoginSuccess and onLoginError |

Usage:

```kotlin
arcCommerceManager.thirdPartyLogin("token", GrantType.FACEBOOK, object: ArcIdentityListener() {
    override fun onLoginSuccess(response: AuthResponse) {

    }
    override fun onLoginError(error: ArcError) {

    }
})
```

 **updatePassword(newPassword, oldPassword, listener)** - Change the password for the currently logged-in user.

| Parameter | Type | Description |
| --- | --- | --- |
| `newPassword` | String | New password for user |
| `oldPassword` | String | Previous password for user |
| `listener` | ArcIdentityListener | Implement onPasswordChangeSuccess and onPasswordChangeError |

Usage:

```kotlin
arcCommerceManager.updatePassword(”newpw”, “oldpw”, object: ArcIdentityListener() {
    override fun onPasswordChangeSuccess(response: IdentityResponse) {

    }

    override fun onPasswordChangeError(error: ArcException) {

    }
})
```

**requestResetPassword(username, listener)** - Request that a reset password email is sent to the user

| Parameter | Type | Description |
| --- | --- | --- |
| `username` | String | Username of user |
| `listener` | ArcIdentityListener | Implement onPasswordResetSuccess and onPasswordResetError |

Usage:

```kotlin
arcCommerceManager.requestResetPassword("username", object: ArcIdentityListener() {
    override fun onPasswordResetSuccess(response: RequestPasswordResetResponse) {

    }

    override fun onPasswordResetError(error: ArcError) {

    }
})
```

**resetPassword(nonce, newPassword, listener)** - Reset Password with nonce

| Parameter | Type | Description |
| --- | --- | --- |
| `nonce` | String | Nonce value provided in email |
| `newPassword` | String | New password for user |
| `listener` | ArcIdentityListener | Implement onPasswordResetSuccess and onPasswordResetError |

Usage:

```kotlin
arcCommerceManager.resetPassword("nonce", "newpw" object: ArcIdentityListener() {
    override fun onPasswordResetSuccess(response: RequestPasswordResetResponse) {

    }

    override fun onPasswordResetError(error: ArcError) {

    }
})
```

 **requestOneTimeAccessLink(email, listener)** - Request a one time access link sent to the users email address.

| Parameter | Type | Description |
| --- | --- | --- |
| `email` | String | Email address to send link |
| `listener` | ArcIdentityListener | Implement onOneTimeAccessLinkSuccess and onOneTimeAccessLinkError |

Usage:

```kotlin
arcCommerceManager.requestOneTimeAccessLink("email" object: ArcIdentityListener() {
    override fun onOneTimeAccessLinkSuccess(response: MagicLinkResponse) {

    }

    override fun onOneTimeAccessLinkError(error: ArcError) {

    }
})
```

 **sendVerificationEmail(email, listener)** - Send an email to the given email address asking the user to verify the account. This is sent after a new account is created.

| Parameter | Type | Description |
| --- | --- | --- |
| `email` | String | Email address to send the verification email to |
| `listener` | ArcIdentityListener | Implement `onEmailVerificationSentSuccess` and `onEmailVerificationSentError` |

Usage:

```kotlin
arcCommerceManager.sendVerificationEmail("email", object: ArcIdentityListener() {
    override fun onEmailVerificationSentSuccess(response: EmailVerificationResponse) {

    }
    override fun onEmailVerificationSentError(error: ArcError) {

    }
})
```

 **redeemOneTimeAccessLink(nonce, listener)** \- Login using the nonce value provided by the requestOneTimeAccessLink() method.

| Parameters | Type |   |
| --- | --- | --- |
| `nonce` | String | Nonce value provided in email |
| `listener` | ArcIdentityListener | Implement `onOneTimeAccessLinkLoginSuccess` and `onOneTimeAccessLinkError` |

Usage:

```kotlin
arcCommerceManager.redeemOneTimeAccessLink("nonce", object: ArcIdentityListener() {
    override fun onOneTimeAccessLinkLoginSuccess(response: MagicLinkAuthResponse) {

    }
    override fun onOneTimeAccessLinkError(error: ArcError) {

    }
})
```

 **updateProfile(request, listener)** - Update the logged-in users profile data.

| Parameter | Type | Description |
| --- | --- | --- |
| `request` | UpdateProfileRequest | UpdateProfileRequestObject (see [Table 1: Class Defintions](#table-1-class-definitions) |
| `listener` | ArcIdentityListener | Implement `onProfileUpdateSuccess` and `onProfileError` |

Usage:

```kotlin
val updateProfileRequest = UpdateProfileRequest(firstName = "firstname", lastName = "lastname", email = "email")

arcCommerceManager.updateProfile(updateProfileRequest, object: ArcIdentityListener() {
    override fun onProfileUpdateSuccess(response: ProfileManagementResponse) {

    }

    override fun onProfileError(error: ArcError) {

    }
})
```

**getUserProfile(listener)** - Fetch the profile of the logged-in user.

| Parameter | Type | Description |
| --- | --- | --- |
| `listener` | ArcIdentityListener | Implement `onFetchProfileSuccess` and `onProfileError` |

Usage:

```kotlin
arcCommerceManager.getUserProfile(object: ArcIdentityListener() {
    override fun onFetchProfileSuccess(response: ProfileManagementResponse) {

    }

    override fun onProfileError(error: ArcError) {

    }
})
```

**signUp(username, password, email, firstname, lastname, listener)** \- Register a new user.

| Parameter | Type | Description |
| --- | --- | --- |
| `username` | String | Username of user |
| `password` | String | Password of user |
| `email` | String | Email of user |
| `firstname` | String | First name of user |
| `lastname` | String | Last name of the user |
| `listener` | ArcIdentityListener | Implement `onRegistrationSuccess` and `onRegistrationError` |

Usage:

```kotlin
arcCommerceManager.signUp("username", "password", "email", "firstname", "lastname",
    object: ArcIdentityListener() {
        override fun onRegistrationSuccess(response: UserResponse) {

        }

        override fun onRegistrationError(error: ArcError) {

        }
    })
```

**logout(listener)** - Logout a logged-inthe user.

| Parameter | Type | Description |
| --- | --- | --- |
| `listener` | ArcIdentityListener | Implement `onLogoutSuccess` and `onLogoutError` |

Usage:

```kotlin
arcCommerceManager.logout(object: ArcIdentityListener() {
    override fun onLogoutSuccess() {

    }

    override fun onLogoutError(error: ArcError) {

    }
})
```

 **requestDeleteAccount(listener)** - Request deletion of user account.

| Parameter | Type | Description |
| --- | --- | --- |
| `listener` | ArcIdentityListener | Implement `onDeleteUserSuccess` and `onDeleteUserError` |

Usage:

```kotlin
arcCommerceManager.requestDeleteAccount(object: ArcIdentityListener() {
    override fun onDeleteUserSuccess() {

    }
    override fun onDeleteUserError(error: ArcError) {

    }
})
```

**approveDeleteAccount(nonce, listener)** - Approve the deletion of user account.

| Parameter | Type | Description |
| --- | --- | --- |
| nonce | String | Nonce value provided in email |
| listener | ArcIdentityListener | Implement `onApproveDeletionSuccess` and `onApproveDeletionError` |

Usage:

```kotlin
arcCommerceManager.approveDeleteAccount("nonce", object: ArcIdentityListener() {
    override fun onApproveDeletionSuccess() {

    }
    override fun onApproveDeletionError(error: ArcError) {

    }
})
```

**validateSession(token, listener)** - Check if the given access token is still valid.

| Parameter | Type | Description |
| --- | --- | --- |
| `token` | String | Access token of the session to validate |
| `listener` | ArcIdentityListener | Implement `onValidateSessionSuccess` and `onValidateSessionError` |

Usage: 

**validateSession(listener)** - Check that the current session token stored by the SDK is still valid.

| Parameter | Type | Description |
| --- | --- | --- |
| `listener` | ArcIdentityListener | Implement `onValidateSessionSuccess` and `onValidateSessionError` |

Usage:

```kotlin
arcCommerceManager.validateSession(object: ArcIdentityListener() {
    override fun onValidateSessionErrorSuccess() {

    }
    override fun onValidateSessionError(error: ArcError) {

    }
})
```

 **refreshSession(token, listener)** - Extend the current session using the passed in access token value.

| Parameter | Type | Description |
| --- | --- | --- |
| `token` | String | Access token of session to refresh |
| `listener` | ArcIdentityListener | Implement `onRefreshSessionSuccess` and `onRefreshSessionFailure` |

Usage:

```kotlin
arcCommerceManager.refreshSession("token", object: ArcIdentityListener() {
    override fun onRefreshSessionSuccess(response: AuthResponse) {

    }
    override fun onRefreshSessionFailure(error: ArcError) {

    }
})
```

**refreshSession(listener)** - Extend the current session using the token stored in the SDK.

| Parameter | Type | Description |
| --- | --- | --- |
| `listener` | ArcIdentityListener | Implement `onRefreshSessionSuccess` and `onRefreshSessionFailure` |

Usage:

```kotlin
arcCommerceManager.refreshSession(object: ArcIdentityListener() {
    override fun onRefreshSessionSuccess(response: AuthResponse) {

    }
    override fun onRefreshSessionFailure(error: ArcError) {

    }
})
```

**runRecaptcha(listener)** - Run the reCAPTCHA process based on the configuration in the site key.

| Parameter | Type | Description |
| --- | --- | --- |
| `listener` | ArcIdentityListener | Implement `onRecaptchaSuccess`, `onRecaptchaFailure`, and `onRecaptchaCancel` |

Usage:

```kotlin
arcCommerceManager.runRecaptcha(object: ArcIdentityListener() {
    override fun onRecaptchaSuccess(token: String) {
        arcCommerceManager.setRecaptchaToken(token)
    }

    override fun onRecaptchaFailure(error: ArcError) {

    }

    override fun onRecaptchaCancel() {

    }
})
```

`getRefreshToken()` - Returns the refresh token of the logged-in user.

`getAccessToken()` - Returns the access token of the logged-in user.

`getRecaptchaToken()` - Returns the reCAPTCHA token. If the ArcCommerceConfig reCAPTCHA settings are turned on then this value will automatically be set. If the client code runs reCAPTCHA manually then the token will need to be saved using `saveRecaptchaToken()`.

`setRecaptchaToken(token)` - Stores the reCAPTCHA token in the SDK. This is used if reCAPTCHA is run by the client code but the client code wants to avoid having to pass the token into the SDK for later calls.

`rememberUser(remember)` - True causes the SDK to cache the session information while the session is active, false will cause this information to not be stored.

### Sales Methods

**getAllActiveSubscriptions(listener)** - Get all active subscriptions for the logged-in user.

| Parameter | Type | Description |
| --- | --- | --- |
| `listener` | ArcSalesListener | Implement `onGetAllActiveSubscriptionsSuccess` and `onGetSubscriptionsFailure` |

Usage:

```kotlin
arcCommerceManager.getAllActiveSubscriptions(object: ArcSalesListener() {
    override fun onGetAllActiveSubscriptionsSuccess(response: SubscriptionsResponse) {

    }

    override fun onGetSubscriptionsFailure(error: ArcError) {

    }
})
```

**getEntitlements(listener)** - Return the entitlements associated with the logged-in user.

| Parameter | Type | Description |
| --- | --- | --- |
| `listener` | ArcxpSalesListener | Implement `onGetEntitlementsSuccess()` and `onGetEntitlementsFailure()` |

Usage:

```kotlin
arcCommerceManager.getEntitlements(object: ArcSalesListener() {
    override fun onGetEntitlementsSuccess(response: EntitlementsResponse) {

    }

    override fun onGetEntitlementsFailure(error: ArcError) {

    }
})

data class EntitlementsResponse(val skus: List<Sku>, val edgescape: Edgescape?)
data class Sku(val sku: String)
data class Edgescape(val city: String?, val continent: String?, val georegion: String?, val dma: String?, val country_code: String?)
```

**getAllSubscriptions(listener)** - Return all subscriptions for the logged-in user.

| Parameter | Type | Description |
| --- | --- | --- |
| `listener` | ArcxpSalesListener | Implement `onGetAllSubscriptionsSuccess` and `onGetSubscriptionsFailure` |

Usage:

```kotlin
arcCommerceManager.getAllSubscriptions(object: ArcSalesListener() {
    override fun onGetAllSubscriptionsSuccess(response: SubscriptionsResponse) {

    }

    override fun onGetSubscriptionsFailure(error: ArcError) {

    }
})
```

### Paywall Methods

Evaluate a page with the paywall algorithm to determine if the page can be shown.

**evaluatePage(pageId, contentType, contentSection, deviceClass, otherConditions, entitlements, listener)**

Use this method to explicitly specify the individual components used to evaluate a page. These values will be wrapped into an ArcxpPageviewData object.

**evaluatePage(pageviewData, listener)**

This method is best used when an `ArcxpPageviewData` object has been received from another source and does not need to be built at evaluation time.

| Parameter | Type | Description |
| --- | --- | --- |
| `pageId` | String | Unique ID of the page |
| `contentType` | String? | Content Type condition setting. Optional |
| `contentSection` | String? | Content Section condition setting. Optional. |
| `deviceClass` | String? | Device Class condition setting. Optional |
| `otherCondition`s | HashMap<String, String> | Any other conditions that need to be included in the evaluation. |
| `entitlements` | EntitlementResponse? | Optional entitlements that can be passed in. If this is omitted or set to null the information will be loaded from the server. |
| `listener` | ArcxpPageviewListener | Implement `onGetActivePaywallRulesSuccess(ArcxpActivePaywallRulesResponse)` and `onGetActivePaywallRulesFailure(ArcxpError)` |
| `pageviewData` | ArcxpPageviewData | Object containing the evaluation data for the page. |

### Retail Methods

**getActivePaywallRules(listener)** - Return all paywall rules.

| Parameter | Type | Description |
| --- | --- | --- |
| `listener` | ArcxpRetailListener | Implement `onGetActivePaywallRulesSuccess()` and `onGetActivePaywallRulesFailure()` |

Usage:

```kotlin

    arcxpCommerceManager.getActivePaywallRules(object: ArcxpRetailListener() {
    override fun onGetActivePaywallRulesSuccess(response: ArcxpActivePaywallRulesResponse) {

    }

    override fun onGetActivePaywallRulesFailure(error: ArcxpError) {

    }
})
```

**getPaywallCache()** - Returns the contents shared preferences that contain the paywall rule stored data.

**clearPaywallCache()** - Clears the paywall shared preferences.

#### Table 1: Class Definitions

|   |   |
| --- | --- |
| ArcIdentityListener | onLoginSuccess(response)<br> onLoginError(ArcError) <br> onEmailVerifiedSuccess(response) <br> onEmailVerifiedError(ArcError) <br> onPasswordChangeSuccess(response) <br> onPasswordChangeError(ArcError) <br> onPasswordResetSuccess(response) <br> onPasswordResetError(ArcError) <br> onMagicLinkSuccess(response) <br> onMagicLinkLoginSuccess(response) <br> onMagicLinkError(ArcError)  <br> onProfileUpdateSuccess(response)  <br> onFetchProfileSuccess(response)  <br> onProfileError(ArcError) <br> onRegistrationSuccess(response)  <br> onRegistrationError(ArcError)  <br> onLogoutSuccess()  <br> onLogoutError(ArcError)  <br> onDeletionSuccess() <br> onDeletionError(ArcError)  <br> onValidateSessionSuccess()  <br> onValidateSessionError(ArcError) <br> onRefreshSessionSuccess(response)  <br> onRefreshSessionFailure(ArcError) <br> onIsLoggedIn(boolean) <br> onRecaptchaSuccess(token) <br> onRecaptchaCancel() <br> onRecaptchaFailure() |
| ArcSalesListener | OnGetSubscriptionsSuccess(response)  <br> onGetAllSubscriptionsSuccess(response)  <br> onError(ArcError) |
| IdentityResponse | createdOn: String <br> createdBy: String  <br> modifiedOn: String <br> modifiedBy: String <br> id: Integer <br> userName: String <br> passwordReset: Boolean <br> type: String <br> lastLoginDate: String <br> locked: Boolean |
| AuthResponse | uuid: String  <br> accessToken: String <br> refreshToken: String |
| RequestPasswordResetResponse | success: Boolean |
| OneTimeAccessLinkResponse | success: Boolean  <br> nonce: String |
| OneTimeAccessLinkAuthResponse | uuid: String  <br> accessToken: String |
| ArcError | type: ArcCommerceSDKErrorType <br> code: String <br> message: String <br> value: Object |
| UpdateProfileRequest | firstName: String <br> lastName: String <br> secondLastName: String <br> displayName: String <br> gender: String (MALE, FEMALE, NON\_CONFORMING, PREFER\_NOT\_TO\_SAY)  <br> email: String <br> picture: String <br> birthYear: String <br> birthMonth: String <br> birthDay: String <br> legacyId: String <br>contacts: List<ContactRequest> <br> addresses: List<AddressRequest> <br> attributes: List<AttributeRequest> |
| ContactRequest | phone: String <br> type: String (WORK, HOME, PRIMARY, OTHER) |
| AddressRequest | line1: String <br> line2: String <br> locality: String <br> region: String <br> postal: String <br> country: String <br> type: String (WORK, HOME, PRIMARY, OTHER) |
| AttributeRequest | name: String <br> value: String <br> type: String (String, Number, Boolean, Date) |
| SubscriptionResponse | List<SubscriptionSummary> |
| SubscriptionSummary | paymentMethod: PaymentMethod <br> productName: String <br> sku: String <br> statusID: SubscriptionStatus (ACTIVE, TERMINATED, CANCELED, SUSPENDED, GIFTED) <br> attributes: List<SubscriptionAttribute> <br> currentRetailCycleIDX: Int |
| PaymentMethod | cardHolderName: String <br> creditCardType: String <br> expiration: String <br> firstSix: String <br> lastFour: String <br> paymentMethodID: Number <br> paymentPartner: String |
| SubscriptionAttribute | key: String <br> value: String |
| EmailVerificationResponse | success: Boolean |
| ArcxpPageviewData | pageId: String <br> conditions: HashMap<String, String> |
| ArcxpRetailListener | onGetActivePaywallRulesSuccess(response: ArcxpActivePaywallRulesResponse) <br> onGetActivePaywallRulesFailure(error: ArcxpError) |
| ArcxpPageviewListener | onInitializationResult(success: Boolean) <br> onEvaluationResult(response: ArcxpPageviewEvaluationResult) |
| ArcxpPageviewEvaluationResult | pageId: String <br> show: Boolean <br> campaign: String |
| ArcxpActivePaywallRulesResponse | response: List<ActivePaywallRule> |
| ActivePaywallRule | id: Int, <br> conditions: HashMap<String, RuleCondition>?, <br> e: List<Object>, <br> cc: String?, <br> cl: String?, <br> rt: Int, <br> budget: RuleBudget |
| RuleCondition | inOrOut: Boolean, <br> values: List<String> |
| RuleBudget | budgetType: String, <br> calendarType: String, <br> calendarWeekDay: String, <br> rollingType: String, <br> rollingDays: Int, <br> rollingHours: Int |

## Paywall

The Arc XP Commerce Module includes a Paywall evaluator that allows you to easily manage how content your readers can consume before they need to register or subscribe. The paywall is accessed through an evaluation function that can be called to determine if a page should be shown given the page parameters and the currently active paywall rules. It isn’t a requirement that paywall evaluation be called on a page.

The paywall evaluator considers all available facts, such as the article consumption history and properties of the current story, to determine if any of the rules are currently exhausted. The paywall script also updates the content consumption history and saves it to shared preferences. If any of the rules are exhausted, the evaluator will return the first exhausted rule and other information. It’s up you to design and build what happens next — Arc XP Commerce paywall simply returns a summation based on the facts at hand and you have complete control over the rest of the user experience.

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

**pageId** - This is a unique page identifier string. Once a page has passed a paywall rule it can be viewed again even if the page counter is above the budget specified in the rule. Therefore each page id must be unique otherwise if a page with the same ID has passed a rule the passed in page will pass the same rule.

**contentType** - This is the Content Criteria condition specification. If it is null then this criteria will not be part of this page and the rules specifying this will note be flagged. It must be a string that matches the values specified in the Content Criteria section of the rule builder. Only a single entry can be specified in this field. Do not concatenate multiple values into a single string.

**contentSection** - This is the Content Criteria condition specification. If it is null then this criteria will not be part of this page and the rules specifying this will note be flagged. It must be a string that matches the values specified in the Content Criteria section of the rule builder. Only a single entry can be specified in this field. Do not concatenate multiple values into a single string.

**deviceClass** - This is the Audience Criteria condition specification. If it is null then this criteria will not be part of this page and the rules specifying this will note be flagged. It must be a string that matches the values specified in the Audience Criteria section of the rule builder. Only a single entry can be specified in this field. Do not concatenate multiple values into a single string.

**otherConditions** - This field allows for the entry of any other conditions other than contentType, contentSection or deviceClass. For each entry in the hashmap, the key/value is the name of the condition and the value for the specified condition.

**entitlements** - This is the list of entitlements for the logged-in user. This parameter is optional. If it is null or omitted then the entitlements will be loaded from using the server during processing of the paywall algorithm. A value can be passed in as an EntitlementResponse object which has the following format:

```kotlin
data class EntitlementsResponse(val skus: List<Sku>, val edgescape: Edgescape?)
data class Sku(val sku: String)
data class Edgescape(val city: String?, val continent: String?, val georegion: String?, val dma: String?, val country_code: String?)
```

The only value that needs to be populated in the EntitlementsResponse object is the `skus` field. All others will be ignored. It is recommended that this field be left as null. The only reason to pass in entitlements information is if the app wants to do its own entitlement management rather than having it managed by the SDK or if the app wants to grant the user an entitlement that would not be returned by a call to the Entitlements API.

**currentTime** - This optional field is provided so that the client app can pass in a time other than the current system time. This is an unlikely scenario and would most likely only occur if the app was implementing some sort of test scenario. If this value is not passed in or set to null then the current system time will be used.

**pageviewData** - This is an ArcxpPageviewData object that encapsulates the page ID and condition data for a page. The format for this object is:

```kotlin
    data class ArcxpPageviewData(val pageId: String, val conditions: HashMap<String, String>)
```

The condition object will be a hashmap with each entry key/value being the name of the condition and the value for the specified condition the same as is done in the otherConditions parameter.

**listener** - This is an ArcxpPageviewDataListener object used to return results of the evaluation method. The format of this object is:

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

**pageId** - The ID of the page being evaluated.

**show** - A true returned means that the page can be shown to the user. A false indicates that one or more paywall rules has determined that the page should not be shown.

**campaign** - The campaign code of the paywall rule that triggered the false value for show. If the show value is true then the campaign code will be null. If more than 1 paywall rule was triggered then the campaign code of the first rule will be returned.

Example:

```kotlin
arcxpCommerceManager.evaluatePage(pageId = myPageId,
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
arcxpCommerceManager.evaluatePage(page,
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

### Paywall Conditions

Paywall rule evaluation takes many data points into consideration, but one of the primary types of data it considers is “conditions.” One condition that we’ve provided is `deviceClass`, but given the flexible nature of conditions, any name can be supplied and used. If there is a desire to consider `deviceClass`, a value for it will need to be provided through the Paywall rules. During Paywall rule evaluation, page view data will be considered. Page view data wraps a page ID and conditions.

Conditions are values that are compared against each other during Paywall evaluation. For example, in the Paywall rules, a condition may have been defined with a key of `deviceClass` and a value of `mobile`. And page view data for a specific page ID may have defined a matching condition with the same key and value pair. In that case, those conditions would contribute to a passing evaluation. However, if the same keys exist with differing values, the evaluation will fail. Additionally, no matching key exists, the evaluation will still fail. Here are some examples.

Example 1:

Paywall rule: `[“deviceClass”: “mobile”]`

Page view data: `[”deviceClass”: “mobile”]`

\*\*PASSES\*\*

Example 2:

Paywall rule: `[“deviceClass”: “mobile”]`

Page view data: `[”deviceClass”: “web”]`

\*\*FAILS\*\*

Example 3:

Paywall rule: `[“deviceClass”: “mobile”\]`

Page view data: `[”contentType”: “story”\]`

\*\*FAILS\*\*
