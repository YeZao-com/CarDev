package com.oo.bluetransforlib.base.io

class CommandCodec {
    fun codec(cmdLine:String):Request{
        return Request.fromString(cmdLine)
    }
    fun decode(resp:Response):String{
        return resp.toString()
    }
}