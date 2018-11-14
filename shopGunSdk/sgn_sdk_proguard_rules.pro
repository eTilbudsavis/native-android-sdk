# ProGuard rules for ShopgunSdk

# GreenRobot rules needed if you enable ProGuard minification
# http://greenrobot.org/eventbus/documentation/proguard
-keepattributes *Annotation*
-keepclassmembers class * {
    @org.greenrobot.eventbus.Subscribe <methods>;
}
-keep enum org.greenrobot.eventbus.ThreadMode { *; }


## Parcelable
-keepclassmembers class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator CREATOR;
}
