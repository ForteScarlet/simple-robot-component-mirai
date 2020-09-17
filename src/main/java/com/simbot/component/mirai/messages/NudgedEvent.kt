/*
 * Copyright (c) 2020. ForteScarlet All rights reserved.
 * Project  component-mirai (Codes other than Mirai)
 * File     NudgedEvent.kt (Codes other than Mirai)
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

import com.forte.qqrobot.beans.messages.msgget.GroupMsg
import com.forte.qqrobot.beans.messages.msgget.PrivateMsg
import com.forte.qqrobot.beans.messages.types.GroupMsgType
import com.forte.qqrobot.beans.messages.types.PowerType
import com.forte.qqrobot.beans.messages.types.PrivateMsgType
import com.simplerobot.modules.utils.KQCodeUtils
import net.mamoe.mirai.contact.Friend
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.contact.nameCardOrNick
import net.mamoe.mirai.event.events.BotNudgedEvent
import net.mamoe.mirai.event.events.MemberNudgedEvent


/**
 * 头像戳一戳事件父接口
 */
public interface MiraiNudgedEvent {
    /**
     * 被戳的是不是bot自己
     */
    val isBot: Boolean

    /**
     * 谁发起的
     */
    val from: String

    /**
     * 被戳的
     */
    val target: String

    /**
     * 戳一戳的动作名称
     */
    val action: String
    /**
     * 戳一戳中设置的自定义后缀
     */
    val suffix: String
}

/**
 * member Nudged event
 */
public class MiraiMemberNudgedEvent(event: MemberNudgedEvent) : MiraiBaseMsgGet<MemberNudgedEvent>(event), GroupMsg, MiraiNudgedEvent {

    private val nudgeId = "nudge.${event.group}.${event.from}.${event.member}.${System.currentTimeMillis()}"
    private val nudgeString = "${event.from.nameCardOrNick}${event.action}${event.member.nameCardOrNick}${event.suffix}"

    private val nudgeCode: String =
        KQCodeUtils.toCq("nudge", false, "code=${event.from.id}", "target=${event.bot.id}", "action=${event.action}", "suffix=${event.suffix}")

    private var _eventMsg: String? = nudgeCode

    /** event消息 */
    override var eventMsg: String?
        get() = _eventMsg
        set(value) {
            _eventMsg = value
        }
    /** toString是必须的 */
    override fun toString(): String = "NudgeEvent($nudgeCode$nudgeString)"

    /** 获取ID  */
    override fun getId(): String = nudgeId

    /**
     * 被戳的是不是bot自己
     */
    override val isBot: Boolean = false

    /**
     * 谁发起的
     */
    override val from: String = event.from.id.toString()

    /**
     * 被戳的
     */
    override val target: String = event.member.id.toString()

    /**
     * 戳一戳的动作名称
     */
    override val action: String = event.action

    /**
     * 戳一戳中设置的自定义后缀
     */
    override val suffix: String = event.suffix



//    override val messageEvent: GroupMessageEvent = event

    private val senderId get() = event.from.id.toString()
    private val groupId get() = event.group.id.toString()
    private var memberPowerType = event.from.permission.powerType

    /** 获取群消息发送人的qq号  */
    override fun getQQ(): String = senderId
    /** 获取群消息的群号  */
    override fun getGroup(): String = groupId
    override fun getCodeNumber(): Long = event.from.id
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
    override fun getRemark(): String = event.from.nameCard

    /**
     * 可以获取昵称
     * @return nickname
     */
    override fun getNickname(): String = event.from.nick

    /**
     * nick or card
     */
    override fun getRemarkOrNickname(): String = event.from.nameCardOrNick
}





/**
 * bot Nudged event
 */
public abstract class MiraiBotNudgedEvent(event: BotNudgedEvent) : MiraiBaseMsgGet<BotNudgedEvent>(event), MiraiNudgedEvent {

    private val nudgeId = "nudge.${event.from}.${event.bot.id}.${System.currentTimeMillis()}"
    private val nudgeString = "${event.from.nameCardOrNick}${event.action}${event.bot.nick}${event.suffix}"

    private val nudgeCode: String =
        KQCodeUtils.toCq("nudge", false, "code=${event.from.id}", "target=${event.bot.id}", "action=${event.action}", "suffix=${event.suffix}")

    private var _eventMsg: String? = nudgeCode

    /** event消息 */
    override var eventMsg: String?
        get() = _eventMsg
        set(value) {
            _eventMsg = value
        }

    /** toString是必须的 */
    override fun toString(): String = "NudgeEvent($nudgeCode$nudgeString)"

    /** 获取ID  */
    override fun getId(): String = nudgeId

    /**
     * 被戳的是不是bot自己
     */
    override val isBot: Boolean = true

    /**
     * 谁发起的
     */
    override val from: String = event.from.id.toString()

    /**
     * 被戳的
     */
    override val target: String = event.bot.id.toString()

    /**
     * 戳一戳的动作名称
     */
    override val action: String = event.action

    /**
     * 戳一戳中设置的自定义后缀
     */
    override val suffix: String = event.suffix
}


/**
 * 群消息被戳
 */
public class MiraiBotGroupNudgedEvent(event: BotNudgedEvent) : MiraiBotNudgedEvent(event), GroupMsg {

    private val member: Member get() = event.from as Member

    private val senderId = member.id.toString()
    private val groupId = member.group.id.toString()
    private var memberPowerType = member.permission.powerType


    /** 获取群消息发送人的qq号  */
    override fun getQQ(): String = senderId
    /** 获取群消息的群号  */
    override fun getGroup(): String = groupId
    override fun getCodeNumber(): Long = member.id
    override fun getGroupCodeNumber(): Long = member.group.id
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
    override fun getRemark(): String = member.nameCard

    /**
     * 可以获取昵称
     * @return nickname
     */
    override fun getNickname(): String = member.nick

    /**
     * nick or card
     */
    override fun getRemarkOrNickname(): String = member.nameCardOrNick
}

/**
 * 私聊好友消息被戳
 */
public class MiraiBotFriendNudgedEvent(event: BotNudgedEvent) : MiraiBotNudgedEvent(event), PrivateMsg {

    private val friend get() = event.from as Friend

    /** 获取发送人的QQ号  */
    override fun getQQ(): String = friend.id.toString()



    override fun getCodeNumber(): Long = friend.id

    /** 获取私聊消息类型，固定为好友 */
    override fun getType(): PrivateMsgType = PrivateMsgType.FROM_FRIEND

    /** 获取消息的字体  */
    override fun getFont(): String? = null

    /**
     * 可以获取昵称
     * @return nickname
     */
    override fun getNickname() = friend.nick
    /**
     * 获取备注信息
     * @return 备注信息
     */
    override fun getRemark() = nickname
    override fun getRemarkOrNickname() = friend.nameCardOrNick
}

/**
 * 私聊临时消息被戳
 */
public class MiraiBotTempNudgedEvent(event: BotNudgedEvent) : MiraiBotNudgedEvent(event), PrivateMsg {

    private val member get() = event.from as Member

    /** 获取发送人的QQ号  */
    override fun getQQ(): String = member.id.toString()



    override fun getCodeNumber(): Long = member.id

    /** 获取私聊消息类型，固定为来自群 */
    override fun getType(): PrivateMsgType = PrivateMsgType.FROM_GROUP

    /** 获取消息的字体  */
    override fun getFont(): String? = null

    /**
     * 可以获取昵称
     * @return nickname
     */
    override fun getNickname() = member.nick
    /**
     * 获取备注信息
     * @return 备注信息
     */
    override fun getRemark() = member.nameCard
    override fun getRemarkOrNickname() = member.nameCardOrNick
}