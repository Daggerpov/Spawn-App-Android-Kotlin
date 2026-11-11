# R8 rules for the release build (minify + resource shrinking are on in app/build.gradle.kts).

# Readable crash stack traces in Play Console (upload the mapping file R8 writes with each release)
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Generic signatures + annotations are needed by Retrofit and kotlinx.serialization at runtime
-keepattributes Signature,InnerClasses,EnclosingMethod,RuntimeVisibleAnnotations,RuntimeVisibleParameterAnnotations,AnnotationDefault

# --- Retrofit -------------------------------------------------------------------------------------
# The API interface is only reached through Retrofit's proxy, so keep it and its method annotations.
-keep interface com.danielagapov.spawn.core.network.SpawnApi { *; }
-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class retrofit2.Response
# suspend functions are seen by Retrofit as taking a Continuation
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation
-dontwarn retrofit2.**

# --- kotlinx.serialization (our DTOs) ---------------------------------------------------------------
-keepclassmembers @kotlinx.serialization.Serializable class com.danielagapov.spawn.core.model.** {
    *** Companion;
    *** INSTANCE;
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.danielagapov.spawn.core.model.**$$serializer { *; }
-keepclasseswithmembers class com.danielagapov.spawn.core.model.** {
    kotlinx.serialization.KSerializer serializer(...);
}
# Enums are looked up by name when decoding
-keepclassmembers enum com.danielagapov.spawn.core.model.** { *; }

# --- OkHttp ----------------------------------------------------------------------------------------
-dontwarn okhttp3.internal.platform.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**

# --- Credential Manager / Sign in with Google -------------------------------------------------------
-if class androidx.credentials.CredentialManager
-keep class androidx.credentials.playservices.** { *; }
-keep class com.google.android.libraries.identity.googleid.** { *; }

# --- Security crypto (EncryptedSharedPreferences uses Tink) -------------------------------------
-dontwarn com.google.errorprone.annotations.**
-dontwarn javax.annotation.**

# --- Mapbox ships its own consumer rules; silence optional deps it references --------------------
-dontwarn com.mapbox.**
