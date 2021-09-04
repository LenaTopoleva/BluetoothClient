package com.lenatopoleva.bluetoothclient.mvp.view.list

interface DeviceItemView: IItemView {
    fun setDeviceName(name: String)
    fun setDeviceAddress(address: String)
    fun showConnectingStatus()
    fun hideConnectionStatus()
}