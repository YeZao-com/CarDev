package com.oo.bluetransforlib.base.io


/**
 *
 * 一个 蓝牙 链接
 *
 * */
interface IConnection {
    fun connect()
    fun close()
    fun write()
    fun read()
}