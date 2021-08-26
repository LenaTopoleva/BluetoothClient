package com.lenatopoleva.bluetoothclient.mvp.model.entity

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Device (val name: String,
                   val address: String,
                   var status: ConnectingStatus = ConnectingStatus.NOT_CONNECTED): Parcelable

enum class ConnectingStatus(){
    CONNECTED, NOT_CONNECTED
}