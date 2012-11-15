# ETA Android SDK

## Introduction
This is a short guide, in the use of this android ETA SDK. We will assume you 
are using [eclipse](http://www.eclipse.org/), and are using the 
[Android Development Tools](http://developer.android.com/tools/sdk/eclipse-adt.html) plugin. 
Further more you are goint to need an API key and secret from [eTilbudsavis.dk](https://etilbudsavis.dk/developers/api/).
Lastly we have included a ETA SDK Demo app in the repository. It demostrates 
some basic features, some of which are also described in this readme.

## Download
If you want to get started quickly, just clone the [native-android-sdk](https://github.com/eTilbudsavis/native-android-sdk.git) repository.
Start a new Android Application Project and import the ETA SDK into eclipse as a library via the menu "Project->Properties->Android".

## Usage


### Init
First thing is to initialize the SDK, this is done by instanciating a new 
object of ETA (eclipse will kindly ask you to import packages as you go):

	ETA eta = new ETA(mApiKey, mApiSecret, this);

The ETA generates and holds various information, like UUID for the device, the 
location (which is optional) and the api object.

### Location
In order to utilize the location awareness 
[eTilbudsavisen.dk](https://etilbudsavis.dk/developers/docs/) offers, you need 
to set the location. Luckily this is real easy too, you just need to work the 
location object inside the newly created eta class:

	eta.location.setLocation(latitude, longitude, geocoded, accuracy, locationDetermined, distance)

As you will find you have several options in the location class. It's also
possible to set bounds on the search results in the api requests, wich is likewise
done via the location object.

### Api
The api makes callback's via the RequestListener interface, which means you 
need to implement it somehow. We'll just make a new listener:

	RequestListener requestListener = new RequestListener() {
			@Override
			public void onSuccess(String response, Object state) {
				JSONObject catalogList = (JSONObject)state;
			}
			@Override
			public void onError(String response, Object state) {
				// Add your code here
			}
		};

You are now ready to get some information from eTilbudsavisen.dk, the api request
goes via the eta object. Lets try getting a catalog list:

	eta.api.request("/api/v1/catalog/list/", requestListener);

Wow, that was easy, less that 15 lines of code and you got a catalog list ;-)

The request listener implements two methods: onSuccess and onError. Both takes 
a response (some status code), and an object, typically in JSON or html format, 
containing the requested data. The onSuccess callback is only fired on HTTP 
status code 200, all others go to onError. The syntax of a response is like this:

	{"status":200,
	"success":true,
	"code":100,
	"description":"OK",
	"data":{ JSON Data }}

Where
* _status_ - Is the HTTP status code
* _success_ - Was request successful?
* _code_ - Code
* _description_ - Description
* _data_ - JSON or html data object


All requests run on the UI thread, but all communication is handled by a class 
extending AsyncTask, so it won't interrupt the UI thread. And the callback's 
are running on the UI thread.

You can include various options into the api.request() call, just create a Bundle 
with key/value pairs, and send it as a parameter. See more about REST API options 
at [eTilbudsavisen.dk](https://etilbudsavis.dk/developers/docs/).


### Pageflip

We have also made it really simple to implement the pageflip in Android.
Create a new WebView in the layout, then set the view in your code:

	WebView webView = (WebView)findViewById(R.id.webView1);

Create an Pageflip object:

	Pageflip pageflip = new Pageflip(webView, eta);

As with the API, the pageflip also makes callbacks via an interface:

	PageflipListener pageflipListener = new PageflipListener() {
			@Override
			public void onPageflipEvent(String event, JSONObject object) {
				// Add your code here
			}
	};

Then call getWebView to get a new fancy, ready-to-go WebView:

	webView = pageflip.getWebView(type, content, pageflipListener);

The onPageflipEvent fires everytime the user interacts with the pageflip, and takes
an event and an JSON object. The object returned on an pagechange event is:

	{"init":false,
	"page":1,
	"pageLabel":"1",
	"pages":[1],
	"pageCount":28,
	"id":"3bb64Yg"}

The type of events and JSON response currently implemented are:

_pagechange_ - When a page changes
* init - Indicates the very first pagechange (finished initialization)
* page - The current page (normalized to the verso)
* pageLabel - The entire page spread (1-2, 2-3, ..., n or 1, 2, 3, ..., n)
* pages - Pages currently visible
* pageCount - The total amount of single page pages
* id - Catalog identifier

_outofbounds_ - When desired page is out of bounds 	
* page - Failed page tried to be reached
* direction - In what direction the failed page is in relation to the current page

_thumbnails_ - Thumbnails dialog toggled 	
* visible - Whether the dialog is visible or not

_hotspot_ - A hotspot is clicked
* Offer info

_singletap_ - A single tap/click
* None

_doubletap_ - A double tap/click
* None

_close_ - Pageflip closed
* None

### Utilities

* createUUID - Used to generate the UUID for each unique device
* logd - Used for logging purposes. Logging can easily be disabled when compiling
for production via the "ENABLE_LOG" boolean.
* buildChecksum - Creates a checksum of a given set of parameters. Note that
the order of gives parameters matters, therefore we use a LinkedHashMap.
* buildParams - Builds GET/POST parameters from a given LinkedHashMap.
* buildJSString - Builds a JavaScript string e.g. { key : value }
* getTime - Gets the current UTC time in seconds

## Feedback
If you have any feedback or comments please write to: morten@etilbudsavis.dk
