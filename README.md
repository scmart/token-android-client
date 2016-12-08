Token for Android
=================
The android client for connecting to the [Token platform](tokenbrowser.com)

<img src="./app/src/main/res/mipmap-xhdpi/launcher.png?raw=true">

Getting Started
===============

1. Clone this code to your machine
2. Download and fully install [Android Studio](https://developer.android.com/studio/index.html)
3. Open this code in Android Studio (Choosing the containing directory in Android Studio `Open` dialog works)
4. Click the `Play` button (`[Ctrl]` + `[Alt]` + `R`)

Making a new build for Play store
=================================

1. Open app/build.gradle
2. Increase the versionCode by 1.
3. Optionally change the versionName to whatever you want.
4. Build the APK however you want (in Android Studio or via gradle)
    - The APK is split so you will end up with multiple APKs. Gradle takes care of numbering them correctly.
    - You'll need the release certificate.
    - Certificate alias and password are kept in Vault.
5. Commit the changes to build.gradle, upload the APKs to Play store
