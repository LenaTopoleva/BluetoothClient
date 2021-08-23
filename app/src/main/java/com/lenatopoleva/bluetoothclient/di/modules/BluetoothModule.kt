package com.lenatopoleva.bluetoothclient.di.modules

import android.bluetooth.BluetoothAdapter
import dagger.Module
import dagger.Provides


@Module
class BluetoothModule {

    @Provides
    fun bluetoothAdapter(): BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()

}