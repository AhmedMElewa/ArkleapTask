package com.elewa.arkleaptask.core.application

import android.app.Application
import com.mazenrashed.printooth.Printooth
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class ArkleapApp : Application() {

    override fun onCreate() {
        super.onCreate()
        Printooth.init(applicationContext);
    }
}