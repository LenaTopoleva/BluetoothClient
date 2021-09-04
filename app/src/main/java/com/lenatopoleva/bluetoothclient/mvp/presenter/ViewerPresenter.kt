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

    var mainPackagePath: String? = null
    var picturesObjectsPath: String? = null
    var picturesActionsPath: String? = null
    var picturesOtherPath: String? = null
    var soundsPath: String? = null
    var toneSoundFileName: String? = null

    fun onStart( deviceFromSharedPrefs: Device) {
        if (mainPackagePath == null || mainPackagePath == ""){
            viewState.openChooseFileAlertDialog()
        }
        else {
            val device = repository.getDevice()
            val deviceStatus = device?.status
            if (device != null) {
                if (deviceStatus == ConnectingStatus.CONNECTED) {
                    viewState.showConnectedWithMessage(device.name)
                    startDataTransmitting()
                }
                if (deviceStatus == ConnectingStatus.NOT_CONNECTED) tryToConnect(device)
            } else if (deviceFromSharedPrefs.address != "") {
                tryToConnect(deviceFromSharedPrefs)
            }
        }
    }

    private fun tryToConnect(device: Device){
        disposables.add(bluetoothService.connectToDevice(device.address)
            .observeOn(uiScheduler)
            .subscribe(
                {   device.status = ConnectingStatus.CONNECTED
                    repository.saveDevice(device)
                    viewState.showConnectedWithMessage(device.name)
                    viewState.showDeviceConnectedToast()
                    startDataTransmitting() },
                {
                    viewState.showUnableToConnectDeviceMessage("${device.name}: ${it.message}")
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
                    when(bluetoothResponse.type) {
                        "show_image" -> {
                            viewState.hideTextView()
                            viewState.hideAppBar()
                            viewState.hideActionBar()
                            viewState.showImageView()
                            viewState.showImage(bluetoothResponse.fileName,
                                bluetoothResponse.subtype, repository.isToneEnabled())
                        }
                        "play_audio" -> viewState.startAudio(bluetoothResponse.fileName )
                        "tone_enable" -> repository.enableTone()
                        "tone_disable" -> repository.disableTone()
                        "stop_session" -> {
                            viewState.showActionBar()
                            viewState.showAppBar()
                            viewState.hideImageView()
                            viewState.showTextView()
                            viewState.showEndOfSessionMessage()
                        }
                    }
                },
                {
                    val errorMessage = it.message
                    errorMessage?.let { message ->
                        viewState.hideImageView()
                        viewState.showTextView()
                        viewState.showDataTransmittingExceptionMessage(message)
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

    fun connectionMenuItemClicked() {
        router.navigateTo(Screens.ConnectionScreen())
    }

    fun chooseFileButtonClicked() {
        viewState.openFileChooser()
    }

}