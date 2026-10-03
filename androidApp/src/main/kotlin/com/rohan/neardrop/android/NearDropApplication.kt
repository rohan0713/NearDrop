package com.rohan.neardrop.android

import android.app.Application
import com.rohan.neardrop.di.NearDropSdk

/**
 * Android Application entry point initializing the shared KMP dependency container.
 */
class NearDropApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        NearDropSdk.initialize()
    }
}
