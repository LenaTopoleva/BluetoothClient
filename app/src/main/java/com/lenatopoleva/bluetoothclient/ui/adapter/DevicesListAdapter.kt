package com.lenatopoleva.bluetoothclient.ui.adapter

import android.view.LayoutInflater
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.lenatopoleva.bluetoothclient.databinding.ItemDeviceBinding
import com.lenatopoleva.bluetoothclient.mvp.presenter.list.IDevicesListPresenter
import com.lenatopoleva.bluetoothclient.mvp.view.list.DeviceItemView

class DevicesListAdapter(val presenter: IDevicesListPresenter): RecyclerView.Adapter<DevicesListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): DevicesListAdapter.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
//        val containerView = inflater.inflate(R.layout.item_device, parent, false)
//        containerView.setOnClickListener{
//            presenter.itemClickListener?.invoke(this)
//        }
        val itemBinding = ItemDeviceBinding.inflate(inflater, parent, false)
        return ViewHolder(itemBinding).apply {
            itemBinding.root.setOnClickListener { presenter.itemClickListener?.invoke(this) }
        }

    }

    override fun onBindViewHolder(holder: DevicesListAdapter.ViewHolder, position: Int) {
        holder.pos = position
        presenter.bindView(holder)
    }

    override fun getItemCount(): Int {
        return presenter.getCount()
    }

    inner class ViewHolder(private val binding: ItemDeviceBinding): RecyclerView.ViewHolder(binding.root),
    DeviceItemView {

        override var pos = -1

        override fun setDeviceName(name: String) {
            binding.tvDeviceName.text = name
        }

        override fun setDeviceAddress(address: String) {
            binding.tvDeviceAddress.text = address
        }

        override fun showConnectingStatus(connection: String) {
            binding.tvConnectingStatus.visibility = VISIBLE
            binding.tvConnectingStatus.text = connection
        }

        override fun hideConnectionStatus() {
            binding.tvConnectingStatus.visibility = GONE
        }
    }
}