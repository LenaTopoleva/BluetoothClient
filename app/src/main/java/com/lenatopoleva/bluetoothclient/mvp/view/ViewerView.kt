package com.lenatopoleva.bluetoothclient.mvp.view

import kotlinx.coroutines.internal.AddLastDesc
import moxy.MvpView
import moxy.viewstate.strategy.alias.AddToEndSingle

@AddToEndSingle
interface ViewerView: MvpView {
}