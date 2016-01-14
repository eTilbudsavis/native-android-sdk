Change Log
==========

Version 3.1.1 *(2016-01)*
----------------------------

 * New: New statistics package, to replace `PageStats` for Pageflip
 * Updates to the event log system, to better allow posting of the events
 * New: A bunch of new request types, that'll allow for easy access to API data, 
   including having depencencies automatically bundled into the request. See demo app for an example.

Version 3.1.0 *(2015-10-29)*
----------------------------

 * New: Added ShopGun Android SDK to jCenter
 * Experimental: Tested for Marshmallow (Android 6, SDK v23)
 * Experimental: Intro and outro fragments can be added directly into `PageflipFramgnet`, with this comes some minor tweaks to make it all fit.
 * Fix: `PageStat` has been sending some negative durations. We expect this to be dute to NTP adjusting time. 
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
 * New: Migrated codebase to new package namespace, `com.shopgun.android.sdk` and refactored all class naems to ShopGun name conventions
 * New: All model objects implements `Parcelable`, and removed `Serilazable`
 * New: `MaterialColor` interface, to create new vibrante colors that match the material deign guidelines. 
   And at the same time, implementations can handle, the unfortunate situation of the API delivering, an invalid color string.
 * New: `Environment` to control API endpoints
 * Fix: Various bugs were squished
 * New: `EventBus` (by greenrobot) to post messages around the system
 * Fix: Major speed improvements to `ListManager` and the underlying `SQLite` database.
 * `Picasso` for imageloading, removed `ImageLoader`
 * Experimantal: Better configuration options for `PageflipFragment`, see `ReaderConfig`
 * Experimental: New `filler` package. This should solve some of the problems with creating and controlling multiple requests.
   This is useful in situations where you want to ensure that a ShopGun model, has all prpoerties you need. 
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
 * Fix: Varius bug have been squished
 * Fix: ERN/id mismatch causing problems in `EtaErnObject`
 * Fix: Thread synchronization issues


Version 2.1.0 *(2014-02-06)*
----------------------------

 * New: Reimplementation of the request structure. Now we have a whole networking interface stack, and some implementations of these.
 * New: JavaDoc on all public exposed methods (not all POJO)
 * New: A logging system via `EtaLog`, that will make debugging way easier
 * New: All POJO objects have been refactored
 * Change: Minor refactoring of class names, to make them reflacttheir (new) purposes
 * Change: Refactored Endpoint, Params, Headersand Sort classes into the Utils package(they don't belong as sub-classes in a request)
 * fix: Improved performance in `EtaLocation`, with less write to disk
 * Deleted: `Api` class has been replaced. It wasn't easily extendable, the method for generics was faulty, and the type of request wasn't easy to work with.
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
 * Fix: Fixed odd linebreaks in Pageflip HTML


Initial release.