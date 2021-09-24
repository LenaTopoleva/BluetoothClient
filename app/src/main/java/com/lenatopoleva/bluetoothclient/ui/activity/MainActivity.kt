package com.lenatopoleva.bluetoothclient.ui.activity

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import com.google.android.material.snackbar.Snackbar
import com.lenatopoleva.bluetoothclient.App
import com.lenatopoleva.bluetoothclient.R
import com.lenatopoleva.bluetoothclient.databinding.ActivityMainBinding
import com.lenatopoleva.bluetoothclient.mvp.presenter.MainPresenter
import com.lenatopoleva.bluetoothclient.mvp.view.MainView
import com.lenatopoleva.bluetoothclient.ui.BackButtonListener
import com.lenatopoleva.bluetoothclient.util.*
import com.orhanobut.logger.Logger
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

    private val bluetoothRequestActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()) {
            result: ActivityResult ->
        Logger.d("MainActivity got activity result BLUETOOTH REQUEST")
        println(">>>MAIN ACTIVITY got activity result BLUETOOTH REQUEST<<<")
        if (result.resultCode != Activity.RESULT_OK) {
            // User did not enable Bluetooth or an error occured
            Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    init {
        App.instance.appComponent.inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Logger.d("MainActivity onCreate")
        println(">>>MAIN ACTIVITY onCreate<<<")
        if (bluetoothAdapter != null && !(bluetoothAdapter!!.isEnabled)) {
            createBluetoothRequestIntent()
        }
        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                REQUEST_READ_EXTERNAL_STORAGE
            )
        }
    }

    override fun onStart() {
        super.onStart()
        Logger.d("MainActivity onStart")
        println(">>>MAIN ACTIVITY onStart<<<")

    }

    override fun onResume() {
        super.onResume()
        Logger.d("MainActivity onResume")
        println(">>>MAIN ACTIVITY onResume<<<")
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        Logger.d("MainActivity onRequestPermissionsResult REQUEST_READ_EXTERNAL_STORAGE")
        println(">>>MAIN ACTIVITY onRequestPermissionsResult REQUEST_READ_EXTERNAL_STORAGE<<<")
        if (requestCode == REQUEST_READ_EXTERNAL_STORAGE) {
            if (!grantResults.containsOnly(PackageManager.PERMISSION_GRANTED)) {
                Snackbar.make(
                    binding.activityMain, R.string.permissions_not_granted,
                    Snackbar.LENGTH_SHORT
                ).setDuration(3000).show()
                finish()
            } else {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            }
        }
    }

    private fun createBluetoothRequestIntent() {
        Logger.d("MainActivity createBluetoothRequestIntent")
        println(">>>MAIN ACTIVITY launch BluetoothRequestIntent<<<")
        val enableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        bluetoothRequestActivityResultLauncher.launch(enableIntent)
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