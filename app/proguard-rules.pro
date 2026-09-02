# Vastavik Computers ProGuard Rules

# ==========================================
# General Android Rules
# ==========================================
-optimizationpasses 5
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-dontpreverify
-verbose
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes Exceptions
-keepattributes InnerClasses
-keepattributes EnclosingMethod
-keepattributes SourceFile,LineNumberTable

# ==========================================
# Kotlin
# ==========================================
-dontwarn kotlin.**
-keep class kotlin.Metadata { *; }
-keepclassmembers class **$WhenMappings {
    <fields>;
}
-keepclassmembers class kotlin.Metadata {
    public <methods>;
}
-keepclassmembers class * {
    @kotlin.jvm.JvmField <fields>;
}
-keepclassmembers class * extends kotlin.Enum {
    <fields>;
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# ==========================================
# Kotlin Coroutines
# ==========================================
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}
-dontwarn kotlinx.coroutines.**

# ==========================================
# Kotlin Serialization
# ==========================================
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

-keep,includedescriptorclasses class com.vastavik.computer.**$$serializer { *; }
-keepclassmembers class com.vastavik.computer.** {
    *** Companion;
}
-keepclasseswithmembers class com.vastavik.computer.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# ==========================================
# Hilt / Dagger
# ==========================================
-dontwarn dagger.hilt.**
-keep class dagger.hilt.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$ViewFragmentContextWrapper { *; }
-keep class * extends dagger.hilt.android.lifecycle.HiltViewModel

-keepclassmembers,allowobfuscation class * {
    @dagger.hilt.android.lifecycle.HiltViewModel <init>(...);
}
-keepclassmembers class * {
    @dagger.hilt.* <fields>;
}
-keep class * extends dagger.hilt.android.internal.** { *; }
-dontwarn dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper
-dontwarn dagger.hilt.android.internal.managers.ViewComponentManager$ViewFragmentContextWrapper

# ==========================================
# Firebase
# ==========================================
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.android.gms.**

# Firebase Auth
-keep class com.google.firebase.auth.** { *; }

# Firebase Firestore
-keep class com.google.firebase.firestore.** { *; }
-keep class com.google.firestore.** { *; }
-keep class com.google.firebase.firestore.Query { *; }

# Firebase Storage
-keep class com.google.firebase.storage.** { *; }

# Firebase Messaging
-keep class com.google.firebase.messaging.** { *; }
-keep class com.vastavik.computer.utils.FirebaseMessagingService { *; }

# Firebase Analytics
-keep class com.google.firebase.analytics.** { *; }

# Google Play Services Auth
-keep class com.google.android.gms.auth.** { *; }
-keep class com.google.android.gms.common.** { *; }

# ==========================================
# Compose
# ==========================================
-dontwarn androidx.compose.**
-keep class androidx.compose.** { *; }
-keepclassmembers class androidx.compose.** {
    volatile <fields>;
}

# ==========================================
# Room
# ==========================================
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# ==========================================
# Coil
# ==========================================
-keep class coil.** { *; }
-dontwarn coil.**

# ==========================================
# YouTube Player
# ==========================================
-keep class com.pierfrancescosoffritti.androidyoutubeplayer.** { *; }
-dontwarn com.pierfrancescosoffritti.androidyoutubeplayer.**

# ==========================================
# Lottie
# ==========================================
-dontwarn com.airbnb.lottie.**
-keep class com.airbnb.lottie.** { *; }

# ==========================================
# Accompanist
# ==========================================
-dontwarn com.google.accompanist.**
-keep class com.google.accompanist.** { *; }

# ==========================================
# Generative AI
# ==========================================
-keep class com.google.ai.client.generativeai.** { *; }
-dontwarn com.google.ai.client.generativeai.**

# ==========================================
# Markwon (Markdown)
# ==========================================
-keep class io.noties.markwon.** { *; }
-dontwarn io.noties.markwon.**

# ==========================================
# Speech Recognition
# ==========================================
-keep class androidx.speech.** { *; }
-dontwarn androidx.speech.**

# ==========================================
# WorkManager
# ==========================================
-keep class * extends androidx.work.Worker
-keep class * extends androidx.work.ListenableWorker
-keepclassmembers class * {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}
-keep class androidx.work.** { *; }
-dontwarn androidx.work.**

# ==========================================
# Data Models (keep from serialization)
# ==========================================
-keep class com.vastavik.computer.data.model.** { *; }
-keepclassmembers class com.vastavik.computer.data.model.** {
    <fields>;
    <init>(...);
}

# ==========================================
# Enums
# ==========================================
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# ==========================================
# Parcelable
# ==========================================
-keepclassmembers class * implements android.os.Parcelable {
    public static final ** CREATOR;
}

# ==========================================
# R8 Rules
# ==========================================
-allowaccessmodification
-repackageclasses ''
-keep class !com.vastavik.computer.BuildConfig { *; }

# ==========================================
# Remove logging in release
# ==========================================
-assumenosideeffects class android.util.Log {
    public static int v(...);
    public static int d(...);
    public static int i(...);
    public static int w(...);
    public static int e(...);
}

# ==========================================
# Keep the application class
# ==========================================
-keep class com.vastavik.computer.VastavikApplication { *; }
-keep class com.vastavik.computer.MainActivity { *; }
