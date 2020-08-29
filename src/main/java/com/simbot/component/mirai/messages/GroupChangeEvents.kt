/*
 *
 * Copyright (c) 2020. ForteScarlet All rights reserved.
 * Project  component-mirai
 * File     GroupChangeEvents.kt
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

@file:Suppress("RedundantVisibilityModifier")

package com.simbot.component.mirai.messages

import com.forte.qqrobot.beans.messages.CodesAble
import com.forte.qqrobot.beans.messages.NickOrRemark
import com.forte.qqrobot.beans.messages.msgget.EventGet
import com.forte.qqrobot.beans.messages.msgget.GroupAdminChange
import com.forte.qqrobot.beans.messages.msgget.GroupMemberIncrease
import com.forte.qqrobot.beans.messages.msgget.GroupMemberReduce
import com.forte.qqrobot.beans.messages.types.GroupAdminChangeType
import com.forte.qqrobot.beans.messages.types.IncreaseType
import com.forte.qqrobot.beans.messages.types.ReduceType
import net.mamoe.mirai.contact.MemberPermission
import net.mamoe.mirai.contact.nameCardOrNick
import net.mamoe.mirai.event.events.*


//region 群成员增减事件


/**
 * 群成员增加事件的统一接口
 * @see MiraiMemberJoinEvent and [Active][MiraiMemberJoinEvent.Active] [Invite][MiraiMemberJoinEvent.Invite]
 * @see MiraiBotJoinEvent and [Active][MiraiBotJoinEvent.Active] [Invite][MiraiBotJoinEvent.Invite]
 */
interface MiraiGroupJoinEvent: GroupMemberIncrease {
    fun isBotSelf(): Boolean
    /** 事件本体 */
    val joinEvent: BotEvent
    /** 离群类型 */
    val increaseType: IncreaseType
    /** 操作者ID */
    val operatorId: String?
}

/**
 * 群成员减少事件的统一接口
 * @see MiraiMemberLeaveEvent and [Kick][MiraiMemberLeaveEvent.Kick] [Quit][MiraiMemberLeaveEvent.Quit]
 * @see MiraiBotLeaveEvent and [Kick][MiraiBotLeaveEvent.Kick] [Active][MiraiBotLeaveEvent.Active]
 */
interface MiraiGroupLeaveEvent: GroupMemberReduce {
    fun isBotSelf(): Boolean
    /** 事件本体 */
    val leaveEvent: BotEvent
    /** 离群类型 */
    val reduceType: ReduceType
    /** 操作者ID */
    val operatorId: String?
}

//region 群成员增加事件

/**
 * 群成员增加事件
 * @see MemberJoinEvent
 */
sealed class MiraiMemberJoinEvent(event: MemberJoinEvent):
        MiraiEventGet<MemberJoinEvent>(event), MiraiGroupJoinEvent {
    private val _self = event.member.id == event.bot.id
    override fun isBotSelf(): Boolean = _self

    /** 入群者的ID */
    private val newMemberId = event.member.id.toString()
    /** 群号 */
    private val groupId = event.group.id.toString()

    /** 入群类型 */
    abstract override val increaseType: IncreaseType
    /** 操作者 */
    abstract override val operatorId: String?

    override val joinEvent: BotEvent
        get() = this.event

    /** 被操作者的QQ号，即入群者  */
    override fun getBeOperatedQQ(): String = newMemberId
    /** 群号  */
    override fun getGroup(): String = groupId
    /** 操作者的QQ号. 可能为null  */
    override fun getOperatorQQ(): String? = operatorId
    /** 获取类型  */
    override fun getType(): IncreaseType = increaseType
    override fun getCodeNumber(): Long = event.member.id
    override fun getGroupCodeNumber(): Long = event.group.id

    /**
     * 被邀请入群的
     */
    class Invite(event: MemberJoinEvent.Invite): MiraiMemberJoinEvent(event) {
        /** 入群类型 */
        override val increaseType: IncreaseType = IncreaseType.AGREE

        /** 操作者. 无法获取，返回null */
        override val operatorId: String? = DeprecatedAPI.memberJoinInviteOperatorQQ
    }

    /**
     * 主动申请入群的
     */
    class Active(event: MemberJoinEvent.Active): MiraiMemberJoinEvent(event) {
        /** 入群类型 */
        override val increaseType: IncreaseType = IncreaseType.AGREE

        /** 操作者 即入群者自己 */
        override val operatorId: String = event.member.id.toString()
    }


}

/**
 * 当bot进入了某个群之后触发此事件。
 * 此事件也属于[群成员增加事件][GroupMemberIncrease]
 * 非bot触发的事件为[MiraiMemberJoinEvent]
 */
open class MiraiBotJoinEvent(event: BotJoinGroupEvent):
        MiraiEventGet<BotJoinGroupEvent>(event), MiraiGroupJoinEvent {
    override fun isBotSelf(): Boolean = true

    /** 入群者的ID */
    private val newMemberId = event.bot.id.toString()
    /** 群号 */
    private val groupId = event.group.id.toString()
    /**
     * 入群类型
     * 暂时默认为主动同意
     * 存在bug mirai预计`1.3.0`会修复
     */
    override val increaseType = IncreaseType.AGREE

    override val joinEvent: BotEvent
        get() = this.event

    override val operatorId: String? = null

    /** 被操作者的QQ号，即入群者  */
    override fun getBeOperatedQQ(): String = newMemberId

    /** 群号  */
    override fun getGroup(): String = groupId

    /** 操作者的QQ号*/
    override fun getOperatorQQ(): String? = null

    /** 获取类型  */
    override fun getType(): IncreaseType = increaseType

    override fun getCodeNumber(): Long = event.bot.id
    override fun getGroupCodeNumber(): Long = event.group.id

    /**
     * 当bot因同意申请等原因**主动**进入了某个群之后触发此事件。
     * [BotJoinGroupEvent.Active]
     * 此事件也属于[群成员增加事件][GroupMemberIncrease]
     * 非bot触发的事件为[MiraiMemberJoinEvent]
     */
    class Active(event: BotJoinGroupEvent.Active):
            MiraiEventGet<BotJoinGroupEvent.Active>(event), MiraiGroupJoinEvent {
        override fun isBotSelf(): Boolean = true

        /** 入群者的ID */
        private val newMemberId = event.bot.id.toString()
        /** 群号 */
        private val groupId = event.group.id.toString()

        override val increaseType = IncreaseType.AGREE

        override val joinEvent: BotEvent
            get() = this.event

        override val operatorId: String = newMemberId

        /** 获取类型  */
        override fun getType(): IncreaseType = increaseType

        /** 群号  */
        override fun getGroup(): String = groupId

        /** 操作者的QQ号，等同于入群者  */
        override fun getOperatorQQ(): String? = newMemberId

        /** 被操作者的QQ号，即入群者  */
        override fun getBeOperatedQQ(): String = operatorId


        override fun getCodeNumber(): Long = event.bot.id
        override fun getGroupCodeNumber(): Long = event.group.id
    }

    /**
     * bot被动的被拉入某群
     * [BotJoinGroupEvent.Invite]
     */
    class Invite(event: BotJoinGroupEvent.Invite):
            MiraiEventGet<BotJoinGroupEvent.Invite>(event), MiraiGroupJoinEvent {
        override fun isBotSelf(): Boolean = true

        /** 入群者的ID */
        private val newMemberId = event.bot.id.toString()
        /** 群号 */
        private val groupId = event.group.id.toString()

        private val invitorId = event.invitor.id.toString()

        override val increaseType = IncreaseType.INVITE
        override val joinEvent: BotEvent
            get() = this.event
        override val operatorId: String = newMemberId

        /** 获取类型  */
        override fun getType(): IncreaseType = increaseType

        /** 群号  */
        override fun getGroup(): String = groupId

        /** 操作者的QQ号，即邀请者  */
        override fun getOperatorQQ(): String? = invitorId

        /** 被操作者的QQ号，即入群者，也就是bot自身  */
        override fun getBeOperatedQQ(): String = operatorId


        override fun getCodeNumber(): Long = event.bot.id
        override fun getGroupCodeNumber(): Long = event.group.id
    }


    }

//endregion

//region 群成员减少事件

/**
 * 群成员减少事件
 */
sealed class MiraiMemberLeaveEvent(event: MemberLeaveEvent):
        MiraiEventGet<MemberLeaveEvent>(event),
        MiraiGroupLeaveEvent {
    private val _self: Boolean = event.member.id == event.bot.id
    override fun isBotSelf(): Boolean = _self
    /** 离群者 */
    private val leaveId = event.member.id.toString()
    private val groupId = event.group.id.toString()

    override val leaveEvent: GroupEvent
        get() = this.event
    
    override fun getCodeNumber(): Long = event.member.id
    override fun getGroupCodeNumber(): Long = event.group.id

    /** 类型 */
    abstract override val reduceType: ReduceType
    /** 操作者ID */
    abstract override val operatorId: String

    /** 被操作者的QQ号，即离群者  */
    override fun getBeOperatedQQ(): String = leaveId

    /** 操作者的QQ号  */
    override fun getOperatorQQ(): String = operatorId

    /** 群号  */
    override fun getGroup(): String = groupId
    /** 获取类型  */
    override fun getType(): ReduceType = reduceType

    /**
     * 群成员减少事件 - 被踢出
     * @see MemberLeaveEvent.Kick
     */
    class Kick(event: MemberLeaveEvent.Kick): MiraiMemberLeaveEvent(event) {
        /** 类型 */
        override val reduceType: ReduceType = ReduceType.KICK_OUT
        /** 操作者ID */
        override val operatorId: String = event.operator?.id?.toString() ?: event.bot.id.toString()
    }

    /**
     * 群成员减少事件 - 主动离去
     * @see MemberLeaveEvent.Quit
     */
    class Quit(event: MemberLeaveEvent.Quit): MiraiMemberLeaveEvent(event) {
        /** 类型 */
        override val reduceType: ReduceType = ReduceType.LEAVE
        /** 操作者ID， 就是离群者自己 */
        override val operatorId: String = event.member.id.toString()
    }
}

/**
 * 群成员减少事件
 * bot离群
 */
sealed class MiraiBotLeaveEvent(event: BotLeaveEvent):
        MiraiEventGet<BotLeaveEvent>(event),
        MiraiGroupLeaveEvent {
    override fun isBotSelf(): Boolean = true
    /** 离群者 */
    private val leaveId = event.bot.id.toString()
    private val groupId = event.group.id.toString()

    override val leaveEvent: BotEvent
        get() = this.event

    override fun getCodeNumber(): Long = event.bot.id
    override fun getGroupCodeNumber(): Long = event.group.id

    /** 类型 */
    abstract override val reduceType: ReduceType
    /** 操作者ID */
    abstract override val operatorId: String

    /** 被操作者的QQ号，即离群者  */
    override fun getBeOperatedQQ(): String = leaveId

    /** 操作者的QQ号  */
    override fun getOperatorQQ(): String = operatorId

    /** 群号  */
    override fun getGroup(): String = groupId
    /** 获取类型  */
    override fun getType(): ReduceType = reduceType

    /**
     * 群成员减少事件 - 被踢出
     * @see MemberLeaveEvent.Kick
     */
    class Kick(event: BotLeaveEvent.Kick): MiraiBotLeaveEvent(event) {
        /** 类型 */
        override val reduceType: ReduceType = ReduceType.KICK_OUT
        /** 操作者ID */
        override val operatorId: String = event.operator.id.toString()
    }

    /**
     * 群成员减少事件 - 主动离去
     * @see MemberLeaveEvent.Quit
     */
    class Active(event: BotLeaveEvent.Active): MiraiBotLeaveEvent(event) {
        /** 类型 */
        override val reduceType: ReduceType = ReduceType.LEAVE
        /** 操作者ID， 就是离群者自己 */
        override val operatorId: String = event.bot.id.toString()
    }
}

//endregion
//endregion


//region 权限变动

/**
 * 权限变动的总接口
 * @see MiraiMemberPermissionChangeEvent
 * @see MiraiBotGroupPermissionChangeEvent
 */
interface MiraiPermissionChangeEvent: GroupAdminChange {
    /** 当前事件 */
    val permissionEvent: BotPassiveEvent
    /** 变更类型 */
    val groupAdminChangeType: GroupAdminChangeType
}

//region 群成员管理员变动
/**
 * 群成员管理员变动（不会是bot
 */
open class MiraiMemberPermissionChangeEvent(event: MemberPermissionChangeEvent):
        MiraiEventGet<MemberPermissionChangeEvent>(event),
        MiraiPermissionChangeEvent {
    private val changeId = event.member.id.toString()
    private val groupId = event.group.id.toString()
    private val operatorId = event.group.owner.id.toString()
    override val groupAdminChangeType = event.toGroupAdminChangeType()

    override val permissionEvent: BotPassiveEvent
        get() = this.event

    /** 被操作者的QQ号  */
    override fun getBeOperatedQQ(): String = changeId

    /** 来自的群  */
    override fun getGroup(): String = groupId
    override fun getCodeNumber(): Long = event.member.id
    override fun getGroupCodeNumber(): Long = event.group.id
    /** 操作者的QQ号（群主）  */
    override fun getOperatorQQ(): String = operatorId

    /** 获取管理员变动类型  */
    override fun getType(): GroupAdminChangeType = groupAdminChangeType
}

//endregion

//region bot权限变更事件
/**
 * bot权限变更事件
 */
open class MiraiBotGroupPermissionChangeEvent(event: BotGroupPermissionChangeEvent):
        MiraiEventGet<BotGroupPermissionChangeEvent>(event),
        MiraiPermissionChangeEvent {
    private val changeId = event.bot.id.toString()
    private val groupId = event.group.id.toString()
    private val operatorId = event.group.owner.id.toString()
    override val groupAdminChangeType = event.toGroupAdminChangeType()

    override val permissionEvent: BotPassiveEvent
        get() = this.event

    override fun getCodeNumber(): Long = event.bot.id

    /** 被操作者的QQ号  */
    override fun getBeOperatedQQ(): String = changeId

    /** 来自的群  */
    override fun getGroup(): String = groupId
    override fun getQQCodeNumber(): Long = event.bot.id
    override fun getGroupCodeNumber(): Long = event.group.id

    /** 操作者的QQ号（群主）  */
    override fun getOperatorQQ(): String = operatorId

    /** 获取管理员变动类型  */
    override fun getType(): GroupAdminChangeType = groupAdminChangeType

}

/**
 * 事件转化为变更类型
 */
private fun MemberPermissionChangeEvent.toGroupAdminChangeType(): GroupAdminChangeType = if(this.new.level >=
        MemberPermission.ADMINISTRATOR.level) GroupAdminChangeType.BECOME_ADMIN else GroupAdminChangeType.CANCEL_ADMIN

/**
 * 事件转化为变更类型
 */
private fun BotGroupPermissionChangeEvent.toGroupAdminChangeType(): GroupAdminChangeType = if(this.new.level >=
        MemberPermission.ADMINISTRATOR.level) GroupAdminChangeType.BECOME_ADMIN else GroupAdminChangeType.CANCEL_ADMIN
//endregion
//endregion


//region 群名称变更
/**
 * 群名称变更事件
 * @see GroupNameChangeEvent
 */
interface GroupNameChanged: EventGet, CodesAble, NickOrRemark {
    /**
     * 变更前
     */
    val oldName: String
    /**
     * 变更后
     */
    val newName: String
    /**
     * 操作人Code
     */
    val operatorCode: String
    /**
     * 操作人CodeNumber
     */
    val operatorCodeNumber: Long
}

/**
 * 群名称变更事件
 * @see GroupNameChanged
 * @see GroupNameChangeEvent
 */
open class MiraiGroupNameChangedEvent(event: GroupNameChangeEvent):
        MiraiEventGet<GroupNameChangeEvent>(event),
        GroupNameChanged
{

    private val groupId: String = event.group.id.toString()
    private val codeId: String = event.operatorOrBot.id.toString()
    /**
     * 变更前
     */
    override val oldName: String get() = event.origin
    /**
     * 变更后
     */
    override val newName: String get() = event.new

    /**
     * 操作人Code
     */
    override val operatorCode: String get() = codeId

    /**
     * 操作人CodeNumber
     */
    override val operatorCodeNumber: Long get() = event.operatorOrBot.id

    /**
     * 获取QQ号信息。
     */
    override fun getQQCode(): String = codeId

    /**
     * 获取消息中存在的群号信息
     */
    override fun getGroupCode(): String = groupId
    /**
     * 获取备注信息，例如群备注，或者好友备注。
     * @return 备注信息
     */
    override fun getRemark(): String = event.operatorOrBot.nameCard
    /**
     * 可以获取昵称
     * @return nickname
     */
    override fun getNickname(): String = event.operatorOrBot.nick
    override fun getRemarkOrNickname(): String = event.operatorOrBot.nameCardOrNick

    override fun getQQCodeNumber(): Long = event.bot.id
    override fun getGroupCodeNumber(): Long = event.group.id

    override fun getQQHeadUrl(): String = event.operatorOrBot.avatarUrl
    override fun getGroupHeadUrl(): String = event.group.avatarUrl
}
//endregion


//region 群成员昵称变更
/**
 * 群员的群昵称变更事件
 * @see MemberCardChangeEvent
 */
interface MemberRemarkChanged: EventGet, CodesAble, NickOrRemark {
    val oldRemark: String
    val newRemark: String
}



/**
 * 群员的群昵称变更事件
 * @see MemberRemarkChanged
 * @see MemberCardChangeEvent
 */
open class MiraiMemberRemarkChangedEvent(event: MemberCardChangeEvent):
        MiraiEventGet<MemberCardChangeEvent>(event),
        MemberRemarkChanged
{

    private val groupId: String = event.group.id.toString()
    private val codeId: String = event.member.id.toString()
    /**
     * 变更前
     */
    override val oldRemark: String get() = event.origin
    /**
     * 变更后
     */
    override val newRemark: String get() = event.new

    /**
     * 获取QQ号信息。
     */
    override fun getQQCode(): String = codeId

    /**
     * 获取消息中存在的群号信息
     */
    override fun getGroupCode(): String = groupId
    /**
     * 获取备注信息，例如群备注，或者好友备注。
     * @return 备注信息
     */
    override fun getRemark(): String = event.member.nameCard
    /**
     * 可以获取昵称
     * @return nickname
     */
    override fun getNickname(): String = event.member.nick
    override fun getRemarkOrNickname(): String = event.member.nameCardOrNick

    override fun getQQCodeNumber(): Long = event.bot.id
    override fun getGroupCodeNumber(): Long = event.group.id

    override fun getQQHeadUrl(): String = event.member.avatarUrl
    override fun getGroupHeadUrl(): String = event.group.avatarUrl
}
//endregion


//region 头衔变更事件
/**
 * 群成员头衔变更事件
 * @see MemberSpecialTitleChangeEvent
 */
interface MemberSpecialTitleChanged: EventGet, CodesAble, NickOrRemark {
    val oldSpecialTitle: String
    val newSpecialTitle: String

    // -- 操作者 可能是bot, 可能是成员自己 --

    val operatorCode: String
    val operatorCodeNumber: Long
}




/**
 * 群员的群头衔变更事件
 * @see MemberSpecialTitleChanged
 * @see MemberSpecialTitleChangeEvent
 */
open class MiraiMemberSpecialTitleChangedEvent(event: MemberSpecialTitleChangeEvent):
        MiraiEventGet<MemberSpecialTitleChangeEvent>(event),
        MemberSpecialTitleChanged
{

    private val groupId: String = event.group.id.toString()
    private val codeId: String = event.member.id.toString()
    private val operatorId: String = event.operatorOrBot.id.toString()
    /**
     * 变更前
     */
    override val oldSpecialTitle: String get() = event.origin
    /**
     * 变更后
     */
    override val newSpecialTitle: String get() = event.new

    override val operatorCode: String = operatorId

    override val operatorCodeNumber: Long
        get() = event.operatorOrBot.id

    /**
     * 获取QQ号信息。
     */
    override fun getQQCode(): String = codeId

    /**
     * 获取消息中存在的群号信息
     */
    override fun getGroupCode(): String = groupId
    /**
     * 获取备注信息，例如群备注，或者好友备注。
     * @return 备注信息
     */
    override fun getRemark(): String = event.member.nameCard
    /**
     * 可以获取昵称
     * @return nickname
     */
    override fun getNickname(): String = event.member.nick
    override fun getRemarkOrNickname(): String = event.member.nameCardOrNick

    override fun getQQCodeNumber(): Long = event.bot.id
    override fun getGroupCodeNumber(): Long = event.group.id

    override fun getQQHeadUrl(): String = event.member.avatarUrl
    override fun getGroupHeadUrl(): String = event.group.avatarUrl
}
//endregion