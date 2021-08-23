package com.lenatopoleva.bluetoothclient.ui.fragment

import android.bluetooth.BluetoothAdapter
import com.lenatopoleva.bluetoothclient.App
import com.lenatopoleva.bluetoothclient.mvp.model.Device
import javax.inject.Inject

class BluetoothService() {

//    private val bluetoothAdapter by lazy {
//        BluetoothAdapter.getDefaultAdapter()
//    }

    @Inject
    @JvmField
    var bluetoothAdapter: BluetoothAdapter? = null

    private var pairedDevices: MutableList<Device>? = null

    init {
        App.instance.appComponent.inject(this)
    }

    fun getPairedDevices(): MutableList<Device> {
        val bluetoothDevicesSet = bluetoothAdapter?.bondedDevices
        pairedDevices = mutableListOf()
        if (bluetoothDevicesSet != null && bluetoothDevicesSet.size > 0) {
            for (device in bluetoothDevicesSet) {
                pairedDevices?.add(Device(device.name, device.address))
            }
        }
        return pairedDevices?: mutableListOf()
    }

    fun cancelSearch() {
        if (bluetoothAdapter != null && bluetoothAdapter!!.isDiscovering){
            bluetoothAdapter!!.cancelDiscovery()
        }
    }

    fun startSearch() {
        if (bluetoothAdapter != null) bluetoothAdapter!!.startDiscovery()
    }

}