/*
 *
 *  * Copyright (c) 2020. ForteScarlet All rights reserved.
 *  * Project  component-mirai
 *  * File     messageEvents.kt
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

import com.forte.qqrobot.beans.messages.msgget.GroupMsg
import com.forte.qqrobot.beans.messages.msgget.PrivateMsg
import com.forte.qqrobot.beans.messages.types.GroupMsgType
import com.forte.qqrobot.beans.messages.types.PowerType
import com.forte.qqrobot.beans.messages.types.PrivateMsgType
import com.simbot.component.mirai.CacheMaps
import net.mamoe.mirai.contact.nameCardOrNick
import net.mamoe.mirai.message.GroupMessageEvent
import net.mamoe.mirai.message.MessageEvent
import net.mamoe.mirai.message.TempMessageEvent


//region 消息事件
//region 好友消息事件
/**
 * Mirai的好友消息事件
 * @param event 监听到的事件
 */
open class MiraiFriendMsg(event: MessageEvent, cacheMaps: CacheMaps): MiraiMessageGet<MessageEvent>(event, cacheMaps), PrivateMsg {
    private val eventTime: Long = event.time.toLong()

    override val onTime: Long get() = eventTime

    /** 获取发送人的QQ号  */
    override fun getQQ(): String = contact.id.toString()



    override fun getCodeNumber(): Long = event.sender.id

    /** 获取私聊消息类型，固定为好友 */
    override fun getType(): PrivateMsgType = PrivateMsgType.FROM_FRIEND

    /** 获取消息的字体  */
    override fun getFont(): String? = null

    /**
     * 可以获取昵称
     * @return nickname
     */
    override fun getNickname() = event.senderName

    /**
     * 获取备注信息
     * @return 备注信息
     */
    override fun getRemark() = nickname


    override fun getRemarkOrNickname() = nickname


}
//endregion


//region 群临时会话消息
/**
 * 群临时会话消息，同样属于私信
 */
open class MiraiTempMsg(event: TempMessageEvent, cacheMaps: CacheMaps): MiraiFriendMsg(event, cacheMaps) {
    override fun getType(): PrivateMsgType = PrivateMsgType.FROM_GROUP
    override fun getCodeNumber(): Long = event.sender.id
}
//endregion


//region 群消息事件
/**
 * Mirai的群消息事件
 * @param event 监听到的事件
 */
open class MiraiGroupMsg(event: GroupMessageEvent, cacheMaps: CacheMaps): MiraiMessageGet<GroupMessageEvent>(event, cacheMaps), GroupMsg {

//    override val messageEvent: GroupMessageEvent = event

    private val senderId = event.sender.id.toString()
    private val groupId = event.group.id.toString()
    private var memberPowerType = event.sender.permission.powerType

    private val eventTime = event.time.toLong()
    override val onTime: Long get() = eventTime

    /** 获取群消息发送人的qq号  */
    override fun getQQ(): String = senderId
    /** 获取群消息的群号  */
    override fun getGroup(): String = groupId
    override fun getCodeNumber(): Long = event.sender.id
    override fun getGroupCodeNumber(): Long = event.group.id
    /**
     * 获取此人在群里的权限
     * @return 权限，例如群员、管理员等
     */
    override fun getPowerType(): PowerType = memberPowerType

    /**
     * 重新定义此人的权限
     * @param powerType 权限
     */
    override fun setPowerType(powerType: PowerType) {
        memberPowerType = powerType
    }

    /** 获取消息类型  */
    override fun getType(): GroupMsgType = GroupMsgType.NORMAL_MSG

    /**
     * 获取备注信息，例如群备注，或者好友备注。
     * @return 备注信息
     */
    override fun getRemark(): String = event.sender.nameCard

    /**
     * 可以获取昵称
     * @return nickname
     */
    override fun getNickname(): String = event.sender.nick

    /**
     * nick or card
     */
    override fun getRemarkOrNickname(): String = event.sender.nameCardOrNick

}

//endregion
//endregion