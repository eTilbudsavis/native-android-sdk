# Android App Todo

- Bugs
  - Fix facebook analytics (method had deprication) - FacebookException: No attribution id returned from the Facebook application
  - From user: Jeg ved ikke om det er GPS'en, men når jeg klikker på "GPS" under "Skift placering" opdaterer den ikke til hvor jeg er selvom GPS'en er aktiv.

- Pre release checks
  - Check source for log and debug code (EtaLog.d, PrettyPrint, debugNetwork, debugPerformance)
  - Make sure that all debug information is logged correctly
  - Test updating from one version of the apk to another (migration code)

- Release 4.1
  - Make sure, that logging information is available to send from ZendeskFragment

- Release 4.2
  - OfferImageView must be updates, maybe use PhotoView-library
  - Make flip-corner on offers
  - Manual sorting of shoppinglist items
  - Get eta-api status and display it if error
  - Implement Google Maps for location (might not work for store maps, in ListViews due to "black rectangle"-issue)
  - Flattening layout structure by using TextSpannables to create faster UI - this might not work for FontAwesome stuff
  - Better ImageLoader solution, look at using an SDK image cache to enable cross app image caching
  - Implement new offer viewer

- Future improvements
  - Get latest application version from Google Play, and display update dialog
  - Livefeed events
  - Implement new catalog reader (native pageflip)

TODO SDK:
RequestFactory - Needed for testing
ObjectFactory - Needed for testing
Create Mock objects for testing, this will also require a ObjectFactory to create dummy data









