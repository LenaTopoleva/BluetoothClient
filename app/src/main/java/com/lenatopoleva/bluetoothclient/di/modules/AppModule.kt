package com.lenatopoleva.bluetoothclient.di.modules

import android.media.MediaPlayer
import com.lenatopoleva.bluetoothclient.App
import com.lenatopoleva.bluetoothclient.mvp.model.repository.IRepository
import com.lenatopoleva.bluetoothclient.mvp.model.repository.Repository
import dagger.Module
import dagger.Provides
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Scheduler
import java.io.File
import javax.inject.Singleton

@Module
class AppModule(val app: App) {

    @Provides
    fun uiScheduler(): Scheduler = AndroidSchedulers.mainThread()

    @Provides
    fun app(): App {
        return app
    }

    @Singleton
    @Provides
    fun repository(): IRepository = Repository()

    @Singleton
    @Provides
    fun mediaPlayer(): MediaPlayer = MediaPlayer()

}
