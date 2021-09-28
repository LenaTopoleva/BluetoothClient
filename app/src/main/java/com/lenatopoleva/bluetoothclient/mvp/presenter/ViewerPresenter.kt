package com.lenatopoleva.bluetoothclient.mvp.presenter

import com.google.gson.Gson
import com.lenatopoleva.bluetoothclient.mvp.model.IBluetoothService
import com.lenatopoleva.bluetoothclient.mvp.model.entity.BluetoothResponse
import com.lenatopoleva.bluetoothclient.mvp.model.entity.ConnectingStatus
import com.lenatopoleva.bluetoothclient.mvp.model.entity.Device
import com.lenatopoleva.bluetoothclient.mvp.model.repository.IRepository
import com.lenatopoleva.bluetoothclient.mvp.view.ViewerView
import com.lenatopoleva.bluetoothclient.navigation.Screens
import com.orhanobut.logger.Logger
import io.reactivex.rxjava3.core.Completable
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

    private var currentDeviceFromSharedPrefs: Device? = null
    var dataIsTransmitting: Boolean = false

    fun onResume(deviceFromSharedPrefs: Device) {
        if (rootPackageUri == null || rootPackageUri == ""){
            Logger.d("ViewerPresenter onResume; rootPackageUri = $rootPackageUri.")
            viewState.openChooseFileAlertDialog()
        }
        else {
            val device = repository.getDevice()
            val deviceStatus = device?.status
            if (device != null) {
                if (deviceStatus == ConnectingStatus.CONNECTED && !dataIsTransmitting) {
                    Logger.d("ViewerPresenter onResume; deviceStatus: ${device.status}, dataIsTransmitting: $dataIsTransmitting\"")
                    println("onResume, deviceStatus: ${device.status}, dataIsTransmitting: $dataIsTransmitting")
                    viewState.showConnectedWithMessage(device.name)
                    startDataTransmitting(device)
                }
                if (deviceStatus == ConnectingStatus.NOT_CONNECTED) {
                    Logger.d("ViewerPresenter onResume; deviceStatus: ${device.status}")
                    println("onResume, deviceStatus: ${device.status}")
                    tryToConnect(device)
                }
            } else if (deviceFromSharedPrefs.address != "") {
                currentDeviceFromSharedPrefs = deviceFromSharedPrefs
                Logger.d("ViewerPresenter onResume;  device from repo = null, deviceFromSharedPrefs.address != \"\"")
                println("onResume, device = null, deviceFromSharedPrefs.address != \"\"")
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
                    Logger.d("ViewerPresenter tryToConnect; Device connected, device status from repo: ${(repository.getDevice() as Device).status}")
                    println("Device connected, device status from repo: ${(repository.getDevice() as Device).status}")
                    viewState.showConnectedWithMessage(device.name)
                    viewState.showDeviceConnectedToast()
                    startDataTransmitting(device)
                },
                {
                    viewState.showUnableToConnectDeviceToast("${device.name}: ${it.message}")
                    Logger.e("ViewerPresenter tryToConnect; Unable to connect device: ${it.message}")
                    println("Unable to connect device: ${it.message}")
                    bluetoothService.closeSocket()
                }
            ))
    }

    private fun startDataTransmitting(device: Device){
        dataIsTransmitting = true
        println("***Start Data Transmitting***")
        disposables.add(
            bluetoothService.startDataTransmitting()
                .subscribeOn(Schedulers.io())
                .observeOn(uiScheduler)
                .subscribe(
                    { response ->
                        val bluetoothResponse = Gson()
                            .fromJson(response, BluetoothResponse::class.java)
                        Logger.d("ViewerPresenter startDataTransmitting; got response, type = ${bluetoothResponse.type}")
                        println("RESPONSE TYPE = ${bluetoothResponse.type}")
                        when (bluetoothResponse.type) {
                            "show_image" -> {
                                openPictureViewMode()
                                prepareImage(bluetoothResponse.fileName, bluetoothResponse.subtype)
                                    .mergeWith(prepareToneAudioIfEnable(bluetoothResponse.tone))
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(uiScheduler)
                                    .subscribe {
                                        viewState.showImage()
                                        viewState.startToneIfEnable(bluetoothResponse.tone)
                                }
                            }
                            "play_audio" -> {
                                prepareAudio(bluetoothResponse.fileName)
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(uiScheduler)
                                    .subscribe { viewState.startAudio() }
                            }
                            "stop_session" -> {
                                closePictureViewMode()
                                repository.getDevice()?.name?.let {
                                    viewState.showConnectedWithMessage(
                                        it
                                    )
                                }
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
                            Logger.e("ViewerPresenter startDataTransmitting; data transmitting exception: $errorMessage")
                        }
                        println("Data transmitting exception: ${it.message}")
                        bluetoothService.stopDataTransmitting()
                    },
                    {
                        println("Data transmitting complete")
                    })
        )
    }

    private fun prepareToneAudioIfEnable(tone: Boolean) = Completable.fromRunnable {
            viewState.prepareToneAudioIfEnable(tone) }

    private fun prepareImage(imageName: String, subtype: String) = Completable.fromRunnable {
            viewState.prepareImage(imageName, subtype) }

    private fun prepareAudio(audioName: String): Completable = Completable.fromRunnable {
            viewState.prepareAudio(audioName) }

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
        Logger.d(
            "ViewerPresenter changeDeviceStatus; new status: $status, " +
                    "device ${device.name} saved to repo"
        )
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
        viewState.openFilePicker()
    }

    fun fabReconnectClicked() {
        val device = repository.getDevice()
        if (device != null) {
            Logger.d(
                "ViewerPresenter fabReconnectClicked; device from repo != null, " +
                        "status = ${device.status}"
            )
            if (device.status == ConnectingStatus.NOT_CONNECTED) tryToConnect(device)
            else viewState.showDeviceConnectedToast()
        }
        else if (currentDeviceFromSharedPrefs != null && currentDeviceFromSharedPrefs!!.address != ""){
            Logger.d(
                "ViewerPresenter fabReconnectClicked; device from repo = $device, " +
                        "currentDeviceFromSharedPrefs != null"
            )
            tryToConnect(currentDeviceFromSharedPrefs!!)
        } else {
            if(currentDeviceFromSharedPrefs == null) Logger.d(
                "ViewerPresenter " +
                        "fabReconnectClicked; device from repo = $device, " +
                        "currentDeviceFromSharedPrefs = null"
            )
            else Logger.d(
                "ViewerPresenter fabReconnectClicked; device from repo = $device," +
                        " currentDeviceFromSharedPrefs address = ${currentDeviceFromSharedPrefs!!.address}"
            )
            viewState.showChooseDeviceToast()
        }
    }

}