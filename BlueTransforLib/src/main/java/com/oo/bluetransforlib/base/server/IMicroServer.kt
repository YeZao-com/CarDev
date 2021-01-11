package com.oo.bluetransforlib.base.server

import com.oo.bluetransforlib.base.io.Request
import com.oo.bluetransforlib.base.io.Response


/**
 *
 *
 * */
interface IMicroServer {
    fun process(req:Request):Response
}