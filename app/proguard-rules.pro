# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in D:\Android\sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:

-ignorewarnings

# Gson
-keep class com.google.gson.stream.** { *; }

# 灵云种子
-keep class com.sinovoice.hcicloudsdk.** {*;}

-keep class android.inputmethodservice.** { *; }

# 语音部分
-keep class com.sinovoice.ai.** { *; }
-keep class com.sinovoice.sdk.** { *; }

#retrofit2
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepattributes Signature
-keepattributes Exceptions
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation

#okhttp
-dontwarn okhttp3.**
-keep class okhttp3.**{*;}
-keep interface okhttp3.**{*;}

-keep class com.zqy.sdk.network.**{*;}

#车牌键盘
-keep class com.parking.keyboard.view.SelectedDrawable
-keep class * extends com.parking.keyboard.view.SelectedDrawable

#英文引擎
-keep class com.superpenguin.foreigninputandoutput.**{*;}
# Keep native methods
-keepclassmembers class * {
    native <methods>;
}