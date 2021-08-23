package com.lenatopoleva.bluetoothclient.mvp.view

import moxy.MvpView
import moxy.viewstate.strategy.alias.AddToEndSingle

@AddToEndSingle
interface ConnectionView: MvpView {
    fun init()
    fun updatePairedDevicesList()
    fun updateNewDevicesList()
}