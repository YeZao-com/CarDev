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
import android.os.Bundle
import com.oo.bluetransforlib.base.io.Request
import com.oo.bluetransforlib.base.io.Response

class BluetoothClient(val activity:Activity) {

    private var connected =false

    val adapter = BluetoothAdapter.getDefaultAdapter()

    private val bluetoothReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when(intent?.action){
                BluetoothDevice.ACTION_FOUND->{

                }
            }
        }
    }
    private val receiverFilter = IntentFilter().apply {
        addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED)
        addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
        addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
    }

    private val activityLifecycleCallbacks = object :Application.ActivityLifecycleCallbacks{
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
            activity.registerReceiver(bluetoothReceiver,receiverFilter)
        }

        override fun onActivityResumed(activity: Activity) {
        }

    }
    init {
        activity.registerActivityLifecycleCallbacks(activityLifecycleCallbacks)
    }

    private val hostServer = ArrayList<BluetoothDevice>()

    fun scanServer(){
        adapter.startDiscovery()
    }

    fun connectToServer(name:String){

    }

    fun disconnectFromServer(){

    }

    fun request(req:Request):Response?{
        return null
    }
    fun requestAsync(req: Request,callback:RequestCallback){

    }

    interface RequestCallback{
        fun onResponse(resp:Response)
    }


    private var stateCallback:ClientStateCallback?=null
    fun setStateCallback(cl:ClientStateCallback){
        stateCallback = cl
    }
    interface ClientStateCallback{
        fun connected()
        fun disconnected()
    }
}