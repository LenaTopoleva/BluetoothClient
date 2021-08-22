package com.lenatopoleva.bluetoothclient.mvp.presenter

import com.lenatopoleva.bluetoothclient.mvp.view.ViewerView
import moxy.MvpPresenter
import ru.terrakok.cicerone.Router
import javax.inject.Inject

class ViewerPresenter: MvpPresenter<ViewerView>() {

    @Inject
    lateinit var router: Router

    fun backClick(): Boolean {
        router.exit()
        return true
    }

}