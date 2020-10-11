/*
 *
 * Copyright (c) 2020. ForteScarlet All rights reserved.
 * Project  component-mirai
 * File     MiraiBotListenRegister.kt
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
@file:JvmName("MiraiBotListenRegister")
package com.simbot.component.mirai

import com.forte.qqrobot.MsgProcessor
import com.forte.qqrobot.beans.messages.msgget.MsgGet
import com.forte.qqrobot.listener.result.ListenResult
import com.simbot.component.mirai.collections.botId
import com.simbot.component.mirai.collections.toImgVoiceCacheKey
import com.simbot.component.mirai.collections.toKey
import com.simbot.component.mirai.messages.*
import com.simbot.component.mirai.utils.toWholeMessage
import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.Friend
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.event.Listener
import net.mamoe.mirai.event.events.*
import net.mamoe.mirai.event.subscribeAlways
import net.mamoe.mirai.message.FriendMessageEvent
import net.mamoe.mirai.message.GroupMessageEvent
import net.mamoe.mirai.message.MessageEvent
import net.mamoe.mirai.message.TempMessageEvent
import net.mamoe.mirai.message.data.*


//region 响应判断

/**
 * 判断响应结果是否为同意 `agree` / `accept`
 */
private fun Any.isAgree(): Boolean = (this is Boolean && this) || this == "agree" || this == "accept"

/**
 * 判断响应结果是否为拒绝 `reject` / `refuse`
 */
private fun Any.isReject(): Boolean = (this is Boolean && !this) || this == "reject" || this == "refuse"

/**
 * 判断响应结果是否为忽略 `ignore`
 */
private fun Any.isIgnore(): Boolean = this == "ignore"

//endregion

//region 处理监听消息
private fun <M : MsgGet> M.onMsg(msgProcessor: MsgProcessor): ListenResult<*>? = msgProcessor.onMsgSelected(this)
//endregion


//region ListenResult快捷回复处理

/**
 * Listen result 快捷回复
 * message event相关的
 */
private suspend fun ListenResult<*>?.quickReplyMessage(event: MessageEvent, cacheMaps: CacheMaps) {
    val result = this?.result() ?: return
    if (result is Map<*, *>) {
        val reply = result["reply"]
        val quote = result["quote"]
        val at = result["at"]
        if (reply != null) {
            var replyMsg = if(reply is Message) {
                reply.asMessageChain()
            }else {
                reply.toString().toWholeMessage(event.subject, cacheMaps).asMessageChain()
            }

            // 是群聊的时候
            if(event is GroupMessageEvent){
                // at送信人
                if(at is Boolean && at){
                    replyMsg = At(event.sender) + replyMsg
                }
                // 引用回复
                if(quote is Boolean && quote){
                    replyMsg = QuoteReply(event.source) + replyMsg
                }else if(quote is String){
                    val quoteSource = cacheMaps.recallCache.get(quote, event.bot.id)
                    if(quoteSource != null){
                        replyMsg = QuoteReply(quoteSource) + replyMsg
                    }
                }
            }
            // reply
            event.reply(replyMsg)
        }
    }
}

/**
 * Listen result 快捷处理，invited join request相关的
 */
private suspend fun ListenResult<*>?.quickReply(event: BotInvitedJoinGroupRequestEvent, cacheMaps: CacheMaps) {
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
                cacheMaps.requestCache.removeFriendRequest(event.botId(), event.toKey())
            } else if (reply.isReject() || reply.isIgnore()) {
                // 不同意申请，即忽略请求
                event.ignore()
                // remove cache
                cacheMaps.requestCache.removeFriendRequest(event.botId(), event.toKey())
            }
        }
    }
}

/**
 * Listen result 快捷处理，member join request相关的
 */
private suspend fun ListenResult<*>?.quickReply(event: MemberJoinRequestEvent, cacheMaps: CacheMaps) {
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
                    cacheMaps.requestCache.removeFriendRequest(event.botId(), event.toKey())
                }
                reply.isReject() -> {
                    // 不同意申请
                    event.reject()
                    // remove cache
                    cacheMaps.requestCache.removeFriendRequest(event.botId(), event.toKey())
                }
                reply.isIgnore() -> {
                    // 忽略
                    event.ignore()
                    // remove cache
                    cacheMaps.requestCache.removeFriendRequest(event.botId(), event.toKey())
                }
            }
        }
    }
}

/**
 * Listen result 快捷处理，new friend request相关的
 */
private suspend fun ListenResult<*>?.quickReply(event: NewFriendRequestEvent, cacheMaps: CacheMaps) {
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
                    cacheMaps.requestCache.removeFriendRequest(event.botId(), event.toKey())
                }
                reply.isReject() -> {
                    // 不同意申请
                    event.reject()
                    // remove cache
                    cacheMaps.requestCache.removeFriendRequest(event.botId(), event.toKey())
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
internal fun Bot.register(msgProcessor: MsgProcessor, cacheMaps: CacheMaps, specialListen: Boolean = false) {
    //region 消息监听相关事件

    registerListenerAlways<MessageEvent> {
        // val topType: String = if ( this is GroupMessageEvent ) "GROUP_" else "PRIVATE_"
        // 首先缓存此消息
        cacheMaps.recallCache.cache(this.source)
        this.message.forEach {
            if(it is Image){
                // 记录图片缓存
                cacheMaps.imageCache[it.imageId.toImgVoiceCacheKey(this)] = it
            }

            if(it is FlashImage){
                // 记录闪照的图片缓存
                with(it.image) {
                    cacheMaps.imageCache[this.imageId.toImgVoiceCacheKey(this@registerListenerAlways)] = this
                }
            }

            if(it is Voice){
                // 记录语音缓存
                cacheMaps.voiceCache[it.fileName.toImgVoiceCacheKey(this)] = it
            }
        }
        val result = when (this) {
            // 好友消息
            is FriendMessageEvent -> {
//                QQLog.debug("{0}", "mirai Friend msg: $this")
                MiraiFriendMsg(this, cacheMaps).onMsg(msgProcessor)
            }
            // 群消息
            is GroupMessageEvent -> {
                MiraiGroupMsg(this, cacheMaps).onMsg(msgProcessor)
            }
            // 群临时会话消息
            is TempMessageEvent -> {
                MiraiTempMsg(this, cacheMaps).onMsg(msgProcessor)
            }
            // 其他类型, 没其他类型了吧？
            else -> null
        }
        // try to quick reply
        result.quickReplyMessage(this, cacheMaps)
    }
    //endregion

    //region 头像戳一戳事件
    // bot被戳
    registerListenerAlways<BotNudgedEvent> {
        println(this)
        if(this.from.id != this.bot.id){
            when(this.from) {
                is Member -> MiraiBotGroupNudgedEvent(this).onMsg(msgProcessor)
                is Friend -> MiraiBotFriendNudgedEvent(this).onMsg(msgProcessor)
            }
        }
    }

    // 群里其他人被戳事件
    registerListenerAlways<MemberNudgedEvent> {
        println(this)
        if(this.from.id != this.bot.id) {
            MiraiMemberNudgedEvent(this).onMsg(msgProcessor)
        }
    }
    //endregion


    // region 申请相关事件

    // 被邀请入群事件监听
    registerListenerAlways<BotInvitedJoinGroupRequestEvent> {
        val result = MiraiBotInvitedJoinGroupRequestEvent(this, cacheMaps).onMsg(msgProcessor)
        // try to quick reply
        result.quickReply(this, cacheMaps)
    }

    // 其他人申请入群事件监听
    registerListenerAlways<MemberJoinRequestEvent> {
        val result = MiraiMemberJoinRequestEvent(this, cacheMaps).onMsg(msgProcessor)
        // try to quick reply
        result.quickReply(this, cacheMaps)
    }

    // 新好友申请事件监听
    registerListenerAlways<NewFriendRequestEvent> {
        val miraiNewFriendRequestEvent = MiraiNewFriendRequestEvent(this, cacheMaps)
        val result = miraiNewFriendRequestEvent.onMsg(msgProcessor)
        // try to quick reply
        result.quickReply(this, cacheMaps)
    }

    // endregion

    //region 群成员/好友增减事件

    //region 成功添加了一个新好友
    registerListenerAlways<FriendAddEvent> {
        MiraiFriendAddEvent(this).onMsg(msgProcessor)
    }
    //endregion
    //region 群成员增加事件
    // 群成员新增
    registerListenerAlways<MemberJoinEvent> {
        when(this){
            is MemberJoinEvent.Active -> MiraiMemberJoinEvent.Active(this).onMsg(msgProcessor)
            is MemberJoinEvent.Invite -> MiraiMemberJoinEvent.Invite(this).onMsg(msgProcessor)
        }
    }
    // bot入群
    // bot主动同意入群
    registerListenerAlways<BotJoinGroupEvent.Active> {
        MiraiBotJoinEvent.Active(this).onMsg(msgProcessor)
    }
    // bot被动拉入群
    registerListenerAlways<BotJoinGroupEvent.Invite> {
        MiraiBotJoinEvent.Invite(this).onMsg(msgProcessor)
    }
    //endregion

    //region 群成员减少事件
    // 群友减少
    registerListenerAlways<MemberLeaveEvent> {
        when(this){
            is MemberLeaveEvent.Kick -> MiraiMemberLeaveEvent.Kick(this).onMsg(msgProcessor)
            is MemberLeaveEvent.Quit -> MiraiMemberLeaveEvent.Quit(this).onMsg(msgProcessor)
        }
    }
    // bot退群
    registerListenerAlways<BotLeaveEvent> {
        when(this){
            is BotLeaveEvent.Kick -> MiraiBotLeaveEvent.Kick(this).onMsg(msgProcessor)
            is BotLeaveEvent.Active -> MiraiBotLeaveEvent.Active(this).onMsg(msgProcessor)
        }
    }
    //endregion

    //endregion

    //region 权限变动事件
    // 成员权限变动
    registerListenerAlways<MemberPermissionChangeEvent> {
        MiraiMemberPermissionChangeEvent(this).onMsg(msgProcessor)
    }
    // bot权限变动
    registerListenerAlways<BotGroupPermissionChangeEvent> {
        MiraiBotGroupPermissionChangeEvent(this).onMsg(msgProcessor)
    }

    //endregion

    //region 消息撤回事件
    registerListenerAlways<MessageRecallEvent> {
        when (this) {
            //region 群消息撤回
            is MessageRecallEvent.GroupRecall -> {
                MiraiGroupRecall(this, cacheMaps).onMsg(msgProcessor)
            }
            //endregion
            //region 好友消息撤回
            is MessageRecallEvent.FriendRecall -> {
                MiraiPrivateRecall(this, cacheMaps).onMsg(msgProcessor)
            }
            //endregion
        }
    }
    //endregion

    //region 禁言相关

    //region 群友被禁言
    registerListenerAlways<MemberMuteEvent> {
        MiraiMemberMuteEvent(this).onMsg(msgProcessor)
    }
    //endregion
    //region 群友被解除禁言
    registerListenerAlways<MemberUnmuteEvent> {
        MiraiMemberUnmuteEvent(this).onMsg(msgProcessor)
    }
    //endregion
    //region bot被禁言
    registerListenerAlways<BotMuteEvent> {
        MiraiBotMuteEvent(this).onMsg(msgProcessor)
    }
    //endregion
    //region bot被取消禁言
    registerListenerAlways<BotUnmuteEvent> {
        MiraiBotUnmuteEvent(this).onMsg(msgProcessor)
    }
    //endregion

    //region 全员禁言 GroupMuteAllEvent
    registerListenerAlways<GroupMuteAllEvent> {
        MiraiGroupMuteAllEvent(this).onMsg(msgProcessor)
    }
    //endregion


    //region 好友删除事件
    registerListenerAlways<FriendDeleteEvent> {
        MiraiFriendDeleteEvent(this).onMsg(msgProcessor)
    }
    //endregion

    // bot特殊事件
    if(specialListen){

        //region 好友更换头像事件
        registerListenerAlways<FriendAvatarChangedEvent> {
            MiraiFriendAvatarChangedEvent(this).onMsg(msgProcessor)
        }
        //endregion


        //region 好友更换昵称事件
        registerListenerAlways<FriendNickChangedEvent> {
            MiraiFriendNicknameChangedEvent(this).onMsg(msgProcessor)
        }
        //endregion


        //region 好友输入状态变更事件
        registerListenerAlways<FriendInputStatusChangedEvent> {
            MiraiFriendInputStatusChangedEvent(this).onMsg(msgProcessor)
        }
        //endregion


        //region bot离线事件
        registerListenerAlways<BotOfflineEvent> {
            MiraiBotOfflineEvent(this).onMsg(msgProcessor)
        }
        //endregion

        //region bot重新登录事件
        registerListenerAlways<BotReloginEvent> {
            MiraiBotReloginEvent(this).onMsg(msgProcessor)
        }
        //endregion


        //region 群名称变更事件
        registerListenerAlways<GroupNameChangeEvent> {
            MiraiGroupNameChangedEvent(this).onMsg(msgProcessor)
        }
        //endregion

        //region 群友群备注变更事件
        registerListenerAlways<MemberCardChangeEvent> {
            MiraiMemberRemarkChangedEvent(this).onMsg(msgProcessor)
        }
        //endregion


        //region 群友群头衔变更事件
        registerListenerAlways<MemberSpecialTitleChangeEvent> {
            MiraiMemberSpecialTitleChangedEvent(this).onMsg(msgProcessor)
        }
        //endregion



    }


}


/**
 * 整合
 */
private inline fun <reified E : BotEvent> Bot.registerListenerAlways(crossinline handler: suspend E.(E) -> Unit):
        Listener<E> {
    return this.subscribeAlways<E> {
        if(this.bot == this@registerListenerAlways){
            handler(this)
        }
    }
}