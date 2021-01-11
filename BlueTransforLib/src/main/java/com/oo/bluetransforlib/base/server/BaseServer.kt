package com.oo.bluetransforlib.base.server

import android.text.TextUtils
import com.oo.bluetransforlib.base.io.Request
import com.oo.bluetransforlib.base.io.Response

/**
 * 中心服务
 * 各个功能有各个子服务模块实现
 *
 * */
object  BaseServer {

    val micServices = HashMap<String, IMicroServer>()
    init {
        //加载所有的服务 并注册到服务表单
        parseServices()
    }

    fun parseServices(){
        micServices[HelloMicroServer.javaClass.simpleName]=HelloMicroServer()
    }
    fun registServices(services:HashMap<String,IMicroServer>){
        for ((k,s) in services) {
            micServices[k]=s
        }
    }

    fun findCmdServer(path:String):IMicroServer?{
        for ((serviceName, IMicroServer) in micServices) {
            if (TextUtils.equals(path,serviceName)) {
                return IMicroServer
            }
        }
        return null
    }
    fun dispatch(req:Request):Response{
        val microServer = findCmdServer(req.cmdName)
        return microServer?.process(req)?: Response.noService(reqId = req.reqId)
    }


}