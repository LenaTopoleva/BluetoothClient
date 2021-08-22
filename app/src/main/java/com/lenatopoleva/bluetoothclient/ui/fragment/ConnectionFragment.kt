package com.lenatopoleva.bluetoothclient.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.lenatopoleva.bluetoothclient.App
import com.lenatopoleva.bluetoothclient.databinding.ConnectionFragmentBinding
import com.lenatopoleva.bluetoothclient.mvp.presenter.ConnectionPresenter
import com.lenatopoleva.bluetoothclient.mvp.view.ConnectionView
import com.lenatopoleva.bluetoothclient.ui.BackButtonListener
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

    val presenter by moxyPresenter { ConnectionPresenter().apply { App.instance.appComponent.inject(this) } }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ConnectionFragmentBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun backPressed() = presenter.backClick()

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}