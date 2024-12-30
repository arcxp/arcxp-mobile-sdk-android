# Security Best Practices 

Please keep the following recommendations in mind while using Arc XP services to make sure your applications operate securely.

## Keys

There are a few keys that identify your application to Arc XP's services, as well as other services that may be integrated with ours. Below is a list of keys that you should make sure are accessed securely, and not made available through your git tracking. If they must exist in files, you can add the filename to `.gitignore` to prevent check in through version control. They should instead be injected during your build process.

- Thumbor resizer key>
- Facebook app ID>
- Facebook client token>
- Google key>
- Admob IDs>

## Social Login Tokens

When setting up your social login services to share with Arc XP Identity, it's unnecessary to store the token that is returned from a successful social login. Simply call Identity's social login hook, and allow the token to be dropped afterwards. If you feel it's necessary to store the social login token, for example in case the network call for the social login fails and you'd like to try again later, make sure to store it securely, and remove it when it's no longer needed. However, it should be kept in mind that if the call fails, the user can simply attempt to login again, preventing the need to store and reuse the token.

### Advertising IDs

Admob advertising IDs including the Application ID and all individual ad type IDs are specific to the client account and can be exploited if compromised.

## Arc XP Commerce Identity User tokens

When logging in to Arc XP's Identity services, our mobile SDK stores access tokens securely with Apple's provided Keychain service. This uses encryption procedures managed by Apple's operating systems. For Android, they are stored in encrypted shared preferences. These stored tokens should not be accessible outside of the Arc XP mobile SDK, but it is being noted here for client awareness. Of course, you should definitely avoid caching any Personal Identifiable Information as a general rule.

## Additional Android Suggestions

Always target latest android apis in your apps, the latest android apis contain the latest security bug fixes and patches.
