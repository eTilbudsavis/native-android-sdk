ShopGun Android SDK
===================

The simple solution to querying for ShopGun-data.

Getting Started
---------------

#### Download with Gradle
If you haven't already added `jCenter` to your `build.gradle`, you'll need this:
```groovy
repositories {
    jcenter() 
}
```

Now add these lines to your module's `build.gradle`:

```groovy
dependencies {
	compile 'com.shopgun.android:sdk:3.1.0-beta'
}
```

or clone from github, and add this to your project's `settings.gradle`:

```groovy
include ':shopGunSdk'
project(':shopGunSdk').projectDir=new File('/path/to/shopgun-android-sdk/shopGunSdk')
```

#### <a name="api-key-secret">API key and secret
You will need to get an *API key* and *API secret* from our 
[ShopGun Developer site] (look for "My Apps")

If you wish to try our demo app, just clone and run it. We've included an API 
key and secret, that will work straight out of the box. But the key only provides 
a limited amount of quereis pr day so don't use it in production.

#### Setup AndroidManifest.xml

We need certain permissions, to make the whole thing run:
```xml
<!-- Obviously we'll need internet -->
<uses-permission android:name="android.permission.INTERNET"/>
<!-- Check for connectivity, prior to performing shoppinglist synchronization -->
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"/>
<!-- Caching of images e.t.c. for Pageflip -->
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
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

#### Initialize
You'll need to inititlize the `ShopGun Android SDK` before you can use it. 
You can do this from either `Application` or an `Activity`. 

```java
// You'll need this import
import com.shopgun.android.sdk.ShopGun;

// And add this to your Application/Activity class
@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    ShopGun.create(this);
}
```

further more, we'll need to know when your `Activity` performs `onStart()` and 
`onStop()`, in order to perform certain synchronization steps.


```java
@Override
protected void onStart() {
    ShopGun.getInstance().onStart();
    super.onStart();
}

@Override
protected void onStop() {
    super.onStop();
    ShopGun.getInstance().onStop();
}

```

#### Location
We are very keen on delivering content close to the user, therefore we'll need
to know their location. 

```java
// ShopGun Headquater (Copenhagen), and radius 100km
SgnLocation loc = ShopGun.getInstance().getLocation();
loc.setLatitude(55.6310771f);
loc.setLongitude(12.5771624f);
loc.setRadius(100000);
loc.setSensor(false);
```

Typically you'll hook into LocationManager to get the device location, rather 
than hardcode it ;-)

#### Performing yout first request

Let's try to get a list of catalogs.

```java
// The callback interface
Response.Listener<JSONArray> listener = new Response.Listener<JSONArray>() {
    @Override
    public void onComplete(JSONArray response, ShopGunError error) {
        if (response != null) {
            // Hurray it's a successful request!
        } else {
            // Whh something went wrong
        }
    }
};
// The request
JsonArrayRequest catalogReq = new JsonArrayRequest(Endpoints.CATALOG_LIST, listener);
// Add the request to the request queue
ShopGun.getInstance().add(catalogReq);
```
That's it, you've performed your first request to our ShopGun API :-)

For more exampels, please have a look at the ShopGun SDK Demo. There you can
find some common usecases for the SDK. It's bundled with the SDK. 


Features
--------


* [Requests](#first-request)
* [Pageflip](#pageflip)
* [SessionManager](#sessionmanager)
* [ListManager](#listmanager)
* [MaterialColor](#MaterialColor)
* [Debugging](#debugging)
* [Models](#models)
* [Utils](#utils)
* [Test](#test)

Feedback
--------
If you have any feedback, then please feel free to let us know. Issues and 
suggestions can be submitted via GitHub issues. Comments can be emailed to me at
<danny@shopgun.com>


Contributing
------------
If you would like to contribute to the ShopGun Android SDK, feel free to do so.
Just fork the repository on GitHub, and send us a pull request.

Please try to follow existing code convention and style, when committing code.


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


[ShopGun Developer site]:https://business.shopgun.com/developers/
[ShopGun Android SDK]:https://github.com/shopgun/shopgun-android-sdk/
[ShopGun API Documentation]:http://docs.api.etilbudsavis.dk/
