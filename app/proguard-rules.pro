# Retrofit & Gson Blogger DTOs
-keep class com.locrights.app.data.remote.** { *; }
-keepattributes Signature,InnerClasses,EnclosingMethod
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

# Room Entities
-keep class com.locrights.app.data.local.** { *; }
