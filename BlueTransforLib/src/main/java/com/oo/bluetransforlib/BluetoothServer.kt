package com.oo.bluetransforlib

import android.Manifest
import android.app.Activity
import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import androidx.core.app.ActivityCompat
import com.oo.bluetransforlib.base.io.Connection
import com.oo.bluetransforlib.base.io.IConnection
import com.oo.bluetransforlib.base.server.BaseServer
import com.oo.bluetransforlib.base.server.IMicroServer
import java.util.*
import kotlin.collections.HashMap


/**
 *
 * server  作为总体的 sever
 *
 *
 *
 * */
class BluetoothServer(val activity: Activity) {
    companion object{
        const val uuid = "f47a4185-8023-4715-aa2e-f527bebb8e52"
    }


    val adapter = BluetoothAdapter.getDefaultAdapter()


    val listenerThread = HandlerThread("listener").apply { start() }
    val listenerHandler = Handler(listenerThread.looper)

    val dispatchThread = HandlerThread("dispatch").apply { start() }
    val dispatchHandler = Handler(dispatchThread.looper)


    val connectedClient = HashMap<String, IConnection>()


    val bluetoothReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                BluetoothAdapter.ACTION_STATE_CHANGED -> {
                    //本机蓝牙状态改变
                    intent.extras
                }
                BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED -> {
                    //本机蓝牙连接状态改变
                }
                BluetoothDevice.ACTION_FOUND -> {
                    //发现设备
                }
                BluetoothDevice.ACTION_BOND_STATE_CHANGED -> {
                    //设备配对状态改变
                }
            }
        }
    }

    val receiverInflater = IntentFilter().apply {
        addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
        addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED)

        addAction(BluetoothDevice.ACTION_FOUND)
        addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED)

    }

    val lifecycleCallbacks = object : Application.ActivityLifecycleCallbacks {
        override fun onActivityPaused(activity: Activity) {
        }

        override fun onActivityStarted(activity: Activity) {
        }

        override fun onActivityDestroyed(activity: Activity) {
            activity.unregisterReceiver(bluetoothReceiver)
        }

        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
        }

        override fun onActivityStopped(activity: Activity) {
        }

        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
            activity.registerReceiver(bluetoothReceiver, receiverInflater)
        }

        override fun onActivityResumed(activity: Activity) {
        }

    }

    var serverSocket: BluetoothServerSocket? = null


    init {
        activity.registerActivityLifecycleCallbacks(lifecycleCallbacks)
    }

    fun registServices(services: HashMap<String, IMicroServer>) {
        BaseServer.registServices(services)
    }

    fun start() {
        if (adapter.isEnabled.not()) {
            stateCallback?.serverShutdown("unable")
            return
        }

        serverSocket = adapter.listenUsingRfcommWithServiceRecord("car", UUID.fromString(uuid))
        listenClientAccess()
        stateCallback?.serverStart()
    }

    fun openAdapter(a: Activity) {
        //首先检查权限
        if ((ActivityCompat.checkSelfPermission(a, Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.checkSelfPermission(a, Manifest.permission.BLUETOOTH_ADMIN) == PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.checkSelfPermission(a, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(a, arrayOf(
                Manifest.permission.BLUETOOTH,Manifest.permission.BLUETOOTH_ADMIN,Manifest.permission.ACCESS_FINE_LOCATION
            ),1)
            return
        }
        a.startActivityForResult(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE),1)
    }

    fun shutDown() {
        //移除接入监听
        listenerHandler.removeCallbacks(listenerTask)
        stateCallback?.serverShutdown("reason")
    }

    val listenerTask = Runnable {
        while (true) {
            val connectedSocket = serverSocket?.accept()
            if (connectedSocket != null && connectedSocket.remoteDevice != null) {
                connecting(connectedSocket)//创建必要的输入输出流
            }
        }
    }

    fun listenClientAccess() {
        //开启 接入监听
        listenerHandler.post(listenerTask)
    }

    private fun connecting(socket: BluetoothSocket) {
        dispatchHandler.post {
            connectedClient[socket.remoteDevice.address] = Connection.newInstance(socket)
        }
    }

    private var stateCallback: OnServerStateCallback? = null

    fun setStateCallback(cl: OnServerStateCallback) {
        stateCallback = cl
    }

    interface OnServerStateCallback {
        fun serverStart()
        fun serverShutdown(reason: String)
    }
}