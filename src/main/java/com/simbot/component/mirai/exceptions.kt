package com.simbot.component.mirai

import com.forte.qqrobot.exception.RobotRuntimeException

open class CQCodeParseHandlerException: RobotRuntimeException {
    constructor() : super()
    constructor(message: String?, vararg format: Any?) : super(message, *format)
    constructor(message: String?) : super(message)
    constructor(message: String?, cause: Throwable?, vararg format: Any?) : super(message, cause, *format)
    constructor(message: String?, cause: Throwable?) : super(message, cause)
    constructor(cause: Throwable?) : super(cause)
    constructor(message: String?, cause: Throwable?, enableSuppression: Boolean, writableStackTrace: Boolean) : super(message, cause, enableSuppression, writableStackTrace)
    constructor(message: String?, cause: Throwable?, enableSuppression: Boolean, writableStackTrace: Boolean, vararg format: Any?) : super(message, cause, enableSuppression, writableStackTrace, *format)
    constructor(pointless: Int, message: String?) : super(pointless, message)
    constructor(pointless: Int, message: String?, cause: Throwable?) : super(pointless, message, cause)
    constructor(pointless: Int, message: String?, cause: Throwable?, enableSuppression: Boolean, writableStackTrace: Boolean) : super(pointless, message, cause, enableSuppression, writableStackTrace)
}

/**
 * KQCode 转化注册异常
 */
open class CQCodeParseHandlerRegisterException: CQCodeParseHandlerException {
    constructor() : super()
    constructor(message: String?, vararg format: Any?) : super(message, *format)
    constructor(message: String?) : super(message)
    constructor(message: String?, cause: Throwable?, vararg format: Any?) : super(message, cause, *format)
    constructor(message: String?, cause: Throwable?) : super(message, cause)
    constructor(cause: Throwable?) : super(cause)
    constructor(message: String?, cause: Throwable?, enableSuppression: Boolean, writableStackTrace: Boolean) : super(message, cause, enableSuppression, writableStackTrace)
    constructor(message: String?, cause: Throwable?, enableSuppression: Boolean, writableStackTrace: Boolean, vararg format: Any?) : super(message, cause, enableSuppression, writableStackTrace, *format)
    constructor(pointless: Int, message: String?) : super(pointless, message)
    constructor(pointless: Int, message: String?, cause: Throwable?) : super(pointless, message, cause)
    constructor(pointless: Int, message: String?, cause: Throwable?, enableSuppression: Boolean, writableStackTrace: Boolean) : super(pointless, message, cause, enableSuppression, writableStackTrace)
}

/**
 * CQCode Param null pointer
 */
open class CQCodeParamNullPointerException(type: String, vararg paramNames: String) : NullPointerException(getInfo(type, *paramNames)) {
    companion object {
        private fun getInfo(type: String, vararg paramNames: String): String {
            val msg = "cq code [$type] can not found param "
            val builder = StringBuilder(msg)
            paramNames.forEachIndexed {index,paramName ->
                builder.append("[").append(paramName).append("]")
                if(index < paramNames.lastIndex){
                    builder.append(" or ")
                }
            }
            return builder.toString()
        }
    }
}
