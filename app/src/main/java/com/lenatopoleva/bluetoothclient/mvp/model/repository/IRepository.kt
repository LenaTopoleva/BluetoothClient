package com.lenatopoleva.bluetoothclient.mvp.model.repository

interface IRepository {
    fun saveDeviceAddress(address: String): Unit
    fun getDeviceAddress(): String?
}