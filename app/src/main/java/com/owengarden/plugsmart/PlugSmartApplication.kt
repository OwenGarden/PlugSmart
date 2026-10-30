package com.owengarden.plugsmart

import android.app.Application
import com.owengarden.plugsmart.data.AppContainer
import com.owengarden.plugsmart.data.sync.PriceSyncWorker

/**
 * Runs once when the app starts, before any screen.
 * It holds the [AppContainer] so ViewModels can get the repositories,
 * and schedules the daily background price download.
 * Registered in AndroidManifest.xml with android:name=".PlugSmartApplication".
 */
class PlugSmartApplication : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        PriceSyncWorker.schedule(this)
    }
}
