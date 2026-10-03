# ProGuard / R8 rules for NearDrop

# Preserve Kotlin Reflection and Serialization annotations
-keepattributes *Annotation*, InnerClasses, EnclosingMethod, Signature

# Kotlinx Serialization
-dontnote kotlinx.serialization.SerializationKt
-keepclassmembers class * {
    *** Companion;
}
-keepclasseswithmembers class * {
    kotlinx.serialization.KSerializer serializer(...);
}
-keepclassmembers class * {
    @kotlinx.serialization.SerialName <fields>;
}

# Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# Data Models and DTOs (prevent stripping fields used in JSON/Bluetooth transfer)
-keep class com.drop.near.data.model.** { *; }
-keep class com.drop.near.domain.model.** { *; }
