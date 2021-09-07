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
    fun showImage(imageName: String, subtype: String, tone: Boolean)
    fun hideTextView()
    fun showImageView()
    fun hideImageView()
    fun showTextView()

    @Skip fun startAudio(audioName: String)
    @Skip fun openChooseFileAlertDialog()
    @Skip fun openFileChooser()

    // Update TextView
    fun showDataTransmittingExceptionMessage(exceptionMessage: String)
    fun showEndOfSessionMessage()
    fun showUnableToConnectDeviceMessage(deviceNameAndError: String)
    fun showConnectedWithMessage(deviceName: String)

    // Toast
    @Skip fun showDeviceConnectedToast()
}