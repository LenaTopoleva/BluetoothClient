package com.lenatopoleva.bluetoothclient.mvp.model.repository

import com.lenatopoleva.bluetoothclient.mvp.model.entity.Device


class Repository: IRepository {

    private var device: Device? = null
    private var tone: Boolean = false

    override fun saveDevice(device: Device) {
        this.device = device
    }

    override fun getDevice(): Device? = device

    override fun enableTone() {
        tone = true
    }

    override fun disableTone() {
        tone = false
    }

    override fun isToneEnabled(): Boolean = tone

}