package com.lenatopoleva.bluetoothclient

import android.app.Application
import com.lenatopoleva.bluetoothclient.di.AppComponent
import com.lenatopoleva.bluetoothclient.di.DaggerAppComponent
import com.lenatopoleva.bluetoothclient.di.modules.AppModule
import com.lenatopoleva.bluetoothclient.util.logger.createLoggerWithDiskLogAdapter

class App: Application() {

    companion object{
        lateinit var instance: App
    }

    lateinit var appComponent: AppComponent
        private set

    override fun onCreate() {
        super.onCreate()
        createLoggerWithDiskLogAdapter(this, "MyTag")
        instance = this
        appComponent =  DaggerAppComponent.builder()
            .appModule(AppModule(this))
            .build()
    }
}