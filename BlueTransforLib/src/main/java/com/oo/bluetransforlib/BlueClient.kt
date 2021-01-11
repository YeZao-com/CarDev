package com.oo.bluetransforlib

import android.app.Activity
import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import java.io.InputStream
import java.util.jar.Manifest
import kotlin.math.acos

object BlueClient : Application.ActivityLifecycleCallbacks {

    val TAG = "BlueClient"

    lateinit var mBluetoothAdapter: BluetoothAdapter


    private val workerThread = HandlerThread("work").apply {
        start()
    }
    private val workHandler = Handler(workerThread.looper)

    lateinit var socket: BluetoothSocket

    lateinit var readStream: InputStream

    private val discoveredDevices = ArrayList<BluetoothDevice>()

    private val intentFilter = IntentFilter().apply {
        addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
        addAction(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        addAction(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE)
        addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
        addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED)

        addAction(BluetoothDevice.ACTION_FOUND)
    }

    val bluetoothBroadCastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            Log.i(TAG, "onReceive: ${intent?.action}")
            when (intent?.action) {
                BluetoothDevice.ACTION_FOUND -> {//发现设备
                    val foundedDevice:BluetoothDevice=intent.extras as BluetoothDevice
                    Log.i(TAG, "onReceive: ${foundedDevice.name}")

                }
                BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED -> {// 连接状态改变
                }
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {// 完成发现
                }
                BluetoothAdapter.ACTION_DISCOVERY_STARTED -> {//开始 发现
                }
                BluetoothAdapter.ACTION_STATE_CHANGED -> {// 蓝牙状态改变

                }
            }
        }
    }


    var context: AppCompatActivity? = null
    fun init(activity: AppCompatActivity) {
        context = activity
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

        activity.registerActivityLifecycleCallbacks(this)
//        activity.registerReceiver(bluetoothBroadCastReceiver, intentFilter)
    }


    fun scanDevice(time: Long) {
        if (!checkEnable()) {
            return
        }
        if (mBluetoothAdapter.isDiscovering) {
            return
        }
        discoveredDevices.clear()
        discoveredDevices.addAll(mBluetoothAdapter.bondedDevices)
        if (mBluetoothAdapter.startDiscovery()) {
            Log.i(TAG, "scanDevice: startDiscover")
        } else {
            Log.i(TAG, "scanDevice: faile ${mBluetoothAdapter.state}")
        }
    }

    private fun checkEnable(): Boolean {
        if (mBluetoothAdapter.isEnabled.not()) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            context?.startActivityForResult(enableBtIntent, 11)
            return false
        }
        return true
    }

    override fun onActivityPaused(activity: Activity) {
    }

    override fun onActivityStarted(activity: Activity) {
    }

    override fun onActivityDestroyed(activity: Activity) {
//        activity.unregisterActivityLifecycleCallbacks(this)
        context = null
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
    }

    override fun onActivityStopped(activity: Activity) {
//        activity.unregisterReceiver(bluetoothBroadCastReceiver)
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
    }

    override fun onActivityResumed(activity: Activity) {

        if (ActivityCompat.checkSelfPermission(
                activity,
                android.Manifest.permission.BLUETOOTH
            ) != PackageManager.PERMISSION_GRANTED
            || ActivityCompat.checkSelfPermission(
                activity,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermission(activity)
            return
        }

//        activity.registerReceiver(bluetoothBroadCastReceiver, intentFilter)
    }

    private fun requestPermission(activity: Activity) {
        ActivityCompat.requestPermissions(
            activity, arrayOf(
                android.Manifest.permission.BLUETOOTH,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ), 1
        )
    }
}