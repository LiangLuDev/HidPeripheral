package com.blackshark.hidperipheral

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.blackshark.hidperipheral.databinding.ActivityMainBinding
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

@OptIn(DelicateCoroutinesApi::class)
class MainActivity : AppCompatActivity(), HidUtils.ConnectionStateChangeListener {
    private val TAG = "MainActivity"
    private lateinit var binding: ActivityMainBinding

    var bluetoothPermission = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == Activity.RESULT_OK) {
            if (isEnableBluetooth()) {
                showToast(R.string.toast_bluetooth_on)
            } else {
                showToast(R.string.toast_bluetooth_off)
            }
        }
    }

    var discoverPermission = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        Log.d(TAG, ": ${it.resultCode}")
        if (it.resultCode == 120) {
            start()
        }
    }
    var connectPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
        if (!it) {
            showToast(R.string.toast_permission)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnStart.setOnClickListener {
            if (Build.VERSION.SDK_INT >= 31 && !hasPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
                connectPermission.launch(Manifest.permission.BLUETOOTH_CONNECT)
                return@setOnClickListener
            }
            if (!isEnableBluetooth()) {
                bluetoothPermission.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
                return@setOnClickListener
            }
            discoverPermission.launch(Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE))
        }

        binding.btnMouse.setOnClickListener {
            if (HidUtils.isConnected()) {
                startActivity(Intent(this, MouseActivity::class.java))
            }
        }

        binding.btnKeyboard.setOnClickListener {
            if (HidUtils.isConnected()) {
                startActivity(Intent(this, KeyboardActivity::class.java))
            }
        }
    }

    private fun start() {
        HidUtils.registerApp(applicationContext)
        HidConsts.reporters(applicationContext)
        HidUtils.connectionStateChangeListener = this
    }

    override fun onConnecting() {
    }

    override fun onConnected() {
        if (Build.VERSION.SDK_INT >= 31 && ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        GlobalScope.launch(Dispatchers.Main) {
            binding.tvConnectStatus.text = "${getString(R.string.connected)} : ${HidUtils.mDevice!!.name}"
        }

    }

    override fun onDisConnected() {
        GlobalScope.launch(Dispatchers.Main) {
            binding.tvConnectStatus.text = getString(R.string.ununited)
        }

    }
}