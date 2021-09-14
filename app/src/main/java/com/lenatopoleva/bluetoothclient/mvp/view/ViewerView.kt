package com.lenatopoleva.bluetoothclient.mvp.view

import moxy.MvpView
import moxy.viewstate.strategy.alias.AddToEndSingle
import moxy.viewstate.strategy.alias.Skip

@AddToEndSingle
interface ViewerView: MvpView {

    fun hideAppBar()
    fun hideActionBar()
    fun showAppBar()
    fun showActionBar()
    fun showImage(imageName: String, subtype: String)
    fun hideTextView()
    fun showImageView()
    fun hideImageView()
    fun showTextView()
    fun hideFab()
    fun showFab()

    @Skip fun startAudio(audioName: String)
    @Skip fun startToneAudioIfEnable(tone: Boolean)
    @Skip fun openChooseFileAlertDialog()
    @Skip fun openFileChooser()

    // Update TextView
    fun showDeviceIsNotConnectedMessage()
    fun showConnectedWithMessage(deviceName: String)

    // Toast
    @Skip fun showDeviceConnectedToast()
    @Skip fun showDataTransmittingExceptionToast(exceptionMessage: String)
    @Skip fun showUnableToConnectDeviceToast(deviceNameAndError: String)
    @Skip fun showChooseDeviceToast()


}