package com.lenatopoleva.bluetoothclient.di

import com.lenatopoleva.bluetoothclient.di.modules.AppModule
import com.lenatopoleva.bluetoothclient.di.modules.BluetoothModule
import com.lenatopoleva.bluetoothclient.di.modules.ImageLoaderModule
import com.lenatopoleva.bluetoothclient.di.modules.NavigationModule
import com.lenatopoleva.bluetoothclient.mvp.presenter.ConnectionPresenter
import com.lenatopoleva.bluetoothclient.mvp.presenter.MainPresenter
import com.lenatopoleva.bluetoothclient.mvp.presenter.ViewerPresenter
import com.lenatopoleva.bluetoothclient.ui.activity.MainActivity
import com.lenatopoleva.bluetoothclient.ui.fragment.BluetoothService
import com.lenatopoleva.bluetoothclient.ui.fragment.ViewerFragment
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [
    AppModule::class,
    NavigationModule::class,
    ImageLoaderModule::class,
    BluetoothModule::class
])


interface AppComponent {
    fun inject(mainPresenter: MainPresenter)
    fun inject(viewerPresenter: ViewerPresenter)
    fun inject(connectionPresenter: ConnectionPresenter)
    fun inject(mainActivity: MainActivity)
    fun inject(viewerFragment: ViewerFragment)
    fun inject(bluetoothService: BluetoothService)
}