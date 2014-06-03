# Android SDK Todo

- Bugs
  - 

- Pre release checks
  - Check source for log and debug code (EtaLog.d, PrettyPrint, debugNetwork, debugPerformance)
  - Make sure that all debug information is logged correctly
  - Test updating from one version of the apk to another (migration code)

- Release 2.1
  - 

- Future improvements
  - Methods for cancelling requests to e.g. SessinManager, so no callbacks will 
    occour post fragment/activity onPause/onStop.
  - Handling of OutOfMemoryError (either clear logs, or create smaller logs)
  - Re-implement queueing of "same" requests, before they hit the cache-queue
  - Native Pageflip View
  - Testing package
    - RequestFactory
    - ObjectFactory
    - Create Mock objects for testing, this will also require a ObjectFactory to create dummy data


