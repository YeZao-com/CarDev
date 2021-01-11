package com.oo.bluetransforlib.base.server

import com.oo.bluetransforlib.base.io.Request
import com.oo.bluetransforlib.base.io.Response

class HelloMicroServer : IMicroServer {
    companion object {
        const val CMD_GREETING = "CMD_GREETING"
    }


    override fun process(req: Request): Response {
        when (req.cmdName) {
            CMD_GREETING -> {
                return Response(reqId = req.reqId, code = 200, message = "你好啊 这里是服务器")
            }
            else -> {
                return Response.noService(req.reqId)
            }
        }
    }
}