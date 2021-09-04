package com.lenatopoleva.bluetoothclient.mvp.presenter

import com.lenatopoleva.bluetoothclient.mvp.model.IBluetoothService
import com.lenatopoleva.bluetoothclient.mvp.model.entity.ConnectingStatus
import com.lenatopoleva.bluetoothclient.mvp.model.entity.Device
import com.lenatopoleva.bluetoothclient.mvp.model.repository.IRepository
import com.lenatopoleva.bluetoothclient.mvp.presenter.list.IDevicesListPresenter
import com.lenatopoleva.bluetoothclient.mvp.view.ConnectionView
import com.lenatopoleva.bluetoothclient.mvp.view.list.DeviceItemView
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.disposables.CompositeDisposable
import moxy.MvpPresenter
import ru.terrakok.cicerone.Router
import javax.inject.Inject

class ConnectionPresenter: MvpPresenter<ConnectionView>() {

    @Inject
    lateinit var router: Router
    @Inject
    lateinit var repository: IRepository
    @Inject
    lateinit var uiScheduler: Scheduler
    @Inject
    lateinit var bluetoothServiceImpl: IBluetoothService


    val pairedDevicesListPresenter = PairedDevicesListPresenter()
    val newDevicesListPresenter = NewDevicesListPresenter()
    private var disposables = CompositeDisposable()

    class PairedDevicesListPresenter: IDevicesListPresenter {
        override var itemClickListener: ((DeviceItemView) -> Unit)? = null
        var pairedDevices = mutableListOf<Device>()

        override fun bindView(view: DeviceItemView) {
            val device = pairedDevices[view.pos]
            view.setDeviceName(device.name)
            view.setDeviceAddress(device.address)
        }

        override fun getCount() = pairedDevices.size

    }

    class NewDevicesListPresenter: IDevicesListPresenter {
        override var itemClickListener: ((DeviceItemView) -> Unit)? = null
        val newDevices = mutableListOf<Device>()

        override fun bindView(view: DeviceItemView) {
            val device = newDevices[view.pos]
            view.setDeviceName(device.name)
            view.setDeviceAddress(device.address)
        }

        override fun getCount() = newDevices.size

    }

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        viewState.init()
        getPairedDevices()
        viewState.updatePairedDevicesList()
        searchNewDevices()

        pairedDevicesListPresenter.itemClickListener = { view ->
            view.showConnectingStatus()
            bluetoothServiceImpl.cancelSearch()
            tryToConnect(pairedDevicesListPresenter.pairedDevices[view.pos], view)
        }
        newDevicesListPresenter.itemClickListener = { view ->
            view.showConnectingStatus()
            bluetoothServiceImpl.cancelSearch()
            tryToConnect(pairedDevicesListPresenter.pairedDevices[view.pos], view)
        }
    }

    private fun tryToConnect(device: Device, itemView: DeviceItemView){
        disposables.add(bluetoothServiceImpl.connectToDevice(device.address)
            .observeOn(uiScheduler)
            .subscribe(
                    {   device.status = ConnectingStatus.CONNECTED
                        repository.saveDevice(device)
                        itemView.hideConnectionStatus()
                        println("Device connected")
                        viewState.showDeviceConnectedMessage()
                        viewState.saveDeviceToSharedPreferences(device)
                        router.exit()
                    },
                    {
                        itemView.hideConnectionStatus()
                        viewState.showUnableToConnectDeviceMessage(it.message)
                        println("Unable to connect device: ${it.message}")
                        bluetoothServiceImpl.closeSocket()
                    }
            ))
    }

    private fun getPairedDevices() {
        val pairedDevices = bluetoothServiceImpl.getPairedDevices()
        pairedDevicesListPresenter.pairedDevices.clear()
        pairedDevicesListPresenter.pairedDevices.addAll(pairedDevices)
    }

    private fun searchNewDevices(){
        // show progress
        viewState.showSearchingTextView()
        bluetoothServiceImpl.cancelSearch()
        bluetoothServiceImpl.startSearch()
    }

    fun newDeviceFound(device: Device) {
        newDevicesListPresenter.newDevices.add(device)
        viewState.updateNewDevicesList()
    }

    fun searchForDevicesFinished() {
        // finish progress
        viewState.hideSearchingTextView()
        if (newDevicesListPresenter.newDevices.size == 0)
        println("searchForDevicesFinished")
    }

    fun onSearchFabClicked(){
        pairedDevicesListPresenter.pairedDevices.clear()
        newDevicesListPresenter.newDevices.clear()
        viewState.updatePairedDevicesList()
        viewState.updateNewDevicesList()

        getPairedDevices()
        viewState.updatePairedDevicesList()
        searchNewDevices()
    }

    fun backClick(): Boolean {
        router.exit()
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        disposables.dispose()
    }

    fun onFragmentDestroy() {
        bluetoothServiceImpl.cancelSearch()
    }

}