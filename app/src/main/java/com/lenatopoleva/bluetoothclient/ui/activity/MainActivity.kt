package com.lenatopoleva.bluetoothclient.ui.activity

import android.os.Bundle
import com.lenatopoleva.bluetoothclient.App
import com.lenatopoleva.bluetoothclient.R
import com.lenatopoleva.bluetoothclient.databinding.ActivityMainBinding
import com.lenatopoleva.bluetoothclient.mvp.presenter.MainPresenter
import com.lenatopoleva.bluetoothclient.mvp.view.MainView
import com.lenatopoleva.bluetoothclient.ui.BackButtonListener
import moxy.MvpAppCompatActivity
import moxy.ktx.moxyPresenter
import ru.terrakok.cicerone.NavigatorHolder
import ru.terrakok.cicerone.android.support.SupportAppNavigator
import javax.inject.Inject

class MainActivity : MvpAppCompatActivity(), MainView {

    private lateinit var binding: ActivityMainBinding

    private val presenter: MainPresenter by moxyPresenter {
        MainPresenter()
    }

    @Inject
    lateinit var navigatorHolder: NavigatorHolder
    val navigator = SupportAppNavigator(this, supportFragmentManager, R.id.container)

    init {
        App.instance.appComponent.inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.topAppBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.item_connection -> {
                    presenter.connectionMenuItemClicked()
                    true
                }
                else -> false
            }
        }
    }

    override fun onResumeFragments() {
        super.onResumeFragments()
        navigatorHolder.setNavigator(navigator)
    }

    override fun onPause() {
        super.onPause()
        navigatorHolder.removeNavigator()
    }

    override fun onBackPressed() {
        supportFragmentManager.fragments.forEach {
            if (it is BackButtonListener && it.backPressed()) {
                return
            }
        }
        presenter.backClick()
    }
}