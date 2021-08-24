package com.lenatopoleva.bluetoothclient.mvp.presenter

import com.lenatopoleva.bluetoothclient.mvp.model.IBluetoothService
import com.lenatopoleva.bluetoothclient.mvp.model.repository.IRepository
import com.lenatopoleva.bluetoothclient.mvp.view.ViewerView
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import moxy.MvpPresenter
import ru.terrakok.cicerone.Router
import javax.inject.Inject

class ViewerPresenter (val bluetoothService: IBluetoothService): MvpPresenter<ViewerView>() {

    @Inject
    lateinit var router: Router
    @Inject
    lateinit var repository: IRepository
    @Inject
    lateinit var uiScheduler: Scheduler

    private var disposables = CompositeDisposable()

    fun onStart() {
        tryToConnect()
    }

    private fun tryToConnect(){
        val deviceAddress: String? = repository.getDeviceAddress()
        println("Device address from repo: $deviceAddress")
        if ( deviceAddress != null){
            disposables.add(bluetoothService.connectToDevice(deviceAddress)
                .observeOn(uiScheduler)
                .subscribe(
                    { viewState.showMessage("Device connected")
                        startDataTransmitting() },
                    {
                        viewState.updateTextView("Unable to connect device: ${it.message}")
                        println("Unable to connect device: ${it.message}")
                        bluetoothService.closeSocket()
                    }
                ))
        }
    }

    private fun startDataTransmitting(){
        disposables.add(bluetoothService.startDataTransmitting()
            .subscribeOn(Schedulers.io())
            .observeOn(uiScheduler)
            .subscribe(
                {
                    val message = it.decodeToString()
                    viewState.updateTextView(message)
                    viewState.hideAppBar()
                    viewState.hideActionBar()
                },
                {
                    val errorMessage = it.message
                    errorMessage?.let { viewState.updateTextView("Lost connection: $it")}
                    viewState.showAppBar()
                    viewState.showActionBar()
                },
                {
                    //Do something on complete
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