package com.blackshark.hidperipheral

import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

fun Context.isEnableBluetooth(): Boolean {
    val manager = getSystemService(AppCompatActivity.BLUETOOTH_SERVICE) as BluetoothManager
    val adapter = manager.adapter ?: return false
    return adapter.isEnabled
}

fun Context.hasPermission(permission: String) =
    checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED

fun Context.showToast(resId: Int) {
    Toast.makeText(this, getString(resId), Toast.LENGTH_SHORT).show()
}