package com.lenatopoleva.bluetoothclient.mvp.presenter

import com.google.gson.Gson
import com.lenatopoleva.bluetoothclient.mvp.model.IBluetoothService
import com.lenatopoleva.bluetoothclient.mvp.model.entity.BluetoothResponse
import com.lenatopoleva.bluetoothclient.mvp.model.entity.ConnectingStatus
import com.lenatopoleva.bluetoothclient.mvp.model.entity.Device
import com.lenatopoleva.bluetoothclient.mvp.model.repository.IRepository
import com.lenatopoleva.bluetoothclient.mvp.view.ViewerView
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import moxy.MvpPresenter
import ru.terrakok.cicerone.Router
import javax.inject.Inject

class ViewerPresenter (): MvpPresenter<ViewerView>() {

    @Inject
    lateinit var router: Router
    @Inject
    lateinit var repository: IRepository
    @Inject
    lateinit var uiScheduler: Scheduler
    @Inject
    lateinit var bluetoothService: IBluetoothService

    private var disposables = CompositeDisposable()


    fun onStart(deviceFromSharedPrefs: Device) {
        val device = repository.getDevice()
        val deviceStatus = device?.status
        if (device != null) {
            if (deviceStatus == ConnectingStatus.CONNECTED) {
                viewState.updateTextView("Connected with ${device.name}")
                startDataTransmitting()
            }
            if (deviceStatus == ConnectingStatus.NOT_CONNECTED) tryToConnect(device)
        } else if (deviceFromSharedPrefs.address != "") {
            tryToConnect(deviceFromSharedPrefs)
        }
    }

    private fun tryToConnect(device: Device){
        disposables.add(bluetoothService.connectToDevice(device.address)
            .observeOn(uiScheduler)
            .subscribe(
                {   device.status = ConnectingStatus.CONNECTED
                    repository.saveDevice(device)
                    viewState.updateTextView("Connected with ${device.name}")
                    viewState.showMessage("Device connected")
                    startDataTransmitting() },
                {
                    viewState.updateTextView("Unable to connect device ${device.name}: ${it.message}")
                    println("Unable to connect device: ${it.message}")
                    bluetoothService.closeSocket()
                }
            ))
    }

    private fun startDataTransmitting(){
        println("***Start Data Transmitting***")
        disposables.add(bluetoothService.startDataTransmitting()
            .subscribeOn(Schedulers.io())
            .observeOn(uiScheduler)
            .subscribe(
                {   response ->
                    val bluetoothResponse = Gson()
                        .fromJson(response, BluetoothResponse::class.java)
                    println("RESPONSE TYPE = ${bluetoothResponse.type}")
                    if(bluetoothResponse.type == "image"){
                        viewState.hideTextView()
                        viewState.hideAppBar()
                        viewState.hideActionBar()
                        viewState.showImageView()
                        viewState.showImage(bluetoothResponse.data)
                    }
                },
                {
                    val errorMessage = it.message
                    errorMessage?.let {
                        viewState.hideImageView()
                        viewState.showTextView()
                        viewState.updateTextView("Data transmitting exception: $it")
                    }
                    println("Data transmitting exception: ${it.message}")
                    viewState.showAppBar()
                    viewState.showActionBar()
                },
                {
                    println("Data transmitting complete")
                }))
    }


    fun backClick(): Boolean {
        router.exit()
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        disposables.dispose()
    }

}