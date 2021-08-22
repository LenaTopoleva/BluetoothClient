package com.lenatopoleva.bluetoothclient.navigation

import com.lenatopoleva.bluetoothclient.ui.fragment.ConnectionFragment
import com.lenatopoleva.bluetoothclient.ui.fragment.ViewerFragment
import ru.terrakok.cicerone.android.support.SupportAppScreen

class Screens {

    class ViewerScreen() : SupportAppScreen() {
        override fun getFragment() = ViewerFragment.newInstance()
    }

    class ConnectionScreen() : SupportAppScreen() {
        override fun getFragment() = ConnectionFragment.newInstance()
    }
}