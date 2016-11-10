# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /home/cleyton/Android/Sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Jsoup rules
-keep class com.cleytongoncalves.centralufmt.data.local.HtmlHelper.** { *; }
-keeppackagenames org.jsoup.nodes

# ButterKnife rules
-keep class butterknife.** { *; }
-dontwarn butterknife.internal.**
-keep class **$$ViewBinder { *; }

-keepclasseswithmembernames class * {
    @butterknife.* <fields>;
}

-keepclasseswithmembernames class * {
    @butterknife.* <methods>;
}

# OkHttp rules
-dontwarn okio.**
-dontwarn com.squareup.okhttp.**

# Gson rules
-keepattributes Signature
-keep class sun.misc.Unsafe { *; }
# Keep non static or private fields of models so Gson can find their names
-keepclassmembers class centralufmt.data.model.** {
    !static !private <fields>;
}

# Produces useful obfuscated stack traces
# http://proguard.sourceforge.net/manual/examples.html#stacktrace
-renamesourcefileattribute SourceFile
-keepattributes SourceFile,LineNumberTable