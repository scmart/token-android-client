# Platform calls Class.forName on types which do not exist on Android to determine platform.
-dontnote retrofit2.Platform
# Platform used when running on RoboVM on iOS. Will not be used at runtime.
-dontnote retrofit2.Platform$IOS$MainThreadExecutor
# Platform used when running on Java 8 VMs. Will not be used at runtime.
-dontwarn retrofit2.Platform$Java8
# Retain generic type information for use by reflection by converters and adapters.
-keepattributes Signature
# Retain declared checked exceptions for use by a Proxy instance.
-keepattributes Exceptions
-keepattributes EnclosingMethod

-keepattributes Annotation

-keep enum com.bakkenbaeck.token.network.ws.model.** { *; }

-keepclassmembers class com.supersonicads.sdk.controller.SupersonicWebView$JSInterface {
    public *;
}
-keepclassmembers class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}
-keep,includedescriptorclasses public class com.google.android.gms.ads.** {
   public *;

}
-keep,includedescriptorclasses class com.supersonic.adapters.** { *; }

-keep class com.bakkenbaeck.token.model.** { *; }
-keep class com.bakkenbaeck.token.network.** { *; }

-keep class org.spongycastle.** {*;}

-dontwarn okio.**

-keep class com.squareup.moshi.** { *; }
-keep interface com.squareup.moshi.** { *; }
-dontwarn com.squareup.moshi.**
-keepclassmembers class ** {
    @com.squareup.moshi.FromJson *;
    @com.squareup.moshi.ToJson *;
}

-keep public class com.bakkenbaeck.token.model.jsonadapter.BigIntegerAdapter { *; }

-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}