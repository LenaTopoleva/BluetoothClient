package com.lenatopoleva.bluetoothclient.mvp.presenter

import com.lenatopoleva.bluetoothclient.App
import com.lenatopoleva.bluetoothclient.mvp.view.MainView
import com.lenatopoleva.bluetoothclient.navigation.Screens
import moxy.MvpPresenter
import ru.terrakok.cicerone.Router
import javax.inject.Inject

class MainPresenter: MvpPresenter<MainView>() {

    @Inject
    lateinit var router: Router

    init {
        App.instance.appComponent.inject(this)
    }

    private val primaryScreen = Screens.ViewerScreen()

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        router.replaceScreen(primaryScreen)
    }

    fun backClick() {
        router.exit()
    }

    fun connectionMenuItemClicked() {
        router.navigateTo(Screens.ConnectionScreen())
    }

}