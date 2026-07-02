# Retrofit
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.drivertest.app.data.remote.dto.** { *; }
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }

# Gson
-keep class com.google.gson.** { *; }
-keepattributes EnclosingMethod

# Room
-keep class com.drivertest.app.data.local.entity.** { *; }

# ML Kit
-dontwarn com.google.mlkit.**
