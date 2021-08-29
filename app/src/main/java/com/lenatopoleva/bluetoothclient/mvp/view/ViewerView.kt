package com.lenatopoleva.bluetoothclient.mvp.view

import moxy.MvpView
import moxy.viewstate.strategy.alias.AddToEndSingle
import moxy.viewstate.strategy.alias.Skip

@AddToEndSingle
interface ViewerView: MvpView {

    @Skip
    fun showMessage(message: String)

    fun updateTextView(message: String)
    fun hideAppBar()
    fun hideActionBar()
    fun showAppBar()
    fun showActionBar()
    fun showImage(image: String)
    fun hideTextView()
    fun showImageView()
    fun hideImageView()
    fun showTextView()

    @Skip
    fun startAudio(audioData: String?, audioCount: Int)
}