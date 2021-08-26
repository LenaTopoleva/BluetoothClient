package com.lenatopoleva.bluetoothclient.ui

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import com.lenatopoleva.bluetoothclient.App
import com.lenatopoleva.bluetoothclient.mvp.model.IBluetoothService
import com.lenatopoleva.bluetoothclient.mvp.model.entity.Device
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.io.IOException
import java.util.*
import javax.inject.Inject

class BluetoothServiceImpl(): IBluetoothService {

    @Inject
    @JvmField
    var bluetoothAdapter: BluetoothAdapter? = null

    private var pairedDevices: MutableList<Device>? = null
    private var remoteDeviceToConnect: BluetoothDevice? = null
    var socket: BluetoothSocket? = null

    init {
        App.instance.appComponent.inject(this)
    }

    companion object{
        // Unique UUID for this application
        val MY_UUID: UUID = UUID.fromString("04c6093b-0000-1000-8000-00805f9b34fb")
    }

    override fun getPairedDevices(): MutableList<Device> {
        val bluetoothDevicesSet = bluetoothAdapter?.bondedDevices
        pairedDevices = mutableListOf()
        if (bluetoothDevicesSet != null && bluetoothDevicesSet.size > 0) {
            for (device in bluetoothDevicesSet) {
                pairedDevices?.add(Device(device.name, device.address))
            }
        }
        return pairedDevices?: mutableListOf()
    }

    override fun connectToDevice(deviceAddress: String): Completable {
        val device = bluetoothAdapter?.getRemoteDevice(deviceAddress)
        try {
            socket = device?.createRfcommSocketToServiceRecord(MY_UUID)
            println("Socket created; socket: ${socket.toString()}")
        } catch (e: IOException){
           println("Creation socket failed")
        }
        val connectRunnable = Runnable {
            cancelSearch()
            socket?.connect()
        }
        return Completable.fromRunnable(connectRunnable)
            .subscribeOn(Schedulers.io())
    }

    override fun startDataTransmitting(): Observable<ByteArray> {
        val inputStream = socket?.inputStream
        val outputStream = socket?.outputStream

        return Observable.create<ByteArray> { emitter ->
            val buffer = ByteArray(4000)
            while(true) {
                inputStream?.read(buffer)
                emitter.onNext(buffer)
            }
        }
    }

    override fun closeSocket(){
        socket?.close()
    }

    override fun cancelSearch() {
        if (bluetoothAdapter != null && bluetoothAdapter!!.isDiscovering){
            bluetoothAdapter!!.cancelDiscovery()
        }
    }

    override fun startSearch() {
        if (bluetoothAdapter != null) bluetoothAdapter!!.startDiscovery()
    }

}