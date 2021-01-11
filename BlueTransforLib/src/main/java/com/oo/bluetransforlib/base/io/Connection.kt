package com.oo.bluetransforlib.base.io

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.os.Handler
import android.os.HandlerThread
import com.oo.bluetransforlib.base.server.BaseServer
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter

class Connection(val socket: BluetoothSocket):IConnection {

    companion object{
        fun newInstance(socket:BluetoothSocket): IConnection{
            return Connection(socket)
        }
    }
    private val clientDevice :BluetoothDevice
    private val reader :BufferedReader
    private val writer :BufferedWriter

    private val receiveThread : HandlerThread
    private val receiveHandler :Handler

    private val sendThread : HandlerThread
    private val sendHandler :Handler

    private val codec:CommandCodec

    init {
        clientDevice = socket.remoteDevice
        reader = BufferedReader(InputStreamReader(socket.inputStream))
        writer = BufferedWriter(OutputStreamWriter(socket.outputStream))

        codec = CommandCodec()

        receiveThread = HandlerThread(clientDevice.name).apply { start() }
        receiveHandler = Handler.createAsync(receiveThread.looper)

        sendThread = HandlerThread(clientDevice.name).apply { start() }
        sendHandler = Handler.createAsync(sendThread.looper)

        receiveHandler.post {
            while (socket.isConnected){
                //输入处理
                val request = codec.codec(reader.readLine())
                //交给输出线程 去处理
                sendHandler.post {
                    val response = BaseServer.dispatch(request)
                    val responseLine = codec.decode(response)
                    writer.write(responseLine)
                    writer.newLine()
                    writer.flush()
                }
            }

            reader.close()
            writer.close()
            receiveThread.quit()
        }
    }










    override fun connect() {
        TODO("Not yet implemented")
    }

    override fun close() {
        TODO("Not yet implemented")
    }

    override fun write() {
        TODO("Not yet implemented")
    }

    override fun read() {
        TODO("Not yet implemented")
    }
}