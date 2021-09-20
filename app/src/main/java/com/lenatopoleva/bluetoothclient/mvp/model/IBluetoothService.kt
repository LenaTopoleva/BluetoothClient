package com.lenatopoleva.bluetoothclient.mvp.model

import com.lenatopoleva.bluetoothclient.mvp.model.entity.Device
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable

interface IBluetoothService {
    fun cancelSearch()
    fun startSearch()
    fun getPairedDevices(): MutableList<Device>
    fun connectToDevice(deviceAddress: String): Completable
    fun closeSocket()
    fun startDataTransmitting(): Observable<String>
    fun stopDataTransmitting()
}