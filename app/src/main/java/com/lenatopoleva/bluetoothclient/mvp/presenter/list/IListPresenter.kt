package com.lenatopoleva.bluetoothclient.mvp.presenter.list

import com.lenatopoleva.bluetoothclient.mvp.view.list.IItemView

interface IListPresenter<V : IItemView> {
    var itemClickListener: ((V) -> Unit)?
    fun bindView(view: V)
    fun getCount(): Int
}