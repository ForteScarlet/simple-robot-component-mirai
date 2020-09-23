/*
 *
 * Copyright (c) 2020. ForteScarlet All rights reserved.
 * Project  component-mirai
 * File     RecallEvents.kt
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

import com.forte.qqrobot.beans.messages.msgget.GroupMsgDelete
import com.forte.qqrobot.beans.messages.msgget.PrivateMsgDelete
import com.simbot.component.mirai.CacheMaps
import com.simbot.component.mirai.utils.MiraiCodeFormatUtils
import net.mamoe.mirai.event.events.MessageRecallEvent
import net.mamoe.mirai.message.data.MessageSource


//region 撤回事件
/**
 * 消息撤回事件父类
 */
abstract class MiraiMessageRecallEvent<out MRE: MessageRecallEvent>(event: MRE): MiraiEventGet<MRE>(event) {
    private val recallMessageId = "${event.messageId}.${event.messageInternalId}"
    protected val authorId: String = event.authorId.toString()
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
open class MiraiGroupRecall(event: MessageRecallEvent.GroupRecall, cacheMaps: CacheMaps): MiraiMessageRecallEvent<MessageRecallEvent.GroupRecall>(event), GroupMsgDelete {

    private val recallMessage: MessageSource? = cacheMaps.recallCache.get("${event.messageId}.${event.messageInternalId}", event.bot.id)

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
open class MiraiPrivateRecall(event: MessageRecallEvent.FriendRecall, cacheMaps: CacheMaps): MiraiMessageRecallEvent<MessageRecallEvent.FriendRecall>(event), PrivateMsgDelete {

    private val recallMessage: MessageSource? = cacheMaps.recallCache.get("${event.messageId}.${event.messageInternalId}", event.bot.id)

    override var eventMsg: String? = MiraiCodeFormatUtils.mi2cq(recallMessage?.originalMessage, cacheMaps)

    /**
     * 获取消息中存在的群号信息
     */
    override fun getCodeNumber(): Long = event.authorId

    /**
     * 获取QQ号信息。
     * 假如一个消息封装中存在多个QQ号信息，例如同时存在处理者与被处理者，一般情况下我们认为其返回值为被处理者。
     * @see .getCode
     */
    override fun getQQCode(): String = event.authorId.toString()
}
//endregion
//endregion