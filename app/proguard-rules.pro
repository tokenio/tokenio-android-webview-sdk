# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Users/your_user/Library/Android/sdk/tools/proguard/proguard-android-optimize.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.kts.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If you use reflection or JNI define guards here to keep interfaces,
# methods and fields used from native code.
# -keep PackageName.** { *; }

# If you use libraries that require some extra configuration please refer to the documentation of
# those libraries.

# Keep Moshi generated adapters
-keep class com.squareup.moshi.JsonAdapter { *; }
-keep class com.example.paymentdemoandroid.model.**JsonAdapter { *; }
-keep @com.squareup.moshi.JsonClass class * {
    *; 
}

# Keep classes needed by Retrofit
-dontwarn retrofit2.Platform$Java8
-keep class retrofit2.** { *; }
-keep interface retrofit2.** { *; }

# Keep OkHttp classes
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-dontwarn okhttp3.**
-dontwarn okio.**
