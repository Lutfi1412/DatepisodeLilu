package com.lutfilia.rundown

import android.app.Application

class RundownApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Graph.init(this)
    }
}