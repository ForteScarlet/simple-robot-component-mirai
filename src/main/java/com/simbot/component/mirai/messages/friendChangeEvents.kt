/*
 *
 *  * Copyright (c) 2020. ForteScarlet All rights reserved.
 *  * Project  component-mirai
 *  * File     friendChangeEvents.kt
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

import com.forte.qqrobot.beans.messages.NickOrRemark
import com.forte.qqrobot.beans.messages.QQCodeAble
import com.forte.qqrobot.beans.messages.msgget.EventGet
import com.forte.qqrobot.beans.messages.msgget.FriendAdd
import com.forte.qqrobot.beans.messages.msgget.FriendDelete
import net.mamoe.mirai.contact.nameCardOrNick
import net.mamoe.mirai.event.events.FriendAddEvent
import net.mamoe.mirai.event.events.FriendAvatarChangedEvent
import net.mamoe.mirai.event.events.FriendDeleteEvent


//region 好友增加事件
/**
 * 好友增加事件
 */
open class MiraiFriendAddEvent(event: FriendAddEvent): MiraiEventGet<FriendAddEvent>(event), FriendAdd {
    private val fromId = event.friend.id.toString()

    /** 添加人的QQ  */
    override fun getQQ(): String = fromId
    override fun getCodeNumber(): Long = event.friend.id

}
//endregion

//region 好友删除事件


/**
 * 好友删除事件
 * 目前为Mirai组件提供的额外监听, 命名为FriendDelete
 */
open class MiraiFriendDeleteEvent(event: FriendDeleteEvent): MiraiEventGet<FriendDeleteEvent>(event), FriendDelete {
    private val friend = event.friend

    private val friendId = friend.id.toString()

    /**
     * 获取备注信息，例如群备注，或者好友备注。
     * @return 备注信息
     */
    override fun getRemark(): String = friend.nick

    /**
     * 可以获取昵称
     * @return nickname
     */
    override fun getNickname(): String = friend.nick

    /**
     * 获取QQ号信息。
     * 假如一个消息封装中存在多个QQ号信息，例如同时存在处理者与被处理者，一般情况下我们认为其返回值为被处理者。
     * @see .getCode
     */
    override fun getQQCode(): String = friendId

    override fun getCodeNumber(): Long = friend.id

    override fun getRemarkOrNickname(): String = friend.nameCardOrNick

    override fun getQQHeadUrl(): String = friend.avatarUrl
}
//endregion

//region 好友更换头像事件
/**
 *  好友更换头像事件
 *  内容类似于[FriendDelete]
 */
interface FriendAvatarChanged: EventGet, QQCodeAble, NickOrRemark

/**
 * 好友更换头像事件 实现类
 * @see FriendAvatarChanged
 */
open class MiraiFriendAvatarChangedEvent(event: FriendAvatarChangedEvent): MiraiEventGet<FriendAvatarChangedEvent>(event), FriendAvatarChanged {
    private val friend = event.friend

    private val friendId = friend.id.toString()

    /**
     * 获取备注信息，例如群备注，或者好友备注。
     * @return 备注信息
     */
    override fun getRemark(): String = friend.nick

    /**
     * 可以获取昵称
     * @return nickname
     */
    override fun getNickname(): String = friend.nick

    /**
     * 获取QQ号信息。
     * 假如一个消息封装中存在多个QQ号信息，例如同时存在处理者与被处理者，一般情况下我们认为其返回值为被处理者。
     * @see .getCode
     */
    override fun getQQCode(): String = friendId

    override fun getCodeNumber(): Long = friend.id

    override fun getRemarkOrNickname(): String = friend.nameCardOrNick

    override fun getQQHeadUrl(): String = friend.avatarUrl
}
//endregion