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
open class MiraiBotInvitedJoinGroupRequestEvent(eventEvent: BotInvitedJoinGroupRequestEvent, private val cacheMaps: CacheMaps): MiraiEventGet<BotInvitedJoinGroupRequestEvent>(eventEvent), GroupAddRequest {
    private val invitorId = eventEvent.invitorId.toString()
    private val groupId = eventEvent.groupId.toString()

    /** 获取QQ号  */
    override fun getQQ(): String = invitorId

    /** 获取群号  */
    override fun getGroup(): String = groupId
    override fun getCodeNumber(): Long = event.invitorId
    override fun getGroupCodeNumber(): Long = event.groupId
    /** 获取ID, 与flag一致  */
    override fun getId(): String = flag

    /** 获取标识  */
    override fun getFlag(): String = cacheMaps.requestCache.cache(event)

    /** 加群类型，此处为被邀请入群  */
    override fun getRequestType(): GroupAddRequestType = GroupAddRequestType.INVITE
}
//endregion

//region 加群申请
/**
 * 加群申请
 */
open class MiraiMemberJoinRequestEvent(eventEvent: MemberJoinRequestEvent, private val cacheMaps: CacheMaps): MiraiEventGet<MemberJoinRequestEvent>(eventEvent), GroupAddRequest {
    private val fromId = event.fromId.toString()
    private val groupId = event.groupId.toString()

    /** 获取QQ号  */
    override fun getQQ(): String = fromId

    /** 获取群号  */
    override fun getGroup(): String = groupId
    override fun getQQCodeNumber(): Long = event.fromId
    override fun getGroupCodeNumber(): Long = event.groupId
    /** 获取ID, 与flag一致  */
    override fun getId(): String = flag

    /** 获取标识  */
    override fun getFlag(): String = cacheMaps.requestCache.cache(event)

    /** 加群类型，此处为申请入群  */
    override fun getRequestType(): GroupAddRequestType = GroupAddRequestType.ADD
}
//endregion


//region 好友添加申请
/**
 * 好友添加申请
 */
open class MiraiNewFriendRequestEvent(eventEvent: NewFriendRequestEvent, private val cacheMaps: CacheMaps): MiraiEventGet<NewFriendRequestEvent>(eventEvent), FriendAddRequest {
    private val fromId = event.fromId.toString()
    private val requestFlag = cacheMaps.requestCache.cache(eventEvent)

    /** 请求人QQ  */
    override fun getQQ(): String = fromId
    override fun getCodeNumber(): Long = event.fromId
    /** 获取ID, 与flag相同 */
    override fun getId(): String = flag

    /** 获取标识  */
    override fun getFlag(): String = requestFlag
}
//endregion
//endregion