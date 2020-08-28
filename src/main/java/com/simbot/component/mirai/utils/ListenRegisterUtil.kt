package com.simbot.component.mirai.utils

import com.forte.qqrobot.beans.messages.msgget.MsgGet
import com.forte.qqrobot.beans.messages.types.MsgGetTypes
import com.forte.qqrobot.factory.MsgGetTypeFactory
import com.forte.qqrobot.log.QQLog
import com.simbot.component.mirai.messages.FriendAvatarChanged
import com.simbot.component.mirai.messages.MiraiEvents
import kotlin.reflect.KClass

/**
 * 用于注册额外的注册监听
 * Created by lcy on 2020/8/28.
 * @author lcy
 */
object ListenRegisterUtil {

    /**
     * 注册一个额外的[MsgGetTypes]并捕获异常
     */
    fun <MG: MsgGet> registerListen(typeName: String, typeClass: Class<MG>): MsgGetTypes? {
        // 头像变更事件
        return try {
            val msgGetTypeFactory = MsgGetTypeFactory.getInstance()
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