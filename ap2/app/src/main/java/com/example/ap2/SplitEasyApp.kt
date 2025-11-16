package com.example.ap2

import android.app.Application
import com.example.ap2.data.TripRepository

class SplitEasyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        TripRepository.initialize(this)
    }
}
