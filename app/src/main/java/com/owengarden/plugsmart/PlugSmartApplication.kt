package com.owengarden.plugsmart

import android.app.Application
import com.owengarden.plugsmart.data.AppContainer

/**
 * Runs once when the app starts, before any screen.
 * It holds the [AppContainer] so ViewModels can get the repository.
 * Registered in AndroidManifest.xml with android:name=".PlugSmartApplication".
 */
class PlugSmartApplication : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
