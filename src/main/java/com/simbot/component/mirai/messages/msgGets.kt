/*
 * Copyright (c) 2020. ForteScarlet All rights reserved.
 * Project  component-mirai (Codes other than Mirai)
 * File     msgGets.kt (Codes other than Mirai)
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

import com.forte.qqrobot.beans.messages.msgget.*
import com.forte.qqrobot.beans.messages.types.*
import com.simbot.component.mirai.MiraiCodeFormatUtils
import com.simbot.component.mirai.RecallCache
import com.simbot.component.mirai.RequestCache
import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.*
import net.mamoe.mirai.event.events.*
import net.mamoe.mirai.message.GroupMessageEvent
import net.mamoe.mirai.message.MessageEvent
import net.mamoe.mirai.message.TempMessageEvent
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.source


//region 基础MsgGet抽象类
/**
 * 基础MsgGet父类
 */
abstract class MiraiBaseMsgGet<out E: BotEvent>(open val event: E): MsgGet {

    open val onTime = System.currentTimeMillis()

    /** event消息 */
    abstract var eventMsg: String?

    // bot id
    abstract var botId : String

    /** 获取原本的数据 originalData  */
    override fun getOriginalData(): String = toString()

    /** toString是必须的 */
    abstract override fun toString(): String

    /**
     * 此消息获取的时候，代表的是哪个账号获取到的消息。
     * @return 接收到此消息的账号。
     */
    override fun getThisCode(): String = event.bot.id.toString()

    /** bot id */
    override fun setThisCode(code: String) {
//        botId = code
    }

    /**
     * 重新设置消息
     * @param newMsg msg
     */
    override fun setMsg(newMsg: String?) {
        eventMsg = newMsg
    }
    /**
     * 一般来讲，监听到的消息大部分都会有个“消息内容”。定义此方法获取消息内容。
     * 如果不存在，则为null。（旧版本推荐为空字符串，现在不了。我变卦了）
     */
    override fun getMsg(): String? = eventMsg


    /** 获取消息的字体  */
    override fun getFont(): String? = null

    /** 获取到的时间, 代表某一时间的秒值。一般情况下是秒值。如果类型不对请自行转化  */
    override fun getTime(): Long = onTime

}

/**
 * mirai 的msgGet的父类，可获取contact对象
 *
 * 消息类型的msgGet
 *
 * 且实现[MsgGet]接口
 */
abstract class MiraiMessageGet<out ME: MessageEvent>(override val event: ME): MiraiBaseMsgGet<ME>(event) {
//    protected open val messageEvent = event

    /**
     * 获取contact
     * > 联系对象, 即可以与 [Bot] 互动的对象. 包含 [用户][User], 和 [群][Group].
     * @see [Contact]
     */
    val contact: Contact get() = event.sender
    val message: MessageChain get() = event.message

    /** 消息id */
    private val msgId: String by lazy { RecallCache.cache(message.source) }

    /** 消息正文，目前会将mirai码替换为CQ码 */
    override var eventMsg: String? = MiraiCodeFormatUtils.mi2cq(message)

    // bot id
    override var botId: String = contact.bot.id.toString()


    /** 获取ID, 一般可用于撤回  */
    override fun getId(): String = msgId


    override fun toString(): String = message.contentToString()
}

/**
 *  mirai 的事件EventGet的父类
 *  实现[EventGet]接口
 */
abstract class MiraiEventGet<out EE: BotEvent>(event: EE): MiraiBaseMsgGet<EE>(event), EventGet {
    /** 事件消息正文 */
    override var eventMsg: String? = null
    protected val eventId by lazy { "$event#$onTime" }

    override var botId: String = event.bot.id.toString()

    /**
     * 重新设置消息
     * @param newMsg msg
     */
    override fun setMsg(newMsg: String?) {
        eventMsg = newMsg
    }
    /**
     * 一般来讲，监听到的消息大部分都会有个“消息内容”。定义此方法获取消息内容。
     * 如果不存在，则为null。（旧版本推荐为空字符串，现在不了。我变卦了）
     */
    override fun getMsg(): String? = eventMsg


    /** 获取消息的字体  */
    override fun getFont(): String? = null

    override fun toString(): String = event.toString()
}
//endregion



//region 消息事件



//region 好友消息事件
/**
 * Mirai的好友消息事件
 * @param event 监听到的事件
 */
open class MiraiFriendMsg(event: MessageEvent): MiraiMessageGet<MessageEvent>(event), PrivateMsg {

    override val onTime: Long get() = event.time.toLong()

    /** 获取发送人的QQ号  */
    override fun getQQ(): String = contact.id.toString()



    override fun getQQCodeNumber(): Long = event.sender.id

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
open class MiraiTempMsg(event: TempMessageEvent): MiraiFriendMsg(event) {
    override fun getType(): PrivateMsgType = PrivateMsgType.FROM_GROUP
}
//endregion


//region 群消息事件
/**
 * Mirai的群消息事件
 * @param event 监听到的事件
 */
open class MiraiGroupMsg(event: GroupMessageEvent): MiraiMessageGet<GroupMessageEvent>(event), GroupMsg {

//    override val messageEvent: GroupMessageEvent = event

    private val senderId by lazy { event.sender.id.toString() }
    private val groupId by lazy { event.group.id.toString() }
    private var memberPowerType = event.sender.permission.toPowerType()

    override val onTime: Long = event.time.toLong()

    /** 获取群消息发送人的qq号  */
    override fun getQQ(): String = senderId
    /** 获取群消息的群号  */
    override fun getGroup(): String = groupId
    override fun getQQCodeNumber(): Long = event.sender.id
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

//region 申请/邀请相关事件

//region bot被邀请入群事件
/**
 * bot被邀请入群事件
 */
open class MiraiBotInvitedJoinGroupRequestEvent(eventEvent: BotInvitedJoinGroupRequestEvent): MiraiEventGet<BotInvitedJoinGroupRequestEvent>(eventEvent), GroupAddRequest {
    private val invitorId by lazy { eventEvent.invitorId.toString() }
    private val groupId by lazy { eventEvent.groupId.toString() }

    /** 获取QQ号  */
    override fun getQQ(): String = invitorId

    /** 获取群号  */
    override fun getGroup(): String = groupId
    override fun getQQCodeNumber(): Long = event.invitorId
    override fun getGroupCodeNumber(): Long = event.groupId
    /** 获取ID, 与flag一致  */
    override fun getId(): String = flag

    /** 获取标识  */
    override fun getFlag(): String = RequestCache.cache(event)

    /** 加群类型，此处为被邀请入群  */
    override fun getRequestType(): GroupAddRequestType = GroupAddRequestType.INVITE
}
//endregion

//region 加群申请
/**
 * 加群申请
 */
open class MiraiMemberJoinRequestEvent(eventEvent: MemberJoinRequestEvent): MiraiEventGet<MemberJoinRequestEvent>(eventEvent), GroupAddRequest {
    private val fromId by lazy { event.fromId.toString() }
    private val groupId by lazy { event.groupId.toString() }

    /** 获取QQ号  */
    override fun getQQ(): String = fromId

    /** 获取群号  */
    override fun getGroup(): String = groupId
    override fun getQQCodeNumber(): Long = event.fromId
    override fun getGroupCodeNumber(): Long = event.groupId
    /** 获取ID, 与flag一致  */
    override fun getId(): String = flag

    /** 获取标识  */
    override fun getFlag(): String = RequestCache.cache(event)

    /** 加群类型，此处为申请入群  */
    override fun getRequestType(): GroupAddRequestType = GroupAddRequestType.ADD
}
//endregion


//region 好友添加申请
/**
 * 好友添加申请
 */
open class MiraiNewFriendRequestEvent(eventEvent: NewFriendRequestEvent): MiraiEventGet<NewFriendRequestEvent>(eventEvent), FriendAddRequest {
    private val fromId by lazy { event.fromId.toString() }
    private val requestFlag = RequestCache.cache(eventEvent)

    /** 请求人QQ  */
    override fun getQQ(): String = fromId
    override fun getQQCodeNumber(): Long = event.fromId
    /** 获取ID, 与flag相同 */
    override fun getId(): String = flag

    /** 获取标识  */
    override fun getFlag(): String = requestFlag
}
//endregion
//endregion

//region 好友/群成员增减事件
//region 好友增加事件
/**
 * 好友增加事件
 */
open class MiraiFriendAddEvent(event: FriendAddEvent): MiraiEventGet<FriendAddEvent>(event), FriendAdd {
    private val fromId by lazy { event.friend.id.toString() }

    /** 添加人的QQ  */
    override fun getQQ(): String = fromId
    override fun getQQCodeNumber(): Long = event.friend.id
    /** 获取ID, 此ID无实际意义  */
    override fun getId(): String = eventId

}
//endregion

//region 群成员增加事件
/**
 * 群成员增加事件
 */
open class MiraiMemberJoinEvent(event: MemberJoinEvent): MiraiEventGet<MemberJoinEvent>(event), GroupMemberIncrease {

    /** 入群者的ID */
    private val newMemberId by lazy { event.member.id.toString() }

    /** 群号 */
    private val groupId by lazy { event.group.id.toString() }

    /** 入群类型 */
    private val increaseType by lazy { event.toIncreaseType() }

    /** 被操作者的QQ号，即入群者  */
    override fun getBeOperatedQQ(): String = newMemberId

    /** 群号  */
    override fun getGroup(): String = groupId

    /** 获取ID, 此处含义不大  */
    override fun getId(): String = eventId

    /** 操作者的QQ号，似乎无法获取  */
    @Deprecated("just null", ReplaceWith("null"))
    override fun getOperatorQQ(): String? = DeprecatedAPI.memberJoinOperatorQQ

    /** 获取类型  */
    override fun getType(): IncreaseType = increaseType

    override fun getQQCodeNumber(): Long = event.member.id
    override fun getGroupCodeNumber(): Long = event.group.id

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
open class MiraiMemberLeaveEvent(event: MemberLeaveEvent): MiraiEventGet<MemberLeaveEvent>(event), GroupMemberReduce {
    /** 离群者 */
    private val leaveId by lazy { event.member.id.toString() }

    /** 操作者ID */
    private val operatorId by lazy { event.getOperatorId().toString() }
    private val groupId by lazy { event.group.id.toString() }
    override fun getQQCodeNumber(): Long = event.member.id
    override fun getGroupCodeNumber(): Long = event.group.id

    /** 类型 */
    private val reduceType by lazy { event.toReduceType() }

    /** 被操作者的QQ号，即离群者  */
    override fun getBeOperatedQQ(): String = leaveId

    /** 操作者的QQ号  */
    override fun getOperatorQQ(): String = operatorId

    /** 群号  */
    override fun getGroup(): String = groupId

    /** 获取ID, 无大意义 */
    override fun getId(): String = eventId

    /** 获取类型  */
    override fun getType():ReduceType = reduceType
}

/**
 * 获取操作者ID
 */
internal fun MemberLeaveEvent.getOperatorId(): Long = when(this){
    // 踢出
    is MemberLeaveEvent.Kick -> this.operator?.id ?: this.bot.id
    // 自己离开，操作者就是自己
    is MemberLeaveEvent.Quit -> this.member.id
    else -> this.member.id
}

/**
 * 离群类型
 */
internal fun MemberLeaveEvent.toReduceType() = when(this){
    is MemberLeaveEvent.Kick -> ReduceType.KICK_OUT
    is MemberLeaveEvent.Quit -> ReduceType.LEAVE
    else -> ReduceType.LEAVE
}

//endregion
//endregion


//region 权限变动
//region 群成员管理员变动
/**
 * 群成员管理员变动（不会是bot
 */
open class MiraiMemberPermissionChangeEvent(event: MemberPermissionChangeEvent): MiraiEventGet<MemberPermissionChangeEvent>(event), GroupAdminChange {
    private val changeId by lazy { event.member.id.toString() }
    private val groupId by lazy { event.group.id.toString() }
    private val operatorId by lazy { event.group.owner.id.toString() }
    private val groupAdminChangeType by lazy { event.toGroupAdminChangeType() }

    /** 被操作者的QQ号  */
    override fun getBeOperatedQQ(): String = changeId

    /** 来自的群  */
    override fun getGroup(): String = groupId

    /** 获取ID, 一般用于消息类型判断  */
    override fun getId(): String = eventId
    override fun getQQCodeNumber(): Long = event.member.id
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
    private val changeId by lazy { event.bot.id.toString() }
    private val groupId by lazy { event.group.id.toString() }
    private val operatorId by lazy { event.group.owner.id.toString() }
    private val groupAdminChangeType by lazy { event.toGroupAdminChangeType() }

    /** 被操作者的QQ号  */
    override fun getBeOperatedQQ(): String = changeId

    /** 来自的群  */
    override fun getGroup(): String = groupId
    override fun getQQCodeNumber(): Long = event.bot.id
    override fun getGroupCodeNumber(): Long = event.group.id
    /** 获取ID, 一般用于消息类型判断  */
    override fun getId(): String = eventId

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


//region 撤回事件
/**
 * 消息撤回事件父类
 */
abstract class MiraiMessageRecallEvent<out MRE: MessageRecallEvent>(event: MRE): MiraiEventGet<MRE>(event) {
    private val recallMessageId by lazy { "${event.messageId}.${event.messageInternalId}.${event.messageTime}" }
    protected val authorId by lazy { event.authorId.toString() }
    /** 获取ID, 一般用于消息类型判断  */
    override fun getId(): String = recallMessageId
    /** 时间 */
    override val onTime: Long by lazy { event.messageTime.toLong() }
}

//region 群消息撤回
/**
 * 群消息撤回
 */
open class MiraiGroupRecall(event: MessageRecallEvent.GroupRecall): MiraiMessageRecallEvent<MessageRecallEvent.GroupRecall>(event), GroupMsgDelete {

    private val recallMessage = RecallCache.get("${event.messageId}.${event.messageInternalId}.${event.messageTime}", event.bot.id)

    override var eventMsg: String? = MiraiCodeFormatUtils.mi2cq(recallMessage?.originalMessage)

    private val groupId by lazy { event.group.id.toString() }
    private val operatorId by lazy { event.getOperatorId().toString() }

    /** 被操作者的QQ号, 即被撤回消息的人的QQ号  */
    override fun getBeOperatedQQ(): String = authorId
    /**
     * 获取消息中存在的群号信息
     */
    override fun getGroupCode(): String = groupId

    override fun getGroupCodeNumber(): Long = event.group.id
    override fun getQQCodeNumber(): Long = event.authorId

    /** 操作者的QQ号，即执行撤回操作的人的QQ号  */
    override fun getOperatorQQ(): String = operatorId
}

/** 获取操作者Id */
internal fun MessageRecallEvent.GroupRecall.getOperatorId(): Long = this.operator?.id ?: this.bot.id
//endregion
//region 私信消息撤回
/**
 * 私信消息撤回
 */
open class MiraiPrivateRecall(event: MessageRecallEvent.FriendRecall): MiraiMessageRecallEvent<MessageRecallEvent.FriendRecall>(event), PrivateMsgDelete {
    /**
     * 获取QQ号信息。
     */
    override fun getQQCode(): String = authorId
    override fun getQQCodeNumber(): Long = event.authorId
}
//endregion
//endregion

//region 禁言相关
/**
 * 禁言相关父类
 */
sealed class MiraiBanEvent<out GE: GroupEvent>(event: GE): MiraiEventGet<GE>(event), GroupBan {
    abstract val muteEventId: String
    private val groupId by lazy { event.group.id.toString() }
    /** 群号  */
    override fun getGroup() = groupId
    override fun getGroupCodeNumber(): Long = event.group.id
    override fun getId(): String = muteEventId
}

/**
 * 执行禁言
 */
abstract class Mute<out GE: GroupEvent>(event: GE): MiraiBanEvent<GE>(event) {
    override val muteEventId = "MUTE_$event"
    override fun getBanType(): GroupBanType = GroupBanType.BAN
}
/**
 * 取消禁言
 */
abstract class Unmute<out GE: GroupEvent>(event: GE): MiraiBanEvent<GE>(event) {
    override val muteEventId = "UN_MUTE_$event"
    override fun getBanType(): GroupBanType = GroupBanType.LIFT_BAN
}

//region 禁言-获取操作者
internal fun MemberMuteEvent.getOperatorId() = this.operator?.id ?: this.bot.id
internal fun MemberUnmuteEvent.getOperatorId() = this.operator?.id ?: this.bot.id
internal fun BotMuteEvent.getOperatorId() = this.operator.id
internal fun BotUnmuteEvent.getOperatorId() = this.operator.id
//endregion

//region 群成员被禁言
/**
 * 群成员被禁言
 */
open class MiraiMemberMuteEvent(event: MemberMuteEvent): Mute<MemberMuteEvent>(event) {
    private val memberId by lazy { event.member.id.toString() }
    private val operatorId by lazy { event.getOperatorId().toString() }
    private val durationSeconds by lazy { event.durationSeconds.toLong() }
    /** 被操作者的QQ号  */
    override fun getBeOperatedQQ(): String = memberId
    override fun getCodeNumber(): Long = event.member.id
    /** 禁言时长-秒  */
    override fun time(): Long = durationSeconds
    /** 操作者的QQ号  */
    override fun getOperatorQQ(): String = operatorId
}
//endregion
//region 群成员解除禁言
/**
 * 群成员解除禁言
 */
open class MiraiMemberUnmuteEvent(event: MemberUnmuteEvent): Unmute<MemberUnmuteEvent>(event) {
    private val memberId by lazy { event.member.id.toString() }
    private val operatorId by lazy { event.getOperatorId().toString() }
    /** 被操作者的QQ号  */
    override fun getBeOperatedQQ(): String = memberId
    override fun getCodeNumber(): Long = event.member.id
    /** 禁言时长-秒  */
    override fun time(): Long = 0
    /** 操作者的QQ号  */
    override fun getOperatorQQ(): String = operatorId
}
//endregion

//region bot被禁言
/**
 * bot被禁言
 */
open class MiraiBotMuteEvent(event: BotMuteEvent): Mute<BotMuteEvent>(event) {
    private val memberId by lazy { event.bot.id.toString() }
    private val operatorId by lazy { event.getOperatorId().toString() }
    private val durationSeconds by lazy { event.durationSeconds.toLong() }
    /** 被操作者的QQ号  */
    override fun getBeOperatedQQ(): String = memberId
    override fun getCodeNumber(): Long = event.bot.id
    /** 禁言时长-秒  */
    override fun time(): Long = durationSeconds
    /** 操作者的QQ号  */
    override fun getOperatorQQ(): String = operatorId
}
//endregion
//region bot解除禁言
/**
 * bot解除禁言
 */
open class MiraiBotUnmuteEvent(event:BotUnmuteEvent): Unmute<BotUnmuteEvent>(event) {
    private val memberId by lazy { event.bot.id.toString() }
    private val operatorId by lazy { event.getOperatorId().toString() }
    /** 被操作者的QQ号  */
    override fun getBeOperatedQQ(): String = memberId
    override fun getCodeNumber(): Long = event.bot.id
    /** 禁言时长-秒  */
    override fun time(): Long = 0
    /** 操作者的QQ号  */
    override fun getOperatorQQ(): String = operatorId
}
//endregion


//endregion


