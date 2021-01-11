package com.oo.cardemo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.oo.bluetransforlib.BluetoothClient
import com.oo.bluetransforlib.BluetoothServer
import com.oo.bluetransforlib.base.io.Request
import com.oo.bluetransforlib.base.server.HelloMicroServer

class BlueClientActivity : AppCompatActivity(), BluetoothClient.ClientStateCallback {


    lateinit var client :BluetoothClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_blue_client)


        client=BluetoothClient(this)
        client.setStateCallback(this)
    }


    fun hello(v:View?){
        client.request(Request(HelloMicroServer.CMD_GREETING))
    }
    fun connect(v:View?){
        client.connectToServer(BluetoothServer.uuid)
    }
    fun disconenct(v:View?){
        client.disconnectFromServer()
    }



    override fun connected() {

    }

    override fun disconnected() {
    }
}