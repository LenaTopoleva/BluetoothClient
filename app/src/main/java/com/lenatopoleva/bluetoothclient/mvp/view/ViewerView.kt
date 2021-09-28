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
    fun hideTextView()
    fun showImageView()
    fun hideImageView()
    fun showTextView()
    fun hideFab()
    fun showFab()

    fun prepareImage(imageName: String, subtype: String)
    fun showImage()

    @Skip fun prepareToneAudioIfEnable(tone: Boolean)
    @Skip fun prepareAudio(audioName: String)
    @Skip fun startToneIfEnable(tone: Boolean)
    @Skip fun startAudio()
    @Skip fun openChooseFileAlertDialog()
    @Skip fun openFilePicker()

    // Update TextView
    fun showDeviceIsNotConnectedMessage()
    fun showConnectedWithMessage(deviceName: String)

    // Toast
    @Skip fun showDeviceConnectedToast()
    @Skip fun showDataTransmittingExceptionToast(exceptionMessage: String)
    @Skip fun showUnableToConnectDeviceToast(deviceNameAndError: String)
    @Skip fun showChooseDeviceToast()

}