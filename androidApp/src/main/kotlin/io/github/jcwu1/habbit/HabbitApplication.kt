package io.github.jcwu1.habbit

import android.app.Application
import io.github.jcwu1.habbit.di.initKoin
import org.koin.android.ext.koin.androidContext

class HabbitApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin {
            androidContext(this@HabbitApplication)
        }
    }
}
