package com.lenatopoleva.bluetoothclient.ui.fragment

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.lenatopoleva.bluetoothclient.R
import com.lenatopoleva.bluetoothclient.mvp.model.Device

class BluetoothService() {

    private val bluetoothAdapter by lazy {
        BluetoothAdapter.getDefaultAdapter()
    }

    private var pairedDevices: MutableList<Device>? = null

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
        if (bluetoothAdapter != null && bluetoothAdapter.isDiscovering){
            bluetoothAdapter.cancelDiscovery()
        }
    }

    fun startSearch() {
        if (bluetoothAdapter != null) bluetoothAdapter.startDiscovery()
    }

}