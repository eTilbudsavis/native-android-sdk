Change Log
==========

Version 5.1.0
-------------


Version 5.0.0
-------------
* AndroidX
* Requires Java 8
* Add a dependency to [android-apollo](https://github.com/apollographql/apollo-android) 
for GraphQL queries (not actively used by the sdk right now, but it'll be in the future)
* Dependency updates
* Bug fixes

Version 4.0.3
-------------
* AndroidX compatible.
* Updated: `eventskit` has been changed to our new anonymous event format. `Event` is deprecated, now is `AnonymousEvent`
* Everything related to shopping list is now deprecated.
We'll no longer maintain this feature and all the classes will be removed at some point in the future.
* Bug fixes

Version 4.0.2-beta
----------------------------
* New: `Glide` now replaces `Picasso` for loading images
* Everything related to shopping list will be deprecated in the next release. 
We'll no longer maintain this feature and all the classes will be removed at some point in the future.

Version 4.0.1-beta
----------------------------
Fixed maven dependencies issues.  
Now you no longer need to have a local copy of Markhor, ZoomLayout and Verso projects; you can have one single dependency to the sdk
adding `implementation 'com.shopgun.android:sdk:x.y.z'` to your gradle file.

Version 4.0.0
----------------------------
With this version, the major changes regards showing the catalogs. The classes used before to show the pages are no longer available
and you will need to perform a migration to the new classes.
* Removed: `Pageflip` package is no longer available to display catalog pages and has been replaced by the new `pagepublicationkit` package.  
For example, if you were using `PageflipFragment`, now it will be `PagedPublicationFragment.newInstance(new CatalogConfiguration(my_catalog))`.
Take a look at `PagedPublicationActivity` in the demo app for an example of an Activity that shows catalog pages.  
The new kit also supports showing optional hotspots in the UI, highlighting the offers in the page.
* New: `eventskit` package to send events about opening catalogs and offers to our endpoint or custom endpoints. Events are dispatched every 120 secs
in batch of 100 events at most. Event system uses `Realm` instance to store events locally before sending them.
* Various bug fix

Version 3.2.2
----------------------------
* New: `RedirectProtocol` to handle redirects when performing http requests.
* Removed: Legacy network `DefaultHttpNetwork`, due to problems with redirect, and SSL certificates.
* Migrated a bunch utility methods to project Markhor and performed a cleanup in the remaining

Version 3.2.1 *(2016-04-20)*
----------------------------
* Prepared for Android Marshmallow
* Added functionality to `ShopGun.Builder`
* Bugfix in key handling
* Removed file caching on external storage

Version 3.2.0 *(2016-03-04)*
----------------------------

* New: ModelRequests, each model should ideally have their own request subclass, that'll make life a lot easier for developers
  * Callbacks with model objects, rather than JSON, giving type safety
  * Parsing performed on a non-ui-thread, giving a smoother user experience
  * Easy loading of sidecar objects
* New: Network implementations allowing mocking of some API requests
* Improved JSON to model object mechanisms, allowing some basic error checking.
* Fix: Some minor issues in PageflipFragment have been sorted out
* Removed: The WeakEventBus-class that previously wrapped EventBus have been removed. Better buckle up.
* Various other minor bug fixes

Version 3.1.1 *(2016-01)*
----------------------------

 * New: New statistics package, to replace `PageStats` for Pageflip
 * Updates to the event log system, to better allow posting of the events
 * New: A bunch of new request types, that'll allow for easy access to API data, 
   including having dependencies automatically bundled into the request. See demo app for an example.

Version 3.1.0 *(2015-10-29)*
----------------------------

 * New: Added ShopGun Android SDK to jCenter
 * Experimental: Tested for Marshmallow (Android 6, SDK v23)
 * Experimental: Intro and outro fragments can be added directly into `PageflipFramgnet`, with this comes some minor tweaks to make it all fit.
 * Fix: `PageStat` has been sending some negative durations. We expect this to be due to NTP adjusting time. 
   So we'll be using `SystemClock.elapsedRealtime()` to hopefully fix this issue.


Version 3.0.1 *(2015-09-27)*
----------------------------

 * Fix: Bug in Json parsing.


Version 3.0.0 *(2015-09-17)*
----------------------------

This version brings a lot of new features, and improvements. But it has come at a cost, migration of old code will
be very close to impossible. We've moved away from Eclipse into Android Studio, and at the same time changed package 
namespace, and most class names. It was a move we've wanted to perform for a while, and we felt now was the right time.
We are sorry for breaking everything.

 * New: Moved codebase to Android Studio structure. Using Gradle as build system.
 * New: Migrated codebase to new package namespace, `com.shopgun.android.sdk` and refactored all class names to ShopGun name conventions
 * New: All model objects implements `Parcelable`, and removed `Serilazable`
 * New: `MaterialColor` interface, to create new vibrant colors that match the material deign guidelines. 
   And at the same time, implementations can handle, the unfortunate situation of the API delivering, an invalid color string.
 * New: `Environment` to control API endpoints
 * Fix: Various bugs were squished
 * New: `EventBus` (by greenrobot) to post messages around the system
 * Fix: Major speed improvements to `ListManager` and the underlying `SQLite` database.
 * `Picasso` for image loading, removed `ImageLoader`
 * Experimental: Better configuration options for `PageflipFragment`, see `ReaderConfig`
 * Experimental: New `filler` package. This should solve some of the problems with creating and controlling multiple requests.
   This is useful in situations where you want to ensure that a ShopGun model, has all properties you need. 
   A `Catalog` might need `Store` and `Dealer` to be set, this should easily and safely be accomplished with this.
 * Deleted the package `com.shopgun.android.sdk.request` it was fast, but not very safe.


Version 2.2.0 *(2015-01-12)*
----------------------------

 * New: `PageflipFragment`, yes no more JavaScript
 * New: `ImageLoader` to help `PageflipFragment` load images faster
 * New: `PhotoView` to project (by Chris Banes) 
 * New: Networking performance, with new loader classes
 * New: Started writing tests for models, and features in SDK


Version 2.1.1
----------------------------

 * This version has a few breaking changes, due to some memory issues that had to be fixed.
 * New: Implemented a Logger interface, allowing for better logging
 * Change: All logs are now logged to the same log, see `Logger`
 * Change: Reduced memory footprint
 * Fix: Various bug have been squished
 * Fix: ERN/id mismatch causing problems in `EtaErnObject`
 * Fix: Thread synchronization issues


Version 2.1.0 *(2014-02-06)*
----------------------------

 * New: Reimplementation of the request structure. Now we have a whole networking interface stack, and some implementations of these.
 * New: JavaDoc on all public exposed methods (not all POJO)
 * New: A logging system via `EtaLog`, that will make debugging way easier
 * New: All POJO objects have been refactored
 * Change: Minor refactoring of class names, to make them reflect their (new) purposes
 * Change: Refactored Endpoint, Params, Headersand Sort classes into the Utils package(they don't belong as sub-classes in a request)
 * fix: Improved performance in `EtaLocation`, with less write to disk
 * Deleted: `Api` class has been replaced. It was not easily extendable, the method for generics was faulty, and the type of request was not easy to work with.
 * fix: Major improvements to `SyncManager`, in keeping state between the local data, server data, and the changes performed in between start and end of a sync cycle


Version 2.0.1 *(2014-02-06)*
----------------------------

 * Fix: Implemented more type safety when parsing JSON for primitive types


Version 2.0.0 *(2014-02-06)*
----------------------------

 * New: `Eta` as a singleton, for ease of use
 * New: Implemented POJO objects, and factory methods for JSON to POJO
 * New: Added `Api` as common request type, which requrns POJO instead of JSON.
 * New: An simple cache solution for API requests via `API`
 * New: Automatic creation and refreshing of Sessions, with `SessionManager`
 * New: A `ShoppinglistManager` as an easy to use interface to create, update and delete shoppinglists, and items
 * New: SQLite database to save local shoppinglist and item state
 * New: A `ListSyncManager`, that will synchronize, and merge local state with remote state
 * New: JavaScript interface for communication between Pageflip and the apps
 * Fix: Major speed improvements for Pageflip (see example)
 * Deleted: Basically a complete re-write, so all of v1.0


Version 1.0.0 *(2013-04-18)*
----------------------------

 * New: Everything
 * Fix: SSL hack to use our wildcard certificate
 * Fix: Using threads instead of `AsyncTask`, as `AsyncTask` is slow
 * Fix: Fixed odd line breaks in Pageflip HTML


Initial release.