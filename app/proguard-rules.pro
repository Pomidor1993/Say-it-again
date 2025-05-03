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

# Włącz usuwanie logów na poziomie optymalizacji
-optimizations code/removal/*

# Media3
-keep class androidx.media3.** { *; }
-dontwarn androidx.media3.**

# ML Kit
-keep class com.google.mlkit.** { *; }
-dontwarn com.google.mlkit.**

# Firebase
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**

# Usuń tylko metody logowania w Firebase (np. FirebaseCrashlytics)
# Usuń logi ze wszystkich klas Twojego pakietu
-assumenosideeffects class com.tomato.sayitagain.** {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
    public static *** w(...);
    public static *** e(...);
}
# Usuń wszystkie wywołania android.util.Log.*
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int d(...);
    public static int i(...);
    public static int w(...);
    public static int e(...);
    public static java.lang.String getStackTraceString(java.lang.Throwable);
}

# Pełna sygnatura metody isLoggable
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int d(...);
    public static int i(...);
    public static int w(...);
    public static int e(...);
}

# Usuń stack trace
-assumenosideeffects class java.lang.Throwable {
    public void printStackTrace();
}

# Zastąp java.util.Random SecureRandom w całym projekcie
-dontwarn java.util.Random
-repackageclasses ''
-adaptclassstrings

# Wymuś używanie SecureRandom
-keep class !java.security.SecureRandom, java.util.Random { *; }


# Wymuś używanie parametrów w zapytaniach SQL
-optimizations !code/sql/*

# Zachowaj bezpieczne metody SQLite
-keep class * extends android.database.sqlite.SQLiteOpenHelper {
    public *;
}