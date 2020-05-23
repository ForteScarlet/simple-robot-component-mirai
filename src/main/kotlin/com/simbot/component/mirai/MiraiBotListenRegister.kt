package com.simbot.component.mirai

import com.forte.qqrobot.MsgProcessor
import com.forte.qqrobot.listener.result.ListenResult
import com.simbot.component.mirai.messages.*
import net.mamoe.mirai.event.events.*
import net.mamoe.mirai.event.subscribe
import net.mamoe.mirai.event.subscribeAlways
import net.mamoe.mirai.event.subscribeFriendMessages
import net.mamoe.mirai.event.subscribeMessages
import net.mamoe.mirai.message.FriendMessageEvent
import net.mamoe.mirai.message.GroupMessageEvent
import net.mamoe.mirai.message.MessageEvent
import net.mamoe.mirai.message.TempMessageEvent

/**
 * 为bot注册对应监听的工具类，为Java用
 */
object MiraiBotListenRegister {
    /** 注册监听 */
    @JvmStatic
    fun register(info: MiraiBotInfo, msgProcessor: MsgProcessor) {
        info.register(msgProcessor)
    }
}


//region 响应判断

/**
 * 判断响应结果是否为同意 `agree` / `accept`
 */
internal fun Any.isAgree(): Boolean = (this is Boolean && this) || this == "agree" || this == "accept"

/**
 * 判断响应结果是否为拒绝 `reject` / `refuse`
 */
internal fun Any.isReject(): Boolean = (this is Boolean && !this) || this == "reject" || this == "refuse"

/**
 * 判断响应结果是否为忽略 `ignore`
 */
internal fun Any.isIgnore(): Boolean = this == "ignore"

//endregion

//region 处理监听消息
internal fun <M : MiraiBaseMsgGet<*>> M.onMsg(msgProcessor: MsgProcessor): ListenResult<*>? = msgProcessor.onMsgSelected(this)
//endregion


//region ListenResult快捷回复处理

/**
 * Listen result 快捷回复
 * message event相关的
 */
suspend fun ListenResult<*>?.quickReplyMessage(event: MessageEvent) {
    val result = this?.result() ?: return
    if (result is Map<*, *>) {
        val reply = result["reply"]
        if (reply != null) {
            event.reply(reply.toString().toWholeMessage(event.subject))
        }
    }
}

/**
 * Listen result 快捷处理，invited join request相关的
 */
suspend fun ListenResult<*>?.quickReply(event: BotInvitedJoinGroupRequestEvent) {
    val result = this?.result() ?: return
    if (result is Map<*, *>) {
        // reply: agree/accpet and ignore/reject
        val reply = result["reply"]
        if (reply != null) {
            // 同意申请
            if (reply.isAgree()) {
                // accept
                event.accept()
                // remove cache
                RequestCache.removeFriendRequest(event.botId(), event.toKey())
            } else if (reply.isReject() || reply.isIgnore()) {
                // 不同意申请，即忽略请求
                event.ignore()
                // remove cache
                RequestCache.removeFriendRequest(event.botId(), event.toKey())
            }
        }
    }
}

/**
 * Listen result 快捷处理，member join request相关的
 */
suspend fun ListenResult<*>?.quickReply(event: MemberJoinRequestEvent) {
    val result = this?.result() ?: return
    if (result is Map<*, *>) {
        // accept or agree
        val reply = result["reply"]
        if (reply != null) {
            // 同意申请
            when {
                reply.isAgree() -> {
                    // accept
                    event.accept()
                    // remove cache
                    RequestCache.removeFriendRequest(event.botId(), event.toKey())
                }
                reply.isReject() -> {
                    // 不同意申请
                    event.reject()
                    // remove cache
                    RequestCache.removeFriendRequest(event.botId(), event.toKey())
                }
                reply.isIgnore() -> {
                    // 忽略
                    event.ignore()
                    // remove cache
                    RequestCache.removeFriendRequest(event.botId(), event.toKey())
                }
            }
        }
    }
}

/**
 * Listen result 快捷处理，new friend request相关的
 */
suspend fun ListenResult<*>?.quickReply(event: NewFriendRequestEvent) {
    val result = this?.result() ?: return
    if (result is Map<*, *>) {
        // accept or agree
        val reply = result["reply"]
        if (reply != null) {
            // 同意申请
            when {
                reply.isAgree() -> {
                    // accept
                    event.accept()
                    // remove cache
                    RequestCache.removeFriendRequest(event.botId(), event.toKey())
                }
                reply.isReject() -> {
                    // 不同意申请
                    event.reject()
                    // remove cache
                    RequestCache.removeFriendRequest(event.botId(), event.toKey())
                }
            }
        }
    }
}


//endregion


/**
 * 注册监听
 *
 */
fun MiraiBotInfo.register(msgProcessor: MsgProcessor) {
    val bot = this.bot


    //region 消息监听相关事件

    bot.subscribeMessages {
        this.always {
            val result = when (this) {
                // 好友消息
                is FriendMessageEvent -> {
                    MiraiFriendMsg(this).onMsg(msgProcessor)
                }
                // 群消息
                is GroupMessageEvent -> {
                    MiraiGroupMsg(this).onMsg(msgProcessor)
                }
                // 群临时会话消息
                is TempMessageEvent -> {
                    MiraiTempMsg(this).onMsg(msgProcessor)
                }
                // 其他类型, 没其他类型了吧？
                else -> null
            }
            // try to quick reply
            result.quickReplyMessage(this)
        }

        //endregion


        // region 申请相关事件

        // 被邀请入群事件监听
        bot.subscribeAlways<BotInvitedJoinGroupRequestEvent> {
            val result = MiraiBotInvitedJoinGroupRequestEvent(this).onMsg(msgProcessor)
            // try to quick reply
            result.quickReply(this)
        }

        // 其他人申请入群事件监听
        bot.subscribeAlways<MemberJoinRequestEvent> {
            val result = MiraiMemberJoinRequestEvent(this).onMsg(msgProcessor)
            // try to quick reply
            result.quickReply(this)
        }

        // 新好友申请事件监听
        bot.subscribeAlways<NewFriendRequestEvent> {
            val result = MiraiNewFriendRequestEvent(this).onMsg(msgProcessor)
            // try to quick reply
            result.quickReply(this)
        }

        // endregion

        //region 群成员/好友增减事件

        //region 成功添加了一个新好友
        bot.subscribeAlways<FriendAddEvent> {
            MiraiFriendAddEvent(this).onMsg(msgProcessor)
        }
        //endregion
        //region 群成员增加事件
        bot.subscribeAlways<MemberJoinEvent> {
            MiraiMemberJoinEvent(this).onMsg(msgProcessor)
        }
        //endregion
        //region 群成员减少事件
        bot.subscribeAlways<MemberLeaveEvent> {
            MiraiMemberLeaveEvent(this).onMsg(msgProcessor)
        }
        //endregion

        //endregion

        //region 权限变动事件
        // 成员权限变动
        bot.subscribeAlways<MemberPermissionChangeEvent> {
            MiraiMemberPermissionChangeEvent(this).onMsg(msgProcessor)
        }
        // bot权限变动
        bot.subscribeAlways<BotGroupPermissionChangeEvent> {
            MiraiBotGroupPermissionChangeEvent(this).onMsg(msgProcessor)
        }

        //endregion

        //region 消息撤回事件
        bot.subscribeAlways<MessageRecallEvent> {
            when (this) {
                //region 群消息撤回
                is MessageRecallEvent.GroupRecall -> {
                    MiraiGroupRecall(this).onMsg(msgProcessor)
                }
                //endregion
                //region 好友消息撤回
                is MessageRecallEvent.FriendRecall -> {
                    MiraiPrivateRecall(this).onMsg(msgProcessor)
                }
                //endregion
            }
        }
        //endregion

        //region 禁言相关

        //region 群友被禁言
        bot.subscribeAlways<MemberMuteEvent> {
            MiraiMemberMuteEvent(this).onMsg(msgProcessor)
        }
        //endregion
        //region 群友被解除禁言
        bot.subscribeAlways<MemberUnmuteEvent> {
            MiraiMemberUnmuteEvent(this).onMsg(msgProcessor)
        }
        //endregion
        //region bot被禁言
        bot.subscribeAlways<BotMuteEvent> {
            MiraiBotMuteEvent(this).onMsg(msgProcessor)
        }
        //endregion
        //region bot被取消禁言
        bot.subscribeAlways<BotUnmuteEvent> {
            MiraiBotUnmuteEvent(this).onMsg(msgProcessor)
        }
        //endregion
        //endregion

    }


}