/*
 *
 * Copyright (c) 2020. ForteScarlet All rights reserved.
 * Project  component-mirai
 * File     FriendChangeEvents.kt
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

import com.forte.qqrobot.beans.messages.NickOrRemark
import com.forte.qqrobot.beans.messages.QQCodeAble
import com.forte.qqrobot.beans.messages.msgget.EventGet
import com.forte.qqrobot.beans.messages.msgget.FriendAdd
import com.forte.qqrobot.beans.messages.msgget.FriendDelete
import net.mamoe.mirai.contact.nameCardOrNick
import net.mamoe.mirai.event.events.*

/*
 * 好友变动相关事件
 */


//region 好友增加事件
/**
 * 好友增加事件
 * @see FriendAddEvent
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
 * @see FriendDeleteEvent
 */
open class MiraiFriendDeleteEvent(event: FriendDeleteEvent): MiraiEventGet<FriendDeleteEvent>(event), FriendDelete {
    private val friend get() = event.friend
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
 *  @see MiraiFriendAvatarChangedEvent
 */
interface FriendAvatarChanged: EventGet, QQCodeAble, NickOrRemark


/**
 * 好友更换头像事件 实现类
 * @see FriendAvatarChanged
 * @see FriendAvatarChangedEvent
 */
open class MiraiFriendAvatarChangedEvent(event: FriendAvatarChangedEvent):
        MiraiEventGet<FriendAvatarChangedEvent>(event), FriendAvatarChanged {
    private val friend get() = event.friend

    private val friendId = friend.id.toString()

    /**
     * 可以获取昵称
     * @return nickname
     */
    override fun getNickname(): String = friend.nick
    /**
     * 获取备注信息，例如群备注，或者好友备注。
     * @return 备注信息
     */
    override fun getRemark(): String = nickname
    /**
     * 获取QQ号信息。
     */
    override fun getQQCode(): String = friendId
    override fun getCodeNumber(): Long = friend.id

    override fun getRemarkOrNickname(): String = nickname
    override fun getQQHeadUrl(): String = friend.avatarUrl
}
//endregion



//region 好友昵称变更事件
/**
 * 好友昵称已经变更事件接口
 * @see net.mamoe.mirai.event.events.FriendNickChangedEvent
 * @see
 */
interface FriendNicknameChanged: EventGet, QQCodeAble, NickOrRemark {
    /** 变更后的新昵称, 应该是与当前Friend得到的值一样 */
    val newNickname: String
    /** 变更前的旧昵称 */
    val oldNickname: String
}


/**
 * 好友头像变更事件
 * @see FriendNicknameChanged
 * @see FriendNickChangedEvent
 */
open class MiraiFriendNicknameChangedEvent(event: FriendNickChangedEvent):
        MiraiEventGet<FriendNickChangedEvent>(event), FriendNicknameChanged {

    private val friendId = event.friend.id.toString()

    /** 变更后的新昵称 */
    override val newNickname: String get() = event.to

    /** 变更前的旧昵称 */
    override val oldNickname: String get() = event.from

    /**
     * 获取QQ号信息。
     */
    override fun getQQCode(): String = friendId
    override fun getCodeNumber(): Long = event.friend.id
    override fun getQQHeadUrl(): String = event.friend.avatarUrl
    /**
     * 可以获取昵称
     * @return nickname
     */
    override fun getNickname(): String = event.friend.nick
    /**
     * @see getNickname
     */
    override fun getRemark(): String = nickname
    /**
     * @see getNickname
     */
    override fun getRemarkOrNickname(): String = nickname
}
//endregion


/**
 * 好友输入框状态改变事件
 * 当开始输入文字、退出聊天窗口或清空输入框时会触发此事件
 * @see net.mamoe.mirai.event.events.FriendInputStatusChangedEvent
 */
interface FriendInputStatusChanged: EventGet, QQCodeAble, NickOrRemark {
    /** 是否正在输入 */
    val inputting: Boolean
}


/**
 * 好友输入框状态改变事件实现类
 * @see net.mamoe.mirai.event.events.FriendInputStatusChangedEvent
 * @see FriendInputStatusChanged
 */
open class MiraiFriendInputStatusChangedEvent(event: FriendInputStatusChangedEvent):
        MiraiEventGet<FriendInputStatusChangedEvent>(event), FriendInputStatusChanged {

    /**
     * 当前是否为输入状态
     */
    override val inputting: Boolean
        get() = event.inputting


    private val friendId = event.friend.id.toString()
    /**
     * 获取QQ号信息。
     */
    override fun getQQCode(): String = friendId
    override fun getCodeNumber(): Long = event.friend.id
    override fun getQQHeadUrl(): String = event.friend.avatarUrl
    /**
     * 可以获取昵称
     * @return nickname
     */
    override fun getNickname(): String = event.friend.nick
    /**
     * @see getNickname
     */
    override fun getRemark(): String = nickname
    /**
     * @see getNickname
     */
    override fun getRemarkOrNickname(): String = nickname
}

