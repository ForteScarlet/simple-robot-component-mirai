/*
 *
 *  * Copyright (c) 2020. ForteScarlet All rights reserved.
 *  * Project  component-mirai
 *  * File     botEvent.kt
 *  *
 *  * You can contact the author through the following channels:
 *  * github https://github.com/ForteScarlet
 *  * gitee  https://gitee.com/ForteScarlet
 *  * email  ForteScarlet@163.com
 *  * QQ     1149159218
 *  *
 *  * The Mirai code is copyrighted by mamoe-mirai
 *  * you can see mirai at https://github.com/mamoe/mirai
 *  *
 *  *
 *
 */

package com.simbot.component.mirai.messages

import com.forte.qqrobot.beans.messages.NicknameAble
import com.forte.qqrobot.beans.messages.QQCodeAble
import com.forte.qqrobot.beans.messages.msgget.MsgGet
import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.event.events.*

/*
    此文件中定义一些只有mirai中才有的特殊事件接口。
 */

/**
 * mirai中特有的bot相关事件
 */
interface MiraiSpecialBotMsgGet<out T: BotEvent>: MsgGet, QQCodeAble, NicknameAble {
    /** 可以得到mirai中的原生Bot对象 */
    val bot: Bot
    /** 可以得到mirai中当前事件的原生对象 */
    val miraiEvent: T

    @JvmDefault override fun getNickname(): String = bot.nick
    @JvmDefault override fun getOriginalData(): String = miraiEvent.toString()
    @JvmDefault override fun getThisCode(): String = bot.id.toString()
    @JvmDefault override fun getQQCode(): String = thisCode
    @JvmDefault override fun getCodeNumber(): Long = bot.id
    @JvmDefault override fun getQQHeadUrl(): String = "http://q1.qlogo.cn/g?b=qq&nk=$thisCode&s=640"
}

/**
 * 针对于[MiraiSpecialBotMsgGet]的抽象类
 */
abstract class BaseMiraiSpecialBotMsgGet<out T: BotEvent>(override val miraiEvent: T): MiraiSpecialBotMsgGet<T> {
    private val eventTime = System.currentTimeMillis()

    override val bot: Bot
        get() = miraiEvent.bot

    private var eventMsg: String? = null

    override fun getMsg(): String? = eventMsg
    override fun setMsg(newMsg: String?) {
        eventMsg = newMsg
    }

    @Deprecated("can not reset bot code in MiraiBotOfflineEvent")
    override fun setThisCode(code: String?) { }

    /** 事件id。默认情况下此id不保证唯一性  */
    override fun getId(): String = "$bot.$miraiEvent"


    /** 获取消息的字体  */
    override fun getFont(): String? = null

    /** 获取到的时间, 代表某一时间的秒值。一般情况下是秒值。如果类型不对请自行转化  */
    override fun getTime(): Long = eventTime
}


/**
 * [Bot] 登录完成, 好友列表, 群组列表初始化完成
 * 不过一般来讲，登录与注册监听的分离的，所以此事件几乎不可能被触发
 * 因此暂时不做整合
 * @see BotOnlineEvent
 */
interface BotOnline: MiraiSpecialBotMsgGet<BotOnlineEvent>


/**
 * [Bot] 离线.
 * @see BotOfflineEvent
 */
interface BotOffline: MiraiSpecialBotMsgGet<BotOfflineEvent> {
    /** 离线类型，分为主动离线与被动离线。 */
    val offlineType: BotOfflineType
    /** 如果为主动离线，此处则**可能**有值。也可能是null。 */
    val cause: Throwable?
    /** 如果为被动离线，则此处不为null。否则为null。 */
    val forceMessage: ForceMessage?
}

/**
 * [Bot] 离线类型
 */
enum class BotOfflineType {
    /**
     * 主动下线。
     * @see BotOfflineEvent.Active
     */
    INITIATIVE,

    /**
     * 被动下线。
     * @see BotOfflineEvent.Force
     */
    PASSIVE,


    /**
     * 因网络原因掉线
     * @see BotOfflineEvent.Dropped
     */
    DROPPED,

    /**
     * 其他可能的未知类型和不稳定/实验性类型
     * @see BotOfflineEvent 及其子类
     */
    OTHER
}

/**
 * 当离线类型为[BotOfflineEvent.Force] ( [BotOfflineType.PASSIVE] ) 被动的时候,
 * 此类封装[BotOfflineEvent.Force.title] [BotOfflineEvent.Force.message]
 */
data class ForceMessage(val title: String, val message: String)



/**
 * [Bot] 主动或被动重新登录. 在此事件广播前就已经登录完毕.
 * [net.mamoe.mirai.event.events.BotReloginEvent]
 */
interface BotRelogin: MiraiSpecialBotMsgGet<BotReloginEvent> {
    /**
     * @see BotReloginEvent.cause
     */
    val cause: Throwable?
}











/**
 * [Bot] 头像被修改（通过其他客户端修改了头像）. 在此事件广播前就已经修改完毕.
 * [net.mamoe.mirai.event.events.BotAvatarChangedEvent]
 */
interface BotAvatarChanged: MiraiSpecialBotMsgGet<BotAvatarChangedEvent>


/**
 * 图片上传前. 可以阻止上传.
 *
 * 此事件总是在 [ImageUploadEvent] 之前广播.
 * 若此事件被取消, [ImageUploadEvent] 不会广播.
 *
 * @see Contact.uploadImage 上传图片. 为广播这个事件的唯一途径
 * @see net.mamoe.mirai.event.events.BeforeImageUploadEvent
 */
interface BeforeImageUpload: MiraiSpecialBotMsgGet<BeforeImageUploadEvent>


/**
 * @see net.mamoe.mirai.event.events.ImageUploadEvent
 */
interface ImageUpload: MiraiSpecialBotMsgGet<ImageUploadEvent>


/**
 * [net.mamoe.mirai.event.events.MessagePreSendEvent]
 */
interface MessagePreSend: MiraiSpecialBotMsgGet<MessagePreSendEvent>

/**
 * [net.mamoe.mirai.event.events.MessagePostSendEvent]
 */
interface MessagePostSend: MiraiSpecialBotMsgGet<MessagePostSendEvent<Contact>>


/*

/**
 * 群 "匿名聊天" 功能状态改变. 此事件广播前修改就已经完成.
 */
data class GroupAllowAnonymousChatEvent internal constructor(


/**
 * 群 "坦白说" 功能状态改变. 此事件广播前修改就已经完成.
 */
data class GroupAllowConfessTalkEvent internal constructor(

/**
 * 群 "允许群员邀请好友加群" 功能状态改变. 此事件广播前修改就已经完成.
 */
data class GroupAllowMemberInviteEvent internal constructor(
 */