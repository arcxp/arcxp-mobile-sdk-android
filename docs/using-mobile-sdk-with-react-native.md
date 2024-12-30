# React Native 

Using an Expo module with React Native to wrap an Android and iOS library involves creating a unified JavaScript interface that communicates with platform-specific code.

We have tested this with our native Mobile SDK libraries (the content functionality specifically). This document aims to show a brief example of how to accomplish this.

A high level view of the project structure:

```
/MyExpoProject
  /node_modules
  /src
  App.js
  package.json

/MyContentModule
  /android
    ContentModule.kt
  /ios
    ContentModule.swift
  package.json
```

## Creating the Project

As an example, we can start from from the example expo module project by the following guide found in [Wrap third-party native libraries](https://docs.expo.dev/modules/third-party-library/).

This will build out the project folder, we can continue with our custom module from there.

## Creating an Expo Module

Basic Example for Android (Kotlin): Create a Kotlin file (e.g., ContentModule.kt) and implement your module.

```kotlin
package com.yourproject.contentmodule

import expo.modules.kotlin.modules.Module
import expo.modules.kotlin.modules.ModuleDefinition

class ContentModule : Module() {
    override fun definition() = ModuleDefinition {
        Name("ContentModule")

        Function("getContent") { getContent() }
    }

    private fun getContent(): String {
        return "This is content from Android"
    }
}
```

Basic Example iOS (Swift): Create a Swift file to implement your native module.

```swift
import ExpoModulesCore

public class MyModule: Module {
  public func definition() -> ModuleDefinition {
    Name("MyModule")

    Function("setUp") { () -> String in
      "setup complete!"
    }
  }
}
```

Android (ContentModule.kt):

```kotlin
package expo.modules.contentmodule

import android.app.Application
import android.content.Context
import com.arcxp.ArcXPMobileSDK
import com.arcxp.commons.util.Success
import com.arcxp.content.ArcXPContentConfig
import expo.modules.kotlin.Promise
import expo.modules.kotlin.exception.Exceptions
import expo.modules.kotlin.modules.Module
import expo.modules.kotlin.modules.ModuleDefinition
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ContentModule : Module() {
    // Each module class must implement the definition function. The definition consists of components
    // that describes the module's functionality and behavior.
    // See https://docs.expo.dev/modules/module-api for more details about available components.
    val context: Context
        get() = appContext.reactContext ?: throw Exceptions.ReactContextLost()

    override fun definition() = ModuleDefinition {
        // Sets the name of the module that JavaScript code will use to refer to the module. Takes a string as an argument.
        // Can be inferred from module's class name, but it's recommended to set it explicitly for clarity.
        // The module will be accessible from `requireNativeModule('ContentModule')` in JavaScript.
        Name("ContentModule")

        // Defines a JavaScript synchronous function that runs the native code on the JavaScript thread.
        Function("sdkSetup") {
            try {
                val contentConfig = ArcXPContentConfig.Builder()
                    .setNavigationEndpoint(endpoint = "mobile-nav")
                    .setVideoCollectionName(videoCollectionName = "mobile-video")
                    .setCacheSize(sizeInMB = 1024)
                    .setCacheTimeUntilUpdate(minutes = 5)
                    .setPreloading(preLoading = true)
                    .build()

                ArcXPMobileSDK.initialize(
                    application = context.applicationContext as Application,
                    site = "arcsales",
                    org = "arcsales",
                    environment = "sandbox",
                    contentConfig = contentConfig,
                    baseUrl = "https://arcsales-arcsales-sandbox.web.arc-cdn.net"
                )


            } catch (e: Exception) {
                return@Function "Error during initialization"
            }

        }

        // Defines a JavaScript function that always returns a Promise and whose native code
        // is by default dispatched on the different thread than the JavaScript runtime runs on.
        AsyncFunction("fetchArticle") { id: String, promise: Promise ->

            CoroutineScope(Dispatchers.Main).launch {
                try {
                    val result = withContext(Dispatchers.IO) {
                        (ArcXPMobileSDK.contentManager()
                            .getContentAsJsonSuspend(id = id) as Success).success
                    }
                    promise.resolve(result)
                } catch (e: Exception) {

                    promise.reject("Error", e.localizedMessage, e)
                }
            }

        }
        AsyncFunction("fetchCollection") { id: String, promise: Promise ->


            CoroutineScope(Dispatchers.Main).launch {
                try {
                    val result = withContext(Dispatchers.IO) {
                        (ArcXPMobileSDK.contentManager()
                            .getCollectionAsJsonSuspend(collectionAlias = id) as Success).success
                    }
                    promise.resolve(result)
                } catch (e: Exception) {

                    promise.reject("Error", e.localizedMessage, e)
                }
            }

        }
        AsyncFunction("fetchSectionList") { promise: Promise ->
            CoroutineScope(Dispatchers.Main).launch {
                try {
                    val result = withContext(Dispatchers.IO) {
                        (ArcXPMobileSDK.contentManager()
                            .getSectionListAsJsonSuspend() as Success).success
                    }
                    promise.resolve(result)
                } catch (e: Exception) {

                    promise.reject("Error", e.localizedMessage, e)
                }
            }

        }
    }
}
```

iOS (ContentModule.swift):

```swift
import ExpoModulesCore
import ArcXP

public class ContentModule: Module {
    
    public func definition() -> ModuleDefinition {
        Name("ContentModule")
        
        Function("sdkSetup") { () -> String in
            let contentConfig = ArcXPContentConfig(organizationName: "arcsales",
                                                   serverEnvironment: .sandbox,
                                                   site: "arcsales",
                                                   hostDomain: "arcsales-arcsales-sandbox.web.arc-cdn.net",
                                                   thumborResizerKey: "test")
            
            let cacheConfig = ArcXPCacheConfig(timeToConsider: 10)
            ArcXPContentManager.setUp(with: contentConfig, cacheConfig: cacheConfig)
            return "setup complete!"
        }
        
        AsyncFunction("fetchArticle") { (id: String, promise: Promise) in
            ArcXPContentManager.client.getRawJsonContent(requestType: .contentType, identifierOrAlias: id) { result in
                switch result {
                case .success(let jsonString):
                    promise.resolve(jsonString)
                case .failure(let error):
                    promise.reject(error)
                }
            }
        }
        
        AsyncFunction("fetchCollection") { (id: String, promise: Promise) in
            ArcXPContentManager.client.getRawJsonContent(requestType: .collectionType, identifierOrAlias: id) { result in
                switch result {
                case .success(let collection):
                    promise.resolve(collection)
                case .failure(let error):
                    promise.reject(error)
                }
            }
        }
        
        AsyncFunction("fetchSectionList") { (promise: Promise) in
            ArcXPContentManager.client.getRawJsonContent(requestType: .sectionListType, identifierOrAlias: "mobile-nav") { result in
                switch result {
                case .success(let list):
                    promise.resolve(list)
                case .failure(let error):
                    promise.reject(error)
                }
            }
        }
    }
}
```

## AsJson Calls from SDK

To support usage outside of Native projects, we have overloaded the methods of SDK to include AsJson returns. So, rather than a kotlin/swift object you would get the `json` as is from server.

### Making Network calls via the Content Module

Basic example:

### Using the Module in JavaScript

* **Access in JavaScript**:
  * In your JavaScript or TypeScript code, you can now access the ContentModule using Expo's module API.
  * An example of accessing this module in a React component could be:

```js
import { NativeModulesProxy } from 'expo-modules-core';

const { ContentModule } = NativeModulesProxy;

const getContentFromModule = async () => {
  try {
    const content = await ContentModule.getContent();
    console.log(content);
  } catch (error) {
    console.error(error);
  }
};
```

A more specific network call example using expo module in app (will call either iOS/android content module):

```js
import { StyleSheet, Text, ActivityIndicator, FlatList, StatusBar, SafeAreaView, View } from 'react-native';
import React, { useState, useEffect } from 'react';

import * as ContentModule from 'content-module';

const App = () => {
  const setup = ContentModule.sdkSetup();
  const [isLoading, setLoading] = useState(true);
  const [data, setData] = useState([""]);

  const getCollection = async () => {
    try {
      const response = await ContentModule.fetchCollection("mobile-topstories");
      const collection = await JSON.parse(response);
      const articles: string[] = [];
      for (var i = 0; i < 20; i++) {
        articles.push(JSON.stringify(collection[i], undefined, 4));
      }
      setData(articles);
    } catch (error) {
      console.error(error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    getCollection();
  }, []);

  return (
    <SafeAreaView style={styles.container}>
      {isLoading ? (
        <ActivityIndicator />
      ) : (
        <FlatList
          data={data}
          renderItem={({item}) => (
            <Text style={styles.item}>
              {item}
            </Text>
          )}
        />
      )}
    </SafeAreaView>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    paddingTop: StatusBar.currentHeight,
    marginHorizontal: 20,
  },
  item: {
    flexShrink: 1,
    color: 'black',
    fontSize: 18,
    padding: 20.
  },
}); 

export default App;
```

## How to include the Mobile SDK in your React Native project

**Android**:

Similar to a native project, the sdk can either be included by manually inserting an aar into project or by maven download. The instructions can be followed in our SDK setup [here](getting-started-initialization.md) to include your credentials in your app’s settings.gradle for the maven route, or alternatively where and how to insert the binary.

**iOS**:

To include our native SDK into a react project you must edit the React project's `.podspec` file like so:

```js
    ...
    s.dependency 'ExpoModulesCore'
    s.dependency 'ArcXP', '~> 1.1.1', 
    # Change the version number to the most up to date version.
    ...
```

## Differences for Native CLI Module

If you don’t use expo, you can create a similar pattern with native modules.

**Android/Kotlin**

1. **Create the Native Module**:
    1. Open your React Native project in Android Studio.
    2. Right-click on your package under android/app/src/main/java/..., select New -> Java Class or Kotlin Class/File.
    3. Name your class (e.g., ContentModule).

2. **Implement the Native Module**:
    1. Extend ReactContextBaseJavaModule.
    2. Override the getName() method to return the name of the module.
    3. Write the methods you want to expose to JavaScript.

```js
class ContentModule(reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {
    
    override fun getName(): String {
        return "ContentModule"
    }

    @ReactMethod
    fun exampleMethod() {
        // Implementation
    }
}
```

**iOS/Swift**

The same process applies for iOS (this example uses swift).

```swift
@objc(ContentModule)
class ContentModule: NSObject, RCTBridgeModule {

    @objc func getContent(_ callback: RCTResponseSenderBlock) {
        callback(["This is content from iOS"])
    }

    // React Native module name
    @objc static func moduleName() -> String! {
        return "ContentModule"
    }
}
```

## Limitations

* When using an Expo module with React Native to access a native library, the communication between the JavaScript (React Native) layer and the native code (iOS or Android) involves a process often referred to as "the bridge." This bridge is responsible for serializing and deserializing data between the two layers.The process of converting data structures or object states into a format that can be stored or transmitted (serialization) and then reversing this process (deserialization) introduces overhead.
* We have yet to investigate returning actual views from the SDK.
* Video/Commerce/Subscriptions/Identity SDK features remain untested.

## Future Thoughts

* Instead of compiling your own module such as this, we could distribute this as an npm module including both of our sdk binaries ( host on github packages with the same authentication (similar to ANS repo)).
* Alternatively, we could focus on the network calls and create a raw react library and avoid the native code here.

## References / For More Information

Tutorial for creating a native module using Expo Modules can be found in [Tutorial: Creating a native module](https://docs.expo.dev/modules/native-module-tutorial/).

Tutorial for wrapping a native library using Expo Modules can be found in [Wrap third-party native libraries](https://docs.expo.dev/modules/third-party-library/).
