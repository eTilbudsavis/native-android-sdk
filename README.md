ShopGun Android SDK
===================

The simple solution to querying for ShopGun-data.

<a name="api-key-secret">API key and secret
-----------------
You will need to get an *API key* and *API secret* from our 
[ShopGun Developer site] (look for "My Apps")

If you wish to try our demo app, just clone and run it. We've included an API 
key and secret, that will work straight out of the box. But the key only provides 
a limited amount of quereis pr day so don't use it in production.

Getting Started
---------------

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
	compile 'com.shopgun.android:sdk:3.1.0-beta'
}
```

or clone from github, and add this to your project's `settings.gradle`:

```groovy
include ':shopGunSdk'
project(':shopGunSdk').projectDir=new File('/path/to/shopgun-android-sdk/shopGunSdk')
```

### Setup AndroidManifest.xml

We need certain permissions, to make the whole thing run:
```xml
<!-- Obviously we'll need internet -->
<uses-permission android:name="android.permission.INTERNET"/>
<!-- Check for connectivity, prior to performint shoppinglist synchronization -->
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

### Initialize
You'll need to inititlize the `ShopGun Android SDK` before you can use it. 
You can do this from either `Application` or an `Activity`. 

```java
// You'll need this import
import com.shopgun.android.sdk.ShopGun;

// And add this to your activity class
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

### Location
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

### Performing yout first request

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
That was it, you've performed your first request to our ShopGun API :-)

For more demonstartions on how you can do requests to the ShopGun API, 
please have a look at the ShopGun SDK Demo. It's inclided in the [ShopGun Android SDK] project.


Features
--------


* [Requests](#first-request)
* [Pageflip](#pageflip)
* [SessionManager](#sessionmanager)
* [ListManager](#listmanager)
* [Debugging](#debugging)
* [Models](#models)
* [Utils](#utils)
* [Test](#test)

Feedback
--------



Contributing
------------



License
-------
We are licenced under [Apache-2.0](http://www.apache.org/licenses/LICENSE-2.0).


[ShopGun Developer site]:https://business.shopgun.com/developers/
[ShopGun Android SDK]:https://github.com/shopgun/shopgun-android-sdk/
[ShopGun API Documentation]:http://docs.api.etilbudsavis.dk/
