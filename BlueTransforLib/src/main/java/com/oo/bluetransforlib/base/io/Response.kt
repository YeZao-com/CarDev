package com.oo.bluetransforlib.base.io

data class Response(
    val reqId:String,
    val code:Int,
    val message:String?
){
    companion object{
        fun noService(reqId: String):Response{
            return Response(reqId,100,"noService")
        }



    }
}