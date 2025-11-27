# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# BouncyCastle — обязательно для всех версий
-keep class org.bouncycastle.** { *; }
-dontwarn org.bouncycastle.**

# Если используешь рефлексию или динамическую загрузку провайдеров
-keep class org.bouncycastle.jce.provider.BouncyCastleProvider { *; }
-keep class org.bouncycastle.jsse.provider.BouncyCastleJsseProvider { *; }

# Дополнительно на всякий случай (для OpenPGP, X.509 и т.д.)
-keep class org.bouncycastle.openpgp.** { *; }
-keep class org.bouncycastle.crypto.** { *; }