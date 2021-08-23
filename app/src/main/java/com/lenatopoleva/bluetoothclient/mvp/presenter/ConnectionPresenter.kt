package com.lenatopoleva.bluetoothclient.mvp.presenter

import com.lenatopoleva.bluetoothclient.mvp.model.Device
import com.lenatopoleva.bluetoothclient.mvp.presenter.list.IDevicesListPresenter
import com.lenatopoleva.bluetoothclient.mvp.view.ConnectionView
import com.lenatopoleva.bluetoothclient.mvp.view.list.DeviceItemView
import com.lenatopoleva.bluetoothclient.ui.fragment.BluetoothService
import io.reactivex.rxjava3.disposables.CompositeDisposable
import moxy.MvpPresenter
import ru.terrakok.cicerone.Router
import javax.inject.Inject

class ConnectionPresenter(private val bluetoothService: BluetoothService): MvpPresenter<ConnectionView>() {

    @Inject
    lateinit var router: Router

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
    }

    private fun getPairedDevices() {
        val pairedDevices = bluetoothService.getPairedDevices()
        pairedDevicesListPresenter.pairedDevices.clear()
        pairedDevicesListPresenter.pairedDevices.addAll(pairedDevices)
    }

    private fun searchNewDevices(){
        // show progress
        bluetoothService.cancelSearch()
        bluetoothService.startSearch()
    }

    fun newDeviceFound(device: Device) {
        newDevicesListPresenter.newDevices.add(device)
        viewState.updateNewDevicesList()
    }

    fun searchForDevicesFinished() {
        // finish progress
        if (newDevicesListPresenter.newDevices.size == 0) newDeviceFound(Device("No devices found"))
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
        bluetoothService.cancelSearch()
    }

}