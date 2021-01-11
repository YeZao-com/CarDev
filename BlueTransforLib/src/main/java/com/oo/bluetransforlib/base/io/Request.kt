package com.oo.bluetransforlib.base.io

import java.util.*

/**
 * 封装 蓝牙功能请求
 * */
class Request(val cmdName:String) {

    val reqId:String

    init {
        reqId = UUID.randomUUID().toString()
    }

    companion object{
        fun fromString(str:String):Request{

            return Request("")
        }
    }


    override fun toString(): String {
        return super.toString()
    }
}