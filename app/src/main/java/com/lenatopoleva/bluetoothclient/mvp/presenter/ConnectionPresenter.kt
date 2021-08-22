package com.lenatopoleva.bluetoothclient.mvp.presenter

import com.lenatopoleva.bluetoothclient.mvp.view.ConnectionView
import moxy.MvpPresenter
import ru.terrakok.cicerone.Router
import javax.inject.Inject

class ConnectionPresenter: MvpPresenter<ConnectionView>() {

    @Inject
    lateinit var router: Router

    fun backClick(): Boolean {
        router.exit()
        return true
    }

}