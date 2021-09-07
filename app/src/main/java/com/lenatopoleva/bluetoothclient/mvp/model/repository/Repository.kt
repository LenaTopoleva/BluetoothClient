package com.lenatopoleva.bluetoothclient.mvp.model.repository

import com.lenatopoleva.bluetoothclient.mvp.model.entity.Device


class Repository: IRepository {

    private var device: Device? = null

    override fun saveDevice(device: Device) {
        this.device = device
    }

    override fun getDevice(): Device? = device

}