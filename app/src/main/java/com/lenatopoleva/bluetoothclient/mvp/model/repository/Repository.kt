package com.lenatopoleva.bluetoothclient.mvp.model.repository

class Repository: IRepository {

    private var address: String? = null

    override fun saveDeviceAddress(address: String) {
        this.address = address
    }

    override fun getDeviceAddress() = address

}