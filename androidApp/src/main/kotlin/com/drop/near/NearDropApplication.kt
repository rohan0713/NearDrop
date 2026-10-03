package com.drop.near

import android.app.Application
import com.drop.near.di.NearDropSdk

/**
 * Android Application entry point initializing the shared KMP dependency container.
 */
class NearDropApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        NearDropSdk.initialize()
    }
}
