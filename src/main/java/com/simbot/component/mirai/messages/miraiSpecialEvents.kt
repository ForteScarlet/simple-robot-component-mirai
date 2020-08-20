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
import com.forte.qqrobot.beans.messages.msgget.EventGet
import com.forte.qqrobot.beans.messages.msgget.MsgGet
import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.event.events.*

/*
    此文件中定义一些只有mirai中才有的特殊事件接口。
 */

/**
 * mirai中那些特有事件
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
 * mirai的特有事件
 */
interface MiraiSpecialBotEventGet<out T: BotEvent>: MiraiSpecialBotMsgGet<T>, EventGet, QQCodeAble, NicknameAble


/**
 * [Bot] 登录完成, 好友列表, 群组列表初始化完成
 * @see BotOnlineEvent
 */
interface BotOnline: MiraiSpecialBotMsgGet<BotOnlineEvent>


/**
 * [Bot] 离线.
 * @see BotOfflineEvent
 */
interface BotOffline: MiraiSpecialBotMsgGet<BotOfflineEvent>


/**
 * [net.mamoe.mirai.event.events.BotReloginEvent]
 */
interface BotRelogin: MiraiSpecialBotMsgGet<BotReloginEvent>


/**
 * [net.mamoe.mirai.event.events.BotAvatarChangedEvent]
 */
interface BotAvatarChanged: MiraiSpecialBotMsgGet<BotAvatarChangedEvent>


/**
 * [net.mamoe.mirai.event.events.BeforeImageUploadEvent]
 */
interface BeforeImageUpload: MiraiSpecialBotMsgGet<BeforeImageUploadEvent>


/**
 * [net.mamoe.mirai.event.events.ImageUploadEvent]
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