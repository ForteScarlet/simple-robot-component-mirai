/*
 *
 * Copyright (c) 2020. ForteScarlet All rights reserved.
 * Project  component-mirai
 * File     RequestEvents.kt
 *
 * You can contact the author through the following channels:
 *  github https://github.com/ForteScarlet
 *  gitee  https://gitee.com/ForteScarlet
 *  email  ForteScarlet@163.com
 *  QQ     1149159218
 *  The Mirai code is copyrighted by mamoe-mirai
 *  you can see mirai at https://github.com/mamoe/mirai
 *
 *
 */

package com.simbot.component.mirai.messages

import com.forte.qqrobot.beans.messages.msgget.FriendAddRequest
import com.forte.qqrobot.beans.messages.msgget.GroupAddRequest
import com.forte.qqrobot.beans.messages.types.GroupAddRequestType
import com.simbot.component.mirai.CacheMaps
import net.mamoe.mirai.event.events.BotInvitedJoinGroupRequestEvent
import net.mamoe.mirai.event.events.MemberJoinRequestEvent
import net.mamoe.mirai.event.events.NewFriendRequestEvent


//region 申请/邀请相关事件
//region bot被邀请入群事件
/**
 * bot被邀请入群事件
 */
open class MiraiBotInvitedJoinGroupRequestEvent(eventEvent: BotInvitedJoinGroupRequestEvent, cacheMaps: CacheMaps): MiraiEventGet<BotInvitedJoinGroupRequestEvent>(eventEvent), GroupAddRequest {
    private val invitorId: String = eventEvent.invitorId.toString()
    private val groupId: String = eventEvent.groupId.toString()
    private val flagId: String = cacheMaps.requestCache.cache(event)

    /** 获取QQ号  */
    override fun getQQ(): String = invitorId

    /** 获取群号  */
    override fun getGroup(): String = groupId
    override fun getCodeNumber(): Long = event.invitorId
    override fun getGroupCodeNumber(): Long = event.groupId
    /** 获取ID, 与flag一致  */
    override fun getId(): String = flag

    /** 获取标识  */
    override fun getFlag(): String = flagId

    /** 加群类型，此处为被邀请入群  */
    override fun getRequestType(): GroupAddRequestType = GroupAddRequestType.INVITE

    override fun getNickname(): String {
        return event.invitor.nick
    }

    override fun invitorCode(): String {
        return event.invitorId.toString()
    }

    override fun invitorNickname(): String {
        return event.invitorNick
    }

    override fun invitorRemark(): String? = null

    override fun inviteeCode(): String {
        return event.bot.id.toString()
    }

    override fun inviteeNickname(): String {
        return event.bot.nick
    }
}
//endregion

//region 加群申请
/**
 * 加群申请
 */
open class MiraiMemberJoinRequestEvent(eventEvent: MemberJoinRequestEvent, cacheMaps: CacheMaps): MiraiEventGet<MemberJoinRequestEvent>(eventEvent), GroupAddRequest {
    private val fromId: String = event.fromId.toString()
    private val groupId: String = event.groupId.toString()
    private val flagId: String = cacheMaps.requestCache.cache(event)

    private var _eventMsg: String? = event.message
    override var eventMsg: String?
        get() = _eventMsg
        set(value) {
            _eventMsg = value
        }

    /** 获取QQ号  */
    override fun getQQ(): String = fromId

    /** 获取群号  */
    override fun getGroup(): String = groupId
    override fun getQQCodeNumber(): Long = event.fromId
    override fun getGroupCodeNumber(): Long = event.groupId
    /** 获取ID, 与flag一致  */
    override fun getId(): String = flag

    /** 获取标识  */
    override fun getFlag(): String = flagId

    /** 加群类型，此处为申请入群  */
    override fun getRequestType(): GroupAddRequestType = GroupAddRequestType.ADD

    override fun getNickname(): String {
        return event.fromNick
    }

    override fun getRemark(): String? = null

    override fun invitorCode(): String? = null

    override fun invitorNickname(): String? = null

    override fun invitorRemark(): String? = null

    override fun inviteeCode(): String {
        return event.fromId.toString()
    }

    override fun inviteeNickname(): String {
        return event.fromNick
    }
}
//endregion


//region 好友添加申请
/**
 * 好友添加申请
 */
open class MiraiNewFriendRequestEvent(eventEvent: NewFriendRequestEvent, cacheMaps: CacheMaps): MiraiEventGet<NewFriendRequestEvent>(eventEvent), FriendAddRequest {
    private val fromId: String = event.fromId.toString()
    private val requestFlag: String = cacheMaps.requestCache.cache(eventEvent)

    private var _eventMsg: String? = event.message
    override var eventMsg: String?
        get() = _eventMsg
        set(value) {
            _eventMsg = value
        }

    /** 请求人QQ  */
    override fun getQQ(): String = fromId
    override fun getCodeNumber(): Long = event.fromId
    /** 获取ID, 与flag相同 */
    override fun getId(): String = flag

    /** 获取标识  */
    override fun getFlag(): String = requestFlag

    override fun getNickname(): String {
        return event.fromNick
    }

}
//endregion
//endregion