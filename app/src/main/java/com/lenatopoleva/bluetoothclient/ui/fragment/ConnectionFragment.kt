package com.lenatopoleva.bluetoothclient.ui.fragment

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.lenatopoleva.bluetoothclient.App
import com.lenatopoleva.bluetoothclient.databinding.ConnectionFragmentBinding
import com.lenatopoleva.bluetoothclient.mvp.model.Device
import com.lenatopoleva.bluetoothclient.mvp.presenter.ConnectionPresenter
import com.lenatopoleva.bluetoothclient.mvp.view.ConnectionView
import com.lenatopoleva.bluetoothclient.ui.BackButtonListener
import com.lenatopoleva.bluetoothclient.ui.adapter.DevicesListAdapter
import moxy.MvpAppCompatFragment
import moxy.ktx.moxyPresenter

class ConnectionFragment: MvpAppCompatFragment(), ConnectionView, BackButtonListener {

    companion object {
        fun newInstance() = ConnectionFragment()
    }

    private var _binding: ConnectionFragmentBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    val presenter by moxyPresenter { ConnectionPresenter(BluetoothService())
        .apply { App.instance.appComponent.inject(this) }
    }

    private val pairedDevicesAdapter by lazy {
        DevicesListAdapter(presenter.pairedDevicesListPresenter)
    }
    private val newDevicesAdapter by lazy {
        DevicesListAdapter(presenter.newDevicesListPresenter)
    }

    // The BroadcastReceiver that listens for discovered devices
    private val broadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            val action = intent.action

            if (BluetoothDevice.ACTION_FOUND == action) {
                val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                // If it's already paired, skip it, because it's been listed already
                if (device!!.bondState != BluetoothDevice.BOND_BONDED) {
                   presenter.newDeviceFound(Device(device.name, device.address))
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED == action) {
               presenter.searchForDevicesFinished()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ConnectionFragmentBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun init() {
        with(binding) {
            rvPairedDevices.layoutManager = LinearLayoutManager(requireContext())
            rvPairedDevices.adapter = pairedDevicesAdapter
            rvNewDevices.layoutManager = LinearLayoutManager(requireContext())
            rvNewDevices.adapter = newDevicesAdapter
            fabSearchDevices.setOnClickListener { presenter.onSearchFabClicked() }
        }
        // Register for broadcasts when a device is discovered
        var filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        activity?.registerReceiver(broadcastReceiver, filter)

        // Register for broadcasts when discovery has finished
        filter = IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        activity?.registerReceiver(broadcastReceiver, filter)
    }

    override fun updatePairedDevicesList() {
        pairedDevicesAdapter.notifyDataSetChanged()
    }

    override fun updateNewDevicesList() {
        newDevicesAdapter.notifyDataSetChanged()
    }

    override fun backPressed() = presenter.backClick()

    override fun onDestroy() {
        super.onDestroy()
        presenter.onFragmentDestroy()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}