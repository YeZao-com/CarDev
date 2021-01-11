package com.oo.cardemo

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.text.TextUtils
import android.util.Log
import android.view.View
import com.oo.bluetransforlib.BlueClient
import java.io.BufferedWriter
import java.io.OutputStreamWriter
import java.util.*
import kotlin.collections.HashMap

class BlueDemoActivity : AppCompatActivity() {

    val TAG= "BlueDemoActivity"



    private val intentFilter = IntentFilter().apply {
        addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
        addAction(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        addAction(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE)
        addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
        addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED)

        addAction(BluetoothDevice.ACTION_FOUND)
    }


    private val workerThread = HandlerThread("work").apply {
        start()
    }

    private val readThread = HandlerThread("read").apply { start() }
    private val writeThread = HandlerThread("write").apply { start() }

    private val workHandler = Handler(workerThread.looper)
    private val readHandler = Handler(readThread.looper)
    private val writeHandler = Handler(writeThread.looper)

    val uuid = UUID.fromString("f47a4185-8023-4715-aa2e-f527bebb8e52")

    private val devices = HashMap<String,BluetoothDevice>()


    private var  socket : BluetoothSocket?=null

    val bluetoothBroadCastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            Log.i(BlueClient.TAG, "onReceive: ${intent?.action}")
            when (intent?.action) {
                BluetoothDevice.ACTION_FOUND -> {//发现设备
                    val foundedDevice:BluetoothDevice=intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE) as BluetoothDevice
                    Log.i(BlueClient.TAG, "name : ${foundedDevice.name} address:  ${foundedDevice.address}")
                    //交给worker 去处理
                    workHandler.post {
                        if (!TextUtils.isEmpty(foundedDevice.name)) {
                            synchronized(devices){

                                devices.put(foundedDevice.address,foundedDevice)
                            }
                        }
                    }
                }
                BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED -> {// 连接状态改变
                    //
                }
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {// 完成发现
                    workHandler.post {
                        synchronized(devices){
                            for ((mac,device) in devices) {
                                Log.i(TAG, "onReceive: ${mac} : ${device.name} ")
                                if (TextUtils.equals("OO",device.name)) {
                                    readHandler.post {
                                        //connect
                                        socket=connect(device)?.apply {
                                            if (device.bondState==BluetoothDevice.BOND_BONDED) {
                                                workHandler.post {
                                                    val device =
                                                        intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)

                                                    socket?.connect()
                                                    val bufferedWriter =
                                                        BufferedWriter(OutputStreamWriter(socket?.outputStream))
                                                    var count = 10
                                                    while (socket?.isConnected==true&&count-->0){
                                                        Log.i(TAG, "while  write : ")
                                                        bufferedWriter.write("12313213213123\n")
                                                        bufferedWriter.flush()
                                                        Thread.sleep(500)
                                                    }
//                                                    writer.close()
                                                    Log.i(TAG, "close write")
                                                }
                                            }else{
                                                device.createBond()
                                            }
                                        }
                                    }
                                    break
                                }
                            }
                        }
                    }
                }
                BluetoothDevice.ACTION_BOND_STATE_CHANGED->{
                    workHandler.post {
                        val device =
                            intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)

                        socket?.connect()

                        val writer = OutputStreamWriter(socket?.outputStream)
                        var count = 10
                        while (socket?.isConnected==true&&count>0){
                            Log.i(TAG, "while  write : ")
                            writer.write("12313213213123")
                            writer.flush()
                            Thread.sleep(500)
                        }
                        Log.i(TAG, "close write")
                    }
                }
                BluetoothAdapter.ACTION_DISCOVERY_STARTED -> {//开始 发现
                }
                BluetoothAdapter.ACTION_STATE_CHANGED -> {// 蓝牙状态改变

                }
            }
        }
    }


    fun connect(device: BluetoothDevice):BluetoothSocket{
        val createRfcommSocketToServiceRecord =
            device.createRfcommSocketToServiceRecord(uuid)
        return createRfcommSocketToServiceRecord
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_blue_demo)
        BlueClient.init(this)
        registerReceiver(bluetoothBroadCastReceiver,intentFilter)
    }

    override fun onDestroy() {
        unregisterReceiver(bluetoothBroadCastReceiver)
        super.onDestroy()
    }

    fun discover(v: View?){
        BlueClient.scanDevice(5000)
    }
    fun connect(v:View?){

    }



}