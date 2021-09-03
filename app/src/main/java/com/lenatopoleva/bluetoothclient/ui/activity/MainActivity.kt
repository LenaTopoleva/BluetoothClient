package com.lenatopoleva.bluetoothclient.ui.activity

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.material.snackbar.Snackbar
import com.lenatopoleva.bluetoothclient.App
import com.lenatopoleva.bluetoothclient.R
import com.lenatopoleva.bluetoothclient.databinding.ActivityMainBinding
import com.lenatopoleva.bluetoothclient.mvp.presenter.MainPresenter
import com.lenatopoleva.bluetoothclient.mvp.view.MainView
import com.lenatopoleva.bluetoothclient.ui.BackButtonListener
import com.lenatopoleva.bluetoothclient.util.*
import moxy.MvpAppCompatActivity
import moxy.ktx.moxyPresenter
import ru.terrakok.cicerone.NavigatorHolder
import ru.terrakok.cicerone.android.support.SupportAppNavigator
import javax.inject.Inject


class MainActivity : MvpAppCompatActivity(), MainView, ActivityCompat.OnRequestPermissionsResultCallback {

    private lateinit var binding: ActivityMainBinding

    private val presenter: MainPresenter by moxyPresenter {
        MainPresenter()
    }

    @Inject
    lateinit var navigatorHolder: NavigatorHolder
    private val navigator = SupportAppNavigator(this, supportFragmentManager, R.id.container)

    @Inject
    @JvmField
    var bluetoothAdapter: BluetoothAdapter? = null

    init {
        App.instance.appComponent.inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        println(">>>>>>>MAIN ACTIVITY onCreate<<<<<<<")
        if (bluetoothAdapter != null && !(bluetoothAdapter!!.isEnabled)) {
            createBluetoothRequestIntent()
        }
        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                REQUEST_READ_EXTERNAL_STORAGE
            )
        } else {
            if(savedInstanceState == null) presenter.openViewerScreen()
        }
    }

    override fun onStart() {
        super.onStart()
        println(">>>>>>>MAIN ACTIVITY onStart<<<<<<<")

    }

    override fun onResume() {
        super.onResume()
        println(">>>>>>>MAIN ACTIVITY onResume<<<<<<<")
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_READ_EXTERNAL_STORAGE) {
            if (!grantResults.containsOnly(PackageManager.PERMISSION_GRANTED)) {
                Snackbar.make(
                    binding.activityMain, R.string.permissions_not_granted,
                    Snackbar.LENGTH_SHORT
                ).setDuration(3000).show()
                finish()
            } else {
                presenter.openViewerScreen()
                super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            }
        }
    }

    private fun createBluetoothRequestIntent() {
        val enableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        startActivityForResult(enableIntent, REQUEST_ENABLE_BLUETOOTH)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        println(">>>>>>>MAIN ACTIVITY onActivityResult<<<<<<<")
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode) {
            REQUEST_ENABLE_BLUETOOTH -> {
                if (resultCode == Activity.RESULT_OK) {
                    // do smth
                } else {
                    // User did not enable Bluetooth or an error occured
                    Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show()
                    finish()
                }
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