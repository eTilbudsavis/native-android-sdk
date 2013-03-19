# ETA Android SDK

## Introduction
This is a short guide to guide you through our Android SDK. We will assume you 
are using [eclipse](http://www.eclipse.org/) and the [Android Development Tools](http://developer.android.com/tools/sdk/eclipse-adt.html) plugin. 
Furthermore you are goind to need an API key and secret from [found here](https://etilbudsavis.dk/developers/api/).
Lastly we have included a ETA SDK Demo app in the repository. It demonstrates 
some basic features, some of which are also described in this README.

## Download
If you want to get started quickly, just clone the [native-android-sdk](https://github.com/eTilbudsavis/native-android-sdk.git) repository.
Start a new Android Application Project and import the ETA SDK into Eclipse as a library via the menu `Project -> Properties -> Android`.

## Usage

### Init
First thing is to initialize the SDK, this is done by instantiating a new 
object of ETA (Eclipse will kindly ask you to import packages as you go):

	ETA eta = new ETA(mApiKey, mApiSecret, this);

The ETA generates and holds various information, like UUID for the device (not to be confused with UDID), the 
location (which is optional) and the API object.

### Location
In order to utilize the location awareness, you need  to set the location. 
Luckily this is real easy too, you just need to get the location object, 
and use the setLocation() method:

	eta.getLocation().setLocation(latitude, longitude, geocoded, accuracy, locationDetermined, distance)

As you will find you have several options in the location class. It's also
possible to set bounds on the search results in the API requests, wich is also
done via the location object.

### API
The API makes callbacks via the RequestListener interface, which means you 
need to implement it somehow. We'll just make a new listener:

	RequestListener requestListener = new RequestListener() {
		@Override
		public void onSuccess(Integer response, Object object) {
			JSONObject jObject = new JSONObject(object.toString());
			// Add your code here
		}

		@Override
		public void onError(Integer response, Object object) {
			// Add your code here
		}
	};

You are now ready to get some information from the backend. For example, here's a list of catalogs:

	eta.api.request("/api/v1/catalog/list/", requestListener);

Really easy, huh?

The request listener implements two methods: `onSuccess` and `onError`. Both takes 
a response (some HTTP status code), and an object, typically in JSON or String format, 
containing the requested data. The `onSuccess` callback is only fired on HTTP 
status code 200, all others go to `onError`. The syntax of a response is like this:

	{"status":200,"success":true,"code":100,"description":"OK","data":{}}

Where
- `status` is the HTTP status code.
- `success` is a Boolean indicating whether the request was successful or not.
- `code` is the internal code.
- `description` is a description of what happened.
- `data` is the requested data.

All requests run on the UI thread, but all communication is handled by a class 
extending AsyncTask, so it won't interrupt the UI thread. And the callbacks 
are running on the UI thread.

You can include various options into the api.request() call, just create a Bundle 
with key/value pairs, and send it as a parameter. See more about REST API options
[here](https://etilbudsavis.dk/developers/docs/).

### Pageflip
We have also made it really simple to implement Pageflip in Android.
Create a new WebView in the layout, then set the view in your code:

	WebView webView = (WebView)findViewById(R.id.webView);

Create an Pageflip object:

	Pageflip pageflip = new Pageflip(webView, eta);

As with the API, the pageflip also makes callbacks via an interface:

	PageflipListener pageflipListener = new PageflipListener() {
		@Override
		public void onPageflipEvent(String event, JSONObject object) {
			// Add your code here.
		}
	};

Then call `getWebView` to get a new fancy, ready-to-go WebView:

	webView = pageflip.getWebView(type, content, pageflipListener);

The `onPageflipEvent` fires everytime the user interacts with Pageflip, and takes
an event and an JSON object. The object returned on an pagechange event is:

	{"init":false,"page":1,"pageLabel":"1","pages":[1],"pageCount":28,"id":"3bb64Yg"}

The type of events and their corresponding JSON response currently implemented are:

_pagechange_ - When a page changes

- `init` Indicates the very first page change (finished initialization).
- `page` The current page (normalized to the verso).
- `pageLabel` The entire page spread (1-2, 2-3, ..., n or 1, 2, 3, ..., n).
- `pages` Pages currently visible.
- `pageCount` The total amount of single page pages.
- `id` Catalog identifier.

_outofbounds_ - When desired page is out of bounds 	

- `page` Failed page tried to be reached.
- `direction` In what direction the failed page is in relation to the current page.

_thumbnails_ - Thumbnails dialog toggled 	

- `visible` Whether the dialog is visible or not.

_hotspot_ - A hotspot is clicked

- Offer info.

_singletap_ - A single tap/click

- None.

_doubletap_ - A double tap/click

- None.

_close_ - Pageflip closed

- None.

### Utilities

- `createUUID` Used to generate the UUID for each unique device.
- `logd` Used for logging purposes. Logging can easily be disabled when compiling for production via the "ENABLE_LOG" boolean.
- `buildChecksum` Creates a checksum of a given set of parameters. Note that the order of gives parameters matters, therefore we use a LinkedHashMap.
- `putNameValuePair` Cast and add any object to a list of BasicNameValuePair
- `buildJSString` Builds a JavaScript string e.g. { key : value }.
- `getTime` Gets the current UTC time in seconds.

## Feedback
If you have any feedback or comments feel free to contact danny@etilbudsavis.dk :-)
