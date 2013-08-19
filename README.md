# eTilbudsavis - Android SDK

### Introduction
This is a short guide to guide you through our Android SDK. We will assume you
are using [eclipse](http://www.eclipse.org/) and the [Android Development Tools](http://developer.android.com/tools/sdk/eclipse-adt.html) plugin. 
Furthermore you are goind to need an API key and secret from [found here](https://etilbudsavis.dk/developers/api/).
Lastly we have included a ETA SDK Demo app in the repository. It demonstrates 
some basic features, some of which are also described in this README.

### Download
If you want to get started quickly, just clone the [native-android-sdk](https://github.com/eTilbudsavis/native-android-sdk.git) repository.
Start a new Android Application Project and import the ETA SDK into Eclipse as a library via the menu `Project -> Properties -> Android`.


# Usage

## Eta

## Session

## Api
You can include various options into the api.request() call, just create a Bundle 
with key/value pairs, and send it as a parameter. See more about REST API options
[here](https://etilbudsavis.dk/developers/docs/).

## Location

## Pageflip


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


## Shoppinglist Manager


## Utilities

## Feedback
If you have any feedback or comments feel free to contact danny@etilbudsavis.dk :-)
