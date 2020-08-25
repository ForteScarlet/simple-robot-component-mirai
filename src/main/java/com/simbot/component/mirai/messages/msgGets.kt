/*
 * Copyright (c) 2020. ForteScarlet All rights reserved.
 * Project  component-mirai (Codes other than Mirai)
 * File     msgGets.kt (Codes other than Mirai)
 *
 * You can contact the author through the following channels:
 * github https://github.com/ForteScarlet
 * gitee  https://gitee.com/ForteScarlet
 * email  ForteScarlet@163.com
 * QQ     1149159218
 *
 * The Mirai code is copyrighted by mamoe-mirai
 * you can see mirai at https://github.com/mamoe/mirai
 *
 *
 */

package com.simbot.component.mirai.messages

import com.forte.qqrobot.beans.messages.msgget.EventGet
import com.forte.qqrobot.beans.messages.msgget.MsgGet
import com.simbot.component.mirai.CacheMaps
import com.simbot.component.mirai.utils.MiraiCodeFormatUtils
import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.User
import net.mamoe.mirai.event.events.BotEvent
import net.mamoe.mirai.message.MessageEvent
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.source


//region 基础MsgGet抽象类
/**
 * 基础MsgGet父类
 */
abstract class MiraiBaseMsgGet<out E: BotEvent>(val event: E): MsgGet {

    open val onTime: Long = System.currentTimeMillis()

    /** bot的qq号的字符串 */
    private val botCodeString: String = event.bot.id.toString()

    /** event消息 */
    abstract var eventMsg: String?

    // bot id
    open var botId: String = botCodeString

    /** 获取原本的数据 originalData  */
    override fun getOriginalData(): String = toString()

    /** toString是必须的 */
    abstract override fun toString(): String

    /**
     * 此消息获取的时候，代表的是哪个账号获取到的消息。
     * @return 接收到此消息的账号。
     */
    override fun getThisCode(): String = botId

    /**
     * bot id
     * 可重置的botId. 但是一般不推荐其重置
     * */
//    @Deprecated("can not reset bot code")
    override fun setThisCode(code: String) {
        botId = code
    }

    /**
     * 重新设置消息
     * @param newMsg msg
     */
    override fun setMsg(newMsg: String?) {
        eventMsg = newMsg
    }
    /**
     * 一般来讲，监听到的消息大部分都会有个“消息内容”。定义此方法获取消息内容。
     * 如果不存在，则为null。（旧版本推荐为空字符串，现在不了。我变卦了）
     */
    override fun getMsg(): String? = eventMsg


    /** 获取消息的字体  */
    override fun getFont(): String? = null

    /** 获取到的时间, 代表某一时间的秒值。一般情况下是秒值。如果类型不对请自行转化  */
    override fun getTime(): Long = onTime

}

/**
 * mirai 消息类型事件父类，可获取contact对象
 *
 * 消息类型的msgGet
 *
 * 且实现[MsgGet]接口
 */
abstract class MiraiMessageGet<out ME: MessageEvent>(event: ME, private val cacheMaps: CacheMaps): MiraiBaseMsgGet<ME>(event) {
//    protected open val messageEvent = event

    /**
     * 获取contact
     * > 联系对象, 即可以与 [Bot] 互动的对象. 包含 [用户][User], 和 [群][Group].
     * @see [Contact]
     */
    val contact: Contact get() = event.sender
    val message: MessageChain get() = event.message

    /** 消息id */
    private val msgId: String = cacheMaps.recallCache.cache(message.source)

    /** 消息正文，目前会将mirai码替换为CQ码 */
    override var eventMsg: String? = MiraiCodeFormatUtils.mi2cq(message, cacheMaps)

//    // bot id
//    override var botId: String = event.bot.id.toString()


    /** 获取ID, 一般可用于撤回  */
    override fun getId(): String = msgId


    override fun toString(): String = "SimbotMiraiMsgEvent(${message.contentToString()})"
}

/**
 *  mirai 的事件EventGet的父类
 *  实现[EventGet]接口
 */
abstract class MiraiEventGet<out EE: BotEvent>(event: EE): MiraiBaseMsgGet<EE>(event), EventGet {
    /** 事件消息正文 */
    override var eventMsg: String? = null
    protected val eventId = "$event#$onTime"
    override fun getId(): String = eventId
//    override var botId: String = event.bot.id.toString()



    /**
     * 重新设置消息
     * @param newMsg msg
     */
    override fun setMsg(newMsg: String?) {
        eventMsg = newMsg
    }
    /**
     * 一般来讲，监听到的消息大部分都会有个“消息内容”。定义此方法获取消息内容。
     * 如果不存在，则为null。（旧版本推荐为空字符串，现在不了。我变卦了）
     */
    override fun getMsg(): String? = eventMsg


    /** 获取消息的字体  */
    override fun getFont(): String? = null

    override fun toString(): String = "SimbotMiraiEvent($event)"
}
//endregion

















