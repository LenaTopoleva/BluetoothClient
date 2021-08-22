package com.lenatopoleva.bluetoothclient.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.lenatopoleva.bluetoothclient.App
import com.lenatopoleva.bluetoothclient.databinding.ViewerFragmentBinding
import com.lenatopoleva.bluetoothclient.mvp.presenter.ViewerPresenter
import com.lenatopoleva.bluetoothclient.mvp.view.ViewerView
import com.lenatopoleva.bluetoothclient.ui.BackButtonListener
import moxy.MvpAppCompatFragment
import moxy.ktx.moxyPresenter

class ViewerFragment: MvpAppCompatFragment(), ViewerView, BackButtonListener {

    companion object {
        fun newInstance() = ViewerFragment()
    }

    private var _binding: ViewerFragmentBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    val presenter by moxyPresenter { ViewerPresenter().apply { App.instance.appComponent.inject(this) } }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ViewerFragmentBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun backPressed() = presenter.backClick()

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}