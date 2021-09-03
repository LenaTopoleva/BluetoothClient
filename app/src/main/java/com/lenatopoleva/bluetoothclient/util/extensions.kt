package com.lenatopoleva.bluetoothclient.util

fun IntArray.containsOnly(num: Int): Boolean = filter { it == num }.isNotEmpty()
