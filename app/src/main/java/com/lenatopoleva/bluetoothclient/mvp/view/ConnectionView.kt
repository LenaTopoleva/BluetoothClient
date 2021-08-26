package com.lenatopoleva.bluetoothclient.mvp.view

import com.lenatopoleva.bluetoothclient.mvp.model.entity.Device
import moxy.MvpView
import moxy.viewstate.strategy.alias.AddToEndSingle

@AddToEndSingle
interface ConnectionView: MvpView {
    fun init()
    fun updatePairedDevicesList()
    fun updateNewDevicesList()
    fun saveDeviceAddress(address: String)
    fun showMessage(s: String)
    fun saveDeviceToSharedPreferences(device: Device)
}