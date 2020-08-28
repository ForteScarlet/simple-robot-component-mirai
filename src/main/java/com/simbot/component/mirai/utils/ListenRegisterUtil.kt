package com.simbot.component.mirai.utils

import com.forte.qqrobot.beans.messages.msgget.MsgGet
import com.forte.qqrobot.beans.messages.types.MsgGetTypes
import com.forte.qqrobot.factory.MsgGetTypeFactory
import com.forte.qqrobot.log.QQLog
import kotlin.reflect.KClass


private data class CauseFactory(val cause: Throwable?, val msgGetTypeFactory: MsgGetTypeFactory?)

/**
 * 用于注册额外的注册监听
 * Created by lcy on 2020/8/28.
 * @author lcy
 */
object ListenRegisterUtil {

    /**
     * 如果[msgGetTypeFactory]实例化错误则会存在此值
     */
    val cause: Throwable?

    /**
     * 尝试获取[MsgGetTypeFactory]实例。
     */
    private val _msgGetTypeFactory: MsgGetTypeFactory?
    init {
        val (c, f) = try {
            CauseFactory(null, MsgGetTypeFactory.getInstance())
        }catch (e: Throwable){
            CauseFactory(e, null)
        }
        cause = c
        _msgGetTypeFactory = f
    }
    private val msgGetTypeFactory: MsgGetTypeFactory
        get() = _msgGetTypeFactory ?: throw IllegalStateException("MsgGetTypeFactory was not instantiated successfully", cause)


    val usable: Boolean get() = cause == null

    /**
     * 注册一个额外的[MsgGetTypes]并捕获异常
     */
    fun <MG: MsgGet> registerListen(typeName: String, typeClass: Class<MG>): MsgGetTypes? {
        // 头像变更事件
        return try {
            QQLog.debug("mirai.event.register", typeName)
            msgGetTypeFactory.register(typeName, typeClass)
        }catch (e: Throwable) {
            // 捕获一切异常
            QQLog.warning("mirai.event.register.failed", typeName, e.localizedMessage)
            QQLog.debug("mirai.event.register.failed", e, typeName, e.localizedMessage)
            null
        }
    }

    fun <MG: MsgGet> registerListen(typeName: String, typeClass: KClass<MG>): MsgGetTypes? {
        return registerListen(typeName, typeClass.java)
    }

}