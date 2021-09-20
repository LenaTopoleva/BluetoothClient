package com.lenatopoleva.bluetoothclient.mvp.presenter

import com.google.gson.Gson
import com.lenatopoleva.bluetoothclient.mvp.model.IBluetoothService
import com.lenatopoleva.bluetoothclient.mvp.model.entity.BluetoothResponse
import com.lenatopoleva.bluetoothclient.mvp.model.entity.ConnectingStatus
import com.lenatopoleva.bluetoothclient.mvp.model.entity.Device
import com.lenatopoleva.bluetoothclient.mvp.model.repository.IRepository
import com.lenatopoleva.bluetoothclient.mvp.view.ViewerView
import com.lenatopoleva.bluetoothclient.navigation.Screens
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import moxy.MvpPresenter
import ru.terrakok.cicerone.Router
import javax.inject.Inject

class ViewerPresenter: MvpPresenter<ViewerView>() {


    @Inject
    lateinit var router: Router
    @Inject
    lateinit var repository: IRepository
    @Inject
    lateinit var uiScheduler: Scheduler
    @Inject
    lateinit var bluetoothService: IBluetoothService

    private var disposables = CompositeDisposable()

    var rootPackageUri: String? = null
    var picturesObjectsPackageName: String? = null
    var picturesActionsPackageName: String? = null
    var picturesOtherPackageName: String? = null
    var soundsPackageName: String? = null
    var toneSoundFileName: String? = null

    var currentDeviceFromSharedPrefs: Device? = null
    var dataIsTransmitting: Boolean = false

    fun onStart( deviceFromSharedPrefs: Device) {
//        if (mainPackagePath == null || mainPackagePath == ""){
        if (rootPackageUri == null || rootPackageUri == ""){
            viewState.openChooseFileAlertDialog()
        }
        else {
            val device = repository.getDevice()
            val deviceStatus = device?.status
            if (device != null) {
                if (deviceStatus == ConnectingStatus.CONNECTED && !dataIsTransmitting) {
                    println("onStart, deviceStatus: ${device.status}, dataIsTransmitting: $dataIsTransmitting")
                    viewState.showConnectedWithMessage(device.name)
                    startDataTransmitting(device)
                }
                if (deviceStatus == ConnectingStatus.NOT_CONNECTED) {
                    println("onStart, deviceStatus: ${device.status}")
                    tryToConnect(device)
                }
            } else if (deviceFromSharedPrefs.address != "") {
                currentDeviceFromSharedPrefs = deviceFromSharedPrefs
                println("onStart, device = null, deviceFromSharedPrefs.address != \"\"")
                tryToConnect(deviceFromSharedPrefs)
            }
        }
    }

    private fun tryToConnect(device: Device){
        disposables.add(bluetoothService.connectToDevice(device.address)
            .observeOn(uiScheduler)
            .subscribe(
                {
                    changeDeviceStatus(device, ConnectingStatus.CONNECTED)
                    println("Device connected, device status from repo: ${(repository.getDevice() as Device).status}")
                    viewState.showConnectedWithMessage(device.name)
                    viewState.showDeviceConnectedToast()
                    startDataTransmitting(device) },
                {
                    viewState.showUnableToConnectDeviceToast("${device.name}: ${it.message}")
                    println("Unable to connect device: ${it.message}")
                    bluetoothService.closeSocket()
                }
            ))
    }

    private fun startDataTransmitting(device: Device){
        dataIsTransmitting = true
        println("***Start Data Transmitting***")
        disposables.add(bluetoothService.startDataTransmitting()
            .subscribeOn(Schedulers.io())
            .observeOn(uiScheduler)
            .subscribe(
                {   response ->
                    val bluetoothResponse = Gson()
                        .fromJson(response, BluetoothResponse::class.java)
                    println("RESPONSE TYPE = ${bluetoothResponse.type}")
                    when(bluetoothResponse.type) {
                        "show_image" -> {
                            openPictureViewMode()
                            viewState.showImage(bluetoothResponse.fileName,
                                bluetoothResponse.subtype)
                            viewState.startToneAudioIfEnable(bluetoothResponse.tone)
                        }
                        "play_audio" -> viewState.startAudio(bluetoothResponse.fileName )
                        "stop_session" -> {
                            closePictureViewMode()
                            repository.getDevice()?.name?.let { viewState.showConnectedWithMessage(it) }
                        }
                    }
                },
                {
                    dataIsTransmitting = false
                    changeDeviceStatus(device, ConnectingStatus.NOT_CONNECTED)
                    closePictureViewMode()
                    viewState.showDeviceIsNotConnectedMessage()
                    val errorMessage = it.message
                    errorMessage?.let { message ->
                        viewState.showDataTransmittingExceptionToast(message)
                    }
                    println("Data transmitting exception: ${it.message}")
                    bluetoothService.stopDataTransmitting()
                },
                {
                    println("Data transmitting complete")
                }))
    }

    private fun openPictureViewMode(){
        viewState.hideTextView()
        viewState.hideAppBar()
        viewState.hideActionBar()
        viewState.showImageView()
        viewState.hideFab()
    }

    private fun closePictureViewMode(){
        viewState.showTextView()
        viewState.showAppBar()
        viewState.showActionBar()
        viewState.hideImageView()
        viewState.showFab()
    }

    private fun changeDeviceStatus(device: Device, status: ConnectingStatus){
        device.status = status
        repository.saveDevice(device)
    }

    fun backClick(): Boolean {
        router.exit()
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        disposables.dispose()
    }

    fun connectionMenuItemClicked() {
        router.navigateTo(Screens.ConnectionScreen())
    }

    fun chooseFileButtonClicked() {
        viewState.openFileChooser()
    }

    fun fabReconnectClicked() {
        val device = repository.getDevice()
        if (device != null) {
            if (device.status == ConnectingStatus.NOT_CONNECTED) tryToConnect(device)
            else viewState.showDeviceConnectedToast()
        }
        else if (currentDeviceFromSharedPrefs != null && currentDeviceFromSharedPrefs!!.address != ""){
            tryToConnect(currentDeviceFromSharedPrefs!!)
        } else viewState.showChooseDeviceToast()
    }

}