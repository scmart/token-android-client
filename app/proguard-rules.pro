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


-keepclassmembers class com.supersonicads.sdk.controller.SupersonicWebView$JSInterface {
    public *;
}
-keepclassmembers class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}
-keep,includedescriptorclasses public class com.google.android.gms.ads.** {
   public *;
}
-keep,includedescriptorclasses class com.supersonic.adapters.** { *;
}

# bitcoinj
-keep,includedescriptorclasses class org.bitcoinj.wallet.Protos$** { *; }
-keepclassmembers class org.bitcoinj.wallet.Protos { com.google.protobuf.Descriptors$FileDescriptor descriptor; }
-keep,includedescriptorclasses class org.bitcoin.protocols.payments.Protos$** { *; }
-keep,includedescriptorclasses class org.bitcoin.protocols.payments.Protos$** { *; }
-keepclassmembers,includedescriptorclasses class org.bitcoin.protocols.payments.Protos { com.google.protobuf.Descriptors$FileDescriptor descriptor; }
-dontwarn org.bitcoinj.store.WindowsMMapHack
-dontwarn org.bitcoinj.store.LevelDBBlockStore
-dontnote org.bitcoinj.crypto.DRMWorkaround
-dontnote org.bitcoinj.crypto.TrustStoreLoader$DefaultTrustStoreLoader

-keep,includedescriptorclasses class org.bitcoinj.wallet.Wallet { *; }
-keep,includedescriptorclasses class org.bitcoinj.core.* { *; }
-dontwarn org.bitcoinj.store.LevelDBFullPrunedBlockStore**

-keep,includedescriptorclasses class org.slf4j.**
-keep,includedescriptorclasses interface org.slf4j.**
-keep,includedescriptorclasses enum org.slf4j.**
-dontwarn org.slf4j.**

-dontwarn okio.**


-keep class com.squareup.moshi.** { *; }
-keep interface com.squareup.moshi.** { *; }
-dontwarn com.squareup.moshi.**
-keep public class com.bakkenbaeck.toshi.model.jsonadapter.BigIntegerAdapter { *; }