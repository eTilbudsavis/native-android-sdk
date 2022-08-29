![Generic badge](https://img.shields.io/badge/minSdkVersion-21-informational.svg)

#### Relevant dependencies
[![Generic badge](https://img.shields.io/badge/retrofit-2.9.0-informational.svg)](https://github.com/square/retrofit)
[![Generic badge](https://img.shields.io/badge/moshi-1.13.0-informational.svg)](https://github.com/square/moshi)
[![Generic badge](https://img.shields.io/badge/room-2.4.3-informational.svg)](https://developer.android.com/jetpack/androidx/releases/room)
[![Generic badge](https://img.shields.io/badge/glide-4.13.2-informational.svg)](https://github.com/bumptech/glide)

TjekSDK
==========

![Logo](docs/SDKAppIcon-120w.png)

This is an SDK for interacting with the different Tjek services from within your own apps.

## Installation
todo: jitpack/maven

## Getting Started
### Application keys
In order to use our SDK you will need to sign up for a free [developer account](https://etilbudsavis.dk/developers).

This will give you an `API key` and a `track ID`. The SDK must be initialized with these values in order to work.

The easiest way to initialize the SDK is to put the keys in the `AndroidManifest.xml` file:

```xml
<application>
    ...
    <meta-data
        android:name="com.tjek.sdk.api_key"
        android:value="your_api_key"/>
    <meta-data
	android:name="com.tjek.sdk.application_track_id"
	android:value="your_track_id"/>
	    
	
    <!-- Optionally, you can also add the correspondent keys for development phase. 
These are used only when the build is DEBUG type.-->   
    <meta-data
        android:name="com.tjek.sdk.develop.api_key"
        android:value="your_develop_api_key"/>
    <meta-data
	android:name="com.tjek.sdk.develop.application_track_id"
	android:value="your_develop_track_id"/>	    
    ...
</application>
```

You can also set these keys at runtime, but **you need to set them before performing any request with the SDK**:

* `TjekSDK.setApiKey(String)`: this will set your api key.
* `TjekSDK.setApplicationTrackId(String)`: this will set the application track id.

If you wish to try our demo app, just clone the project and run it. We've included an API 
key that will work straight out of the box. But the key only provides 
a limited amount of queries per day so don't use it in production.

### Java 8 and desugaring
The sdk uses the new `java.time` classes insead of the old and deprecated `Date` and `Calendar`. 
You need to add the following to your **app module**'s `build.gradle` file:

```
android {
	compileOptions {
        // Flag to enable support for the new language APIs (For AGP 4.1+)
        coreLibraryDesugaringEnabled = true
        sourceCompatibility JavaVersion.VERSION_1_8
        targetCompatibility JavaVersion.VERSION_1_8
    }

dependencies {
	coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:1.1.5")
}
```
Check the [official guide](https://developer.android.com/studio/write/java8-support#library-desugaring) to verify other constraint you may have in your app.

## Usage
### Publication viewer
There are two different ways of showing publications: [Incito](https://tjek.com/incito/) (vertically scrolling dynamic content) or PDF (horizontally paged static images).

You can choose which one to use based on the `hasIncitoPublication` and `hasPagedPublication` properties on the [`PublicationV2`](tjekSdk/src/main/java/com/tjek/sdk/api/models/PublicationV2.kt) model; you can fetch this model using one of the publication requests available in [`TjekAPI`](tjekSdk/src/main/java/com/tjek/sdk/api/TjekAPI.kt).

#### Incito viewer
In order to show an Incito publication, you need to use [`IncitoPublicationFragment`](tjekSdk/src/main/java/com/tjek/sdk/publicationviewer/incito/IncitoPublicationFragment.kt). You can also add an [`IncitoEventListener`](tjekSdk/src/main/java/com/tjek/sdk/publicationviewer/incito/IncitoEventListener.kt) to it to receive relevant events.
Take a look at the example [`IncitoPublicationActivity`](tjekSdkDemo/src/main/java/com/tjek/sdk/demo/publication/IncitoPublicationActivity.kt) for more details.

#### PDF viewer
In order to show a PDF publication, you need to use [`PagedPublicationFragment`](tjekSdk/src/main/java/com/tjek/sdk/publicationviewer/paged/PagedPublicationFragment.kt). You can also add different types of [listeners](tjekSdk/src/main/java/com/tjek/sdk/publicationviewer/paged/Interfaces.kt) to it to receive relevant events.
Take a look at the example [`PagedPublicationActivity`](tjekSdkDemo/src/main/java/com/tjek/sdk/demo/publication/PagedPublicationActivity.kt) for more details.

#### Common aspects
Both fragments can be created with a `newInstance` call. For each fragment type there are two variations of the call, depending on the parameters you have:

* `newInstance(Id, <type>PublicationConfiguration)`: use this if you only have the `publicationId`
* `newInstance(PublicationV2, <type>PublicationConfiguration)`: use this if you have the whole `PublicationV2` model already.

Depending on the type of fragment, you need to use [`IncitoPublicationConfiguration`](tjekSdk/src/main/java/com/tjek/sdk/publicationviewer/incito/IncitoPublicationConfiguration.kt) or [`PagedPublicationConfiguration`](tjekSdk/src/main/java/com/tjek/sdk/publicationviewer/paged/PagedPublicationConfiguration.kt). Take a look at the examples to see how they're used, [`IncitoPublicationActivity`](tjekSdkDemo/src/main/java/com/tjek/sdk/demo/publication/IncitoPublicationActivity.kt) and [`PagedPublicationActivity`](tjekSdkDemo/src/main/java/com/tjek/sdk/demo/publication/PagedPublicationActivity.kt).


Both type of fragments allow you to customize the loading screen and the error screen. To do that, you have to add a callback using `setCustomScreenCallback(callback: LoaderAndErrorScreenCallback)`. If you don't provide it, the fragments will use the default error and loading views. Take a look at [`LoaderAndErrorScreen.kt`](tjekSdk/src/main/java/com/tjek/sdk/publicationviewer/LoaderAndErrorScreen.kt) file to see what you need to provide and how the default callbacks look like.

### TjekAPI
In the [`TjekAPI`](tjekSdk/src/main/java/com/tjek/sdk/api/TjekAPI.kt) object you can find all the network requests that is possible to perform using the Tjek SDK (remember to call `TjekSDK.setApiKey` if you don't provide a key in the manifest before using any of these functions). All the functions are `suspend fun` and return a `ResponseType` that can be `Success`, carrying the expected data type in the `data` property, or `Error` that contains an optional code and an optional message.

Some api return a data type of `PaginatedResponse`, usually returned when you request some lists of data. In this object there is a `results` property containing the list of data you asked and a `PageInfo` object containing the info regarding the next run of the same call. If `PageInfo.hasNextPage` is true it means that there are more data that can be loaded and you can runt the same call passing `PageInfo.lastCursor` as value for `PaginatedRequestV2.nextPage`.

Here is an example: fetching a list of publications given a certain location.

```
val location = LocationQuery(Coordinate(55.6310771, 12.5771624))

var hasMoreToLoad = true
var pagination = PaginatedRequestV2.firstPage()

while(hasMoreToLoad) {
    when (val res = TjekAPI.getPublications(nearLocation = location, pagination = pagination)) {
        is ResponseType.Error -> println("Error")
        is ResponseType.Success -> {
        	// do something with the data
            println("from ${pagination.startCursor}")
            println("publications: ${res.data.results.joinToString { it.branding.name.toString() }}")
            
            // update the info in the pagination object
            pagination = pagination.nextPage(res.data.pageInfo.lastCursor)
            
            // see if there are more data we can ask
            hasMoreToLoad = res.data.pageInfo.hasNextPage
        }
    }
}
```

#### Location
The Tjek SDK is unaware of any location information and *it won't store it* in any way. It is the app's responsibility to get it somehow. Some api calls have an optional `LocationQuery` parameter where you can set a `Coordinate(latitude, longitude)` and an optional `maxRadius` (in meters) if you want to limit the results to a certain area around the given coordinates.

If you want your location info to be recorded also in the events sent by the Tjek SDK, you need to set it using `TjekSDK.setEventsLocation(Location)`: we recommend to set it only if the location is coming from a GPS-sourced data. All the events generated after you set the location will carry that info.


## Changelog
For a history of changes to the SDK, see the [changelog](CHANGELOG.md) file.

*This also includes migration steps if needed.*


## License

    Copyright (C) 2022 Tjek
    
	Licensed under the Apache License, Version 2.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at
	
	     http://www.apache.org/licenses/LICENSE-2.0
	
	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.
