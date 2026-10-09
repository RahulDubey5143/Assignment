package com.example.learning

import android.app.Application
import com.example.learning.data.local.DatabaseDriverFactory

class LearningApp : Application() {

    lateinit var container: AppContainer
    override fun onCreate() {
        super.onCreate()
        container = AppContainer(DatabaseDriverFactory(this))
    }
}
