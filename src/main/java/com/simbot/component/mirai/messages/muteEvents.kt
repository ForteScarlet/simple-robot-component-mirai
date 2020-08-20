/*
 *
 *  * Copyright (c) 2020. ForteScarlet All rights reserved.
 *  * Project  component-mirai
 *  * File     muteEvents.kt
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

import com.forte.qqrobot.beans.messages.msgget.GroupBan
import com.forte.qqrobot.beans.messages.types.GroupBanType
import net.mamoe.mirai.event.events.*


//region 禁言相关
/**
 * 禁言相关父类
 */
sealed class MiraiBanEvent<out GE: GroupEvent>(event: GE): MiraiEventGet<GE>(event), GroupBan {
    abstract val muteEventId: String
    private val groupId = event.group.id.toString()
    /** 群号  */
    override fun getGroup(): String = groupId
    override fun getGroupCodeNumber(): Long = event.group.id
    override fun getGroupHeadUrl(): String = event.group.avatarUrl
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
    private val memberId = event.member.id.toString()
    private val operatorId = event.getOperatorId().toString()
    private val durationSeconds = event.durationSeconds.toLong()
    /** 被操作者的QQ号  */
    override fun getBeOperatedQQ(): String = memberId
    override fun getCodeNumber(): Long = event.member.id
    override fun getQQHeadUrl(): String = event.member.avatarUrl
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
    private val memberId = event.member.id.toString()
    private val operatorId = event.getOperatorId().toString()
    /** 被操作者的QQ号  */
    override fun getBeOperatedQQ(): String = memberId
    override fun getCodeNumber(): Long = event.member.id
    override fun getQQHeadUrl(): String = event.member.avatarUrl
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
    private val memberId = event.bot.id.toString()
    private val operatorId = event.getOperatorId().toString()
    private val durationSeconds = event.durationSeconds.toLong()
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
open class MiraiBotUnmuteEvent(event: BotUnmuteEvent): Unmute<BotUnmuteEvent>(event) {
    private val memberId = event.bot.id.toString()
    private val operatorId = event.getOperatorId().toString()
    /** 被操作者的QQ号  */
    override fun getBeOperatedQQ(): String = memberId
    override fun getCodeNumber(): Long = event.bot.id
    /** 禁言时长-秒  */
    override fun time(): Long = 0
    /** 操作者的QQ号  */
    override fun getOperatorQQ(): String = operatorId
}
//endregion


//region 全体禁言事件
/**
 * 全体禁言事件.
 * 当[getBanType]为[GroupBanType.BAN]的时候，代表开启全体禁言，反之代表关闭
 * 其中:
 * - 全体禁言事件的[getBeOperatedQQ]值必定为null.
 * - 全体禁言事件的[time]值必定为-1.
 */
open class MiraiGroupMuteAllEvent(event: GroupMuteAllEvent):  MiraiBanEvent<GroupMuteAllEvent>(event) {

    /** 禁言类型 */
    private val _banType: GroupBanType = if(event.new) GroupBanType.BAN else GroupBanType.LIFT_BAN

    /** 操作者code，可能是bot自己 */
    private val operatorCode = event.operatorOrBot.id.toString()

    /** 根据新的状态判断id */
    override val muteEventId: String = if(_banType.isBan) "GROUP_MUTE_$event" else "GROUP_UN_MUTE_$event"

    /** 获取禁言类型：禁言/解除禁言  */
    override fun getBanType(): GroupBanType = _banType

    /** 操作者的QQ号  */
    override fun getOperatorQQ(): String = operatorCode

    override fun getCodeNumber(): Long = event.bot.id

    /** 被操作者的QQ号  */
    override fun getBeOperatedQQ(): String? = null

    /** 禁言时长-秒  */
    override fun time(): Long = -1

}
//endregion

// alt shift Z



//endregion