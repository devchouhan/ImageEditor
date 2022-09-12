package com.example.imageeditor

import android.app.Application

class SingleToneClass : Application() {
    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object{
        lateinit var instance:SingleToneClass
    }
}