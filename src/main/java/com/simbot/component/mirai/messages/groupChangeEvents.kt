/*
 *
 *  * Copyright (c) 2020. ForteScarlet All rights reserved.
 *  * Project  component-mirai
 *  * File     groupChangeEvents.kt
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

import com.forte.qqrobot.beans.messages.msgget.GroupAdminChange
import com.forte.qqrobot.beans.messages.msgget.GroupMemberIncrease
import com.forte.qqrobot.beans.messages.msgget.GroupMemberReduce
import com.forte.qqrobot.beans.messages.types.GroupAdminChangeType
import com.forte.qqrobot.beans.messages.types.IncreaseType
import com.forte.qqrobot.beans.messages.types.ReduceType
import net.mamoe.mirai.contact.MemberPermission
import net.mamoe.mirai.contact.isOperator
import net.mamoe.mirai.event.events.*


//region 群成员增减事件


//region 群成员增加事件
/**
 * 群成员增加事件
 */
open class MiraiMemberJoinEvent(event: MemberJoinEvent): MiraiEventGet<MemberJoinEvent>(event), GroupMemberIncrease {

    fun isBotSelf(): Boolean = event.member.id == event.bot.id

    /** 入群者的ID */
    private val newMemberId = event.member.id.toString()

    /** 群号 */
    private val groupId = event.group.id.toString()

    /** 入群类型 */
    private val increaseType = event.toIncreaseType()

    /** 被操作者的QQ号，即入群者  */
    override fun getBeOperatedQQ(): String = newMemberId

    /** 群号  */
    override fun getGroup(): String = groupId

    /** 操作者的QQ号，似乎无法获取  */
    @Deprecated("just null", ReplaceWith("null"))
    override fun getOperatorQQ(): String? = DeprecatedAPI.memberJoinOperatorQQ

    /** 获取类型  */
    override fun getType(): IncreaseType = increaseType

    override fun getCodeNumber(): Long = event.member.id
    override fun getGroupCodeNumber(): Long = event.group.id

}

/**
 * 当bot进入了某个群之后触发此事件。
 * 此事件也属于[群成员增加事件][GroupMemberIncrease]
 * 非bot触发的事件为[MiraiMemberJoinEvent]
 */
open class MiraiBotJoinEvent(event: BotJoinGroupEvent): MiraiEventGet<BotJoinGroupEvent>(event), GroupMemberIncrease {
    fun isBotSelf(): Boolean = true

    /** 入群者的ID */
    private val newMemberId = event.bot.id.toString()
    /** 群号 */
    private val groupId = event.group.id.toString()
    /**
     * 入群类型
     * 暂时默认为主动同意
     * todo 存在bug mirai预计`1.3.0`会修复
     */
    private val increaseType = IncreaseType.AGREE

    /** 被操作者的QQ号，即入群者  */
    override fun getBeOperatedQQ(): String = newMemberId

    /** 群号  */
    override fun getGroup(): String = groupId

    /** 操作者的QQ号，似乎无法获取  */
    @Deprecated("just null", ReplaceWith("null"))
    override fun getOperatorQQ(): String? = DeprecatedAPI.memberJoinOperatorQQ

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
    class Active(event: BotJoinGroupEvent.Active): MiraiEventGet<BotJoinGroupEvent.Active>(event), GroupMemberIncrease {
        fun isBotSelf(): Boolean = true

        /** 入群者的ID */
        private val newMemberId = event.bot.id.toString()
        /** 群号 */
        private val groupId = event.group.id.toString()

        /** 获取类型  */
        override fun getType(): IncreaseType = IncreaseType.AGREE

        /** 群号  */
        override fun getGroup(): String = groupId

        /** 操作者的QQ号，等同于入群者  */
        override fun getOperatorQQ(): String? = newMemberId

        /** 被操作者的QQ号，即入群者  */
        override fun getBeOperatedQQ(): String = newMemberId


        override fun getCodeNumber(): Long = event.bot.id
        override fun getGroupCodeNumber(): Long = event.group.id
    }

    /**
     * bot被动的被拉入某群
     * [BotJoinGroupEvent.Invite]
     */
    class Invite(event: BotJoinGroupEvent.Invite): MiraiEventGet<BotJoinGroupEvent.Invite>(event), GroupMemberIncrease {
        fun isBotSelf(): Boolean = true

        /** 入群者的ID */
        private val newMemberId = event.bot.id.toString()
        /** 群号 */
        private val groupId = event.group.id.toString()

        private val invitorId = event.invitor.id.toString()

        /** 获取类型  */
        override fun getType(): IncreaseType = IncreaseType.AGREE

        /** 群号  */
        override fun getGroup(): String = groupId

        /** 操作者的QQ号，即邀请者  */
        override fun getOperatorQQ(): String? = invitorId

        /** 被操作者的QQ号，即入群者，也就是bot自身  */
        override fun getBeOperatedQQ(): String = newMemberId


        override fun getCodeNumber(): Long = event.bot.id
        override fun getGroupCodeNumber(): Long = event.group.id
    }


    }




/**
 * [MemberJoinEvent]转化为[IncreaseType]
 */
fun MemberJoinEvent.toIncreaseType(): IncreaseType = when(this){
    is MemberJoinEvent.Active -> IncreaseType.AGREE
    is MemberJoinEvent.Invite -> IncreaseType.INVITE
    else -> IncreaseType.AGREE
}


//endregion

//region 群成员减少事件
/**
 * 群成员减少事件
 */
sealed class MiraiMemberLeaveEvent(event: MemberLeaveEvent): MiraiEventGet<MemberLeaveEvent>(event), GroupMemberReduce {
    /** 离群者 */
    private val leaveId = event.member.id.toString()
    private val groupId = event.group.id.toString()

    override fun getCodeNumber(): Long = event.member.id
    override fun getGroupCodeNumber(): Long = event.group.id

    /** 类型 */
    protected abstract val reduceType: ReduceType
    /** 操作者ID */
    protected abstract val operatorId: String

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
sealed class MiraiBotLeaveEvent(event: BotLeaveEvent): MiraiEventGet<BotLeaveEvent>(event), GroupMemberReduce {
    /** 离群者 */
    private val leaveId = event.bot.id.toString()
    private val groupId = event.group.id.toString()

    override fun getCodeNumber(): Long = event.bot.id
    override fun getGroupCodeNumber(): Long = event.group.id

    /** 类型 */
    protected abstract val reduceType: ReduceType
    /** 操作者ID */
    protected abstract val operatorId: String

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
//region 群成员管理员变动
/**
 * 群成员管理员变动（不会是bot
 */
open class MiraiMemberPermissionChangeEvent(event: MemberPermissionChangeEvent): MiraiEventGet<MemberPermissionChangeEvent>(event), GroupAdminChange {
    private val changeId = event.member.id.toString()
    private val groupId = event.group.id.toString()
    private val operatorId = event.group.owner.id.toString()
    private val groupAdminChangeType = event.toGroupAdminChangeType()

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

/**
 * 事件转化为变更类型
 */
internal fun MemberPermissionChangeEvent.toGroupAdminChangeType(): GroupAdminChangeType = if(this.new.level >= MemberPermission.ADMINISTRATOR.level) GroupAdminChangeType.BECOME_ADMIN else GroupAdminChangeType.CANCEL_ADMIN

//endregion

//region bot权限变更事件
/**
 * bot权限变更事件
 */
open class MiraiBotGroupPermissionChangeEvent(event: BotGroupPermissionChangeEvent): MiraiEventGet<BotGroupPermissionChangeEvent>(event), GroupAdminChange {
    private val changeId = event.bot.id.toString()
    private val groupId = event.group.id.toString()
    private val operatorId = event.group.owner.id.toString()
    private val groupAdminChangeType = event.toGroupAdminChangeType()

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
internal fun BotGroupPermissionChangeEvent.toGroupAdminChangeType(): GroupAdminChangeType = if(this.new.level >= MemberPermission.ADMINISTRATOR.level) GroupAdminChangeType.BECOME_ADMIN else GroupAdminChangeType.CANCEL_ADMIN
//endregion
//endregion