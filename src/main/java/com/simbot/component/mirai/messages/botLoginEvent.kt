/*
 *
 *  * Copyright (c) 2020. ForteScarlet All rights reserved.
 *  * Project  component-mirai
 *  * File     botLoginEvent.kt
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

import net.mamoe.mirai.Bot
import net.mamoe.mirai.event.events.BotReloginEvent
import net.mamoe.mirai.event.events.BotOfflineEvent as OffEvent

/*
    bot账号状态相关事件
 */

///**
// * [Bot] 登录完成, 好友列表, 群组列表初始化完成
// * 不过一般来讲，登录与注册监听的分离的，所以此事件几乎不可能被触发
// * @see BotOnline
// */
//open class MiraiBotOnline: BotOnline {
//
//}


/**
 * [OffEvent]转化为[BotOfflineType]
 */
private val OffEvent.offlineType: BotOfflineType get() = if(this is OffEvent.Active) BotOfflineType.INITIATIVE else BotOfflineType.PASSIVE


/**
 * [Bot] 离线.
 * @see BotOffline
 * @see MiraiEvents.botOfflineEvent
 */
class MiraiBotOfflineEvent(event: OffEvent) : BaseMiraiSpecialBotMsgGet<OffEvent>(event), BotOffline {

    /** 离线类型，分为主动离线与被动离线。 */
    override val offlineType: BotOfflineType = event.offlineType

    /** 如果为主动离线，此处则**可能**有值。也可能是null。 */
    override val cause: Throwable? = if(event is OffEvent.Active) event.cause else null

    /** 如果为被动离线，则此处不为null。否则为null。 */
    override val forceMessage: ForceMessage? = if(event is OffEvent.Force) event.let {
        ForceMessage(it.title, it.message)
    } else null

}


/**
 * [Bot] 主动或被动重新登录. 在此事件广播前就已经登录完毕.
 * @see BotRelogin
 * @see MiraiEvents.botReloginEvent
 */
class MiraiBotReloginEvent(event: BotReloginEvent):BaseMiraiSpecialBotMsgGet<BotReloginEvent>(event), BotRelogin {
    /**
     * @see BotReloginEvent.cause
     */
    override val cause: Throwable? = event.cause

}





