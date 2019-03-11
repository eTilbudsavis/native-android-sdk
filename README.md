ShopGun Android SDK
===================

The simple solution for querying ShopGun data.

## Getting Started

### Download with Gradle
If you haven't already added `jCenter` to your `build.gradle`, you'll need this:
```groovy
repositories {
    jcenter()
}
```

Now add these lines to your module's `build.gradle`:

```groovy
dependencies {
    implementation 'com.shopgun.android:sdk:5.0.0-rc1'
}
```

or clone from github, and add this to your project's `settings.gradle`:

```groovy
include ':shopGunSdk'
project(':shopGunSdk').projectDir=new File('/path/to/shopgun-android-sdk/shopGunSdk')
```

From version `5.0.0`, the SDK requires Java 8 enabled, so in your app `build.gradle`:
```groovy
android {
    compileOptions {
        sourceCompatibility JavaVersion.VERSION_1_8
        targetCompatibility JavaVersion.VERSION_1_8
    }
} 
```    

### API key and secret
You will need to get an *API key* and *API secret* from our 
[ShopGun Developer site] (look for "My Apps")

If you wish to try our demo app, just clone and run it. We've included an API 
key and secret, that will work straight out of the box. But the key only provides 
a limited amount of queries per day so don't use it in production.

### Setup AndroidManifest.xml

We need certain permissions, to make the whole thing run:
```xml
<!-- Obviously we'll need internet -->
<uses-permission android:name="android.permission.INTERNET"/>
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"/>
```

Now add the ApiKey and ApiSecret, you obtained in 
[API key and secret](#api-key-secret) section.

```xml
<application android:label="@string/app_name" ...>
    ...
    <meta-data android:name="com.shopgun.android.sdk.api_key"
        android:value="your_shopgun_sdk_api_key"/>
    <meta-data android:name="com.shopgun.android.sdk.api_secret"
        android:value="your_shopgun_sdk_api_secret"/>
    <meta-data android:name="com.shopgun.android.sdk.develop.api_key"
        android:value="your_shopgun_sdk_api_key_debug"/>
    <meta-data android:name="com.shopgun.android.sdk.develop.api_secret"
        android:value="your_shopgun_sdk_api_secret_debug"/>
    ...
</application>
```

### Building the ShopGun instance
The ShopGun SDK works with a singleton that must be instantiated in the `onCreate()` of your MainActivity/Application.
Here there is a basic example that extends the base class `Application`:
```java
public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        new ShopGun.Builder(this)
                .setDevelop(true)
                .addNetworkInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC))
                .setInstance();    
    }
}
``` 

### Location
We are very keen on delivering content close to the user, therefore we'll need
to know their location. 

```java
// ShopGun Headquarter (Copenhagen), and radius 100km
SgnLocation loc = ShopGun.getInstance().getLocation();
loc.setLatitude(55.6310771f);
loc.setLongitude(12.5771624f);
loc.setRadius(100000);
loc.setSensor(false);
```

Typically you'll hook into LocationManager to get the device location, rather 
than hardcode it :wink: (but remember to properly ask for permission).

### Performing your first request

Let's try to get a list of catalogs. First of all, define a listener that will handle the request response:

```java
// The callback interface
LoaderRequest.Listener<List<Catalog>> catalogListener = new LoaderRequest.Listener<List<Catalog>>() {

    @Override
    public void onRequestComplete(List<Catalog> response, List<ShopGunError> errors) {
        if (errors.isEmpty()) {
            // Hurray it's a successful request!
            updateUI();
        } else {
            // Whh something went wrong
            showErrorMessage();
        }
    }

    @Override
    public void onRequestIntermediate(List<Catalog> response, List<ShopGunError> errors) {
        // Do intermediate update of UI, or other actions needed.
        if (errors.isEmpty()) {
            // Hurray it's a successful request!
        } else {
            // Whh something went wrong
        }
    }
    
};
```
Then, when you are ready to fetch the data:
```java
void fetchData() {
    showProgress("Fetching catalogs");
    CatalogListRequest r = new CatalogListRequest(catalogListener);
    r.loadStore(true);
    r.loadDealer(true);
    // Limit is default to 24, it's good for cache performance on the API
    r.setLimit(24);
    ShopGun.getInstance().add(r);
}
```
That's it, you've performed your first request to our ShopGun API :smiley:

For more examples, please have a look at the ShopGun SDK Demo. There you can
find some common use cases for the SDK. It's bundled with the SDK. 

Feedback
--------
If you have any feedback, then please feel free to let us know. Issues and 
suggestions can be submitted via GitHub issues. Comments can be emailed to me at
<agb@shopgun.com>


Contributing
------------
If you would like to contribute to the ShopGun Android SDK, feel free to do so.
Just fork the repository on GitHub and send us a pull request.

Please try to follow existing code convention and style when committing code.


Links
-----
[ShopGun API Documentation](http://docs.api.etilbudsavis.dk/)


License
-------

    Copyright 2015 ShopGun
    
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
    
      http://www.apache.org/licenses/LICENSE-2.0
    
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
