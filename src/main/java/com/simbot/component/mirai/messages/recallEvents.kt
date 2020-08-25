/*
 *
 *  * Copyright (c) 2020. ForteScarlet All rights reserved.
 *  * Project  component-mirai
 *  * File     recallEvents.kt
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

import com.forte.qqrobot.beans.messages.msgget.GroupMsgDelete
import com.forte.qqrobot.beans.messages.msgget.PrivateMsgDelete
import com.simbot.component.mirai.CacheMaps
import com.simbot.component.mirai.utils.MiraiCodeFormatUtils
import net.mamoe.mirai.event.events.MessageRecallEvent


//region 撤回事件
/**
 * 消息撤回事件父类
 */
abstract class MiraiMessageRecallEvent<out MRE: MessageRecallEvent>(event: MRE): MiraiEventGet<MRE>(event) {
    private val recallMessageId = "${event.messageId}.${event.messageInternalId}.${event.messageTime}"
    protected val authorId = event.authorId.toString()
    private val eventMessageTime: Long = event.messageTime.toLong()
    /** 获取ID, 一般用于消息类型判断  */
    override fun getId(): String = recallMessageId
    /** 时间 */
    override val onTime: Long = eventMessageTime
}

//region 群消息撤回
/**
 * 群消息撤回
 */
open class MiraiGroupRecall(event: MessageRecallEvent.GroupRecall, private val cacheMaps: CacheMaps): MiraiMessageRecallEvent<MessageRecallEvent.GroupRecall>(event), GroupMsgDelete {

    private val recallMessage = cacheMaps.recallCache.get("${event.messageId}.${event.messageInternalId}.${event.messageTime}", event.bot.id)

    override var eventMsg: String? = MiraiCodeFormatUtils.mi2cq(recallMessage?.originalMessage, cacheMaps)

    private val groupId = event.group.id.toString()
    private val operatorId = event.getOperatorId().toString()

    /** 被操作者的QQ号, 即被撤回消息的人的QQ号  */
    override fun getBeOperatedQQ(): String = authorId
    /**
     * 获取消息中存在的群号信息
     */
    override fun getGroupCode(): String = groupId

    override fun getGroupCodeNumber(): Long = event.group.id
    override fun getCodeNumber(): Long = event.authorId

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
    override fun getCodeNumber(): Long = event.authorId
}
//endregion
//endregion