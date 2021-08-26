package com.lenatopoleva.bluetoothclient.di.modules

import android.bluetooth.BluetoothAdapter
import com.lenatopoleva.bluetoothclient.mvp.model.IBluetoothService
import com.lenatopoleva.bluetoothclient.ui.BluetoothServiceImpl
import dagger.Module
import dagger.Provides
import javax.inject.Singleton


@Module
class BluetoothModule {

    @Provides
    fun bluetoothAdapter(): BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()

    @Singleton
    @Provides
    fun bluetoothService(): IBluetoothService = BluetoothServiceImpl()

}