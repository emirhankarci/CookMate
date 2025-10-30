package com.emirhankarci.cookmate

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class CookMateApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        // Firebase otomatik başlatılır, ekstra kod gerekmez
    }
}
