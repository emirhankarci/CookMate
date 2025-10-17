package com.emirhankarci.seninlemutfakta

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class SeninleMutfaktaApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        // Firebase otomatik başlatılır, ekstra kod gerekmez
    }
}