package com.lenatopoleva.bluetoothclient.mvp.model.repository

import com.lenatopoleva.bluetoothclient.mvp.model.entity.Device

interface IRepository {
    fun saveDevice(device: Device)
    fun getDevice(): Device?
}