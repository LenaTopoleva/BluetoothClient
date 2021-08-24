package com.lenatopoleva.bluetoothclient.ui.fragment

import android.bluetooth.BluetoothAdapter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import com.lenatopoleva.bluetoothclient.App
import com.lenatopoleva.bluetoothclient.databinding.ViewerFragmentBinding
import com.lenatopoleva.bluetoothclient.mvp.presenter.ViewerPresenter
import com.lenatopoleva.bluetoothclient.mvp.view.ViewerView
import com.lenatopoleva.bluetoothclient.ui.BackButtonListener
import com.lenatopoleva.bluetoothclient.ui.BluetoothServiceImpl
import com.lenatopoleva.bluetoothclient.ui.activity.MainActivity
import moxy.MvpAppCompatFragment
import moxy.ktx.moxyPresenter
import javax.inject.Inject

class ViewerFragment: MvpAppCompatFragment(), ViewerView, BackButtonListener {

    companion object {
        fun newInstance() = ViewerFragment()
        const val REQUEST_ENABLE_BLUETOOTH = 1
    }

    private var _binding: ViewerFragmentBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    val presenter by moxyPresenter { ViewerPresenter(BluetoothServiceImpl())
        .apply { App.instance.appComponent.inject(this) } }

    @Inject
    @JvmField
    var bluetoothAdapter: BluetoothAdapter? = null

    init {
        App.instance.appComponent.inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ViewerFragmentBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onStart() {
        super.onStart()
        presenter.onStart()
    }

    override fun showMessage(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }

    override fun updateTextView(message: String) {
        binding.tvConnectionStatus.text = ""
        binding.tvConnectionStatus.text = message
    }

    override fun hideAppBar() {
        (activity as MainActivity).topAppBar.visibility = View.GONE
    }

    override fun hideActionBar() {
        activity?.window?.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        activity?.actionBar?.hide();
    }

    override fun showAppBar() {
        (activity as MainActivity).topAppBar.visibility = View.VISIBLE
    }

    override fun showActionBar() {
        activity?.window?.clearFlags( WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        activity?.actionBar?.show();
    }

    override fun backPressed() = presenter.backClick()

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}