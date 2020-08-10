/*
 *
 *  Copyright (c) 2020. ForteScarlet All rights reserved.
 *  Project  component-mirai
 *  File     Reply.kt
 *
 *  You can contact the author through the following channels:
 *  github https://github.com/ForteScarlet
 *  gitee  https://gitee.com/ForteScarlet
 *  email  ForteScarlet@163.com
 *  QQ     1149159218
 *
 *  The Mirai code is copyrighted by mamoe-mirai
 *  you can see mirai at https://github.com/mamoe/mirai
 *
 *
 *
 */

@file:Suppress("unused")

package com.simbot.component.mirai.messages

import com.simbot.component.mirai.collections.SingletonMap
import net.mamoe.mirai.event.events.BotInvitedJoinGroupRequestEvent
import net.mamoe.mirai.event.events.MemberJoinRequestEvent
import net.mamoe.mirai.event.events.NewFriendRequestEvent
import net.mamoe.mirai.message.MessageEvent

/**
 * 提供一些通过返回值进行快捷回复的功能
 *
 * **此类构建完成后不可变。**
 *
 * 目前版本中，当返回值是Map类型的的时候才会快捷回复。
 * 此类终将会被核心整合，所以不要过分依赖此类。
 *
 * 目前存在的可以使用的快捷回复：
 * reply 快速回复消息, 通过[MessageEvent]的[MessageEvent.reply]方法进行回复。
 * 详见[com.simbot.component.mirai.quickReplyMessage]
 *
 * reply 快速同意/拒绝/忽略，一般事件为:
 * - [BotInvitedJoinGroupRequestEvent]
 * - [MemberJoinRequestEvent]
 * - [NewFriendRequestEvent]
 * 的时候生效。
 * 详见[com.simbot.component.mirai.quickReply]
 *
 *
 * @see com.simbot.component.mirai.quickReplyMessage
 * @see com.simbot.component.mirai.quickReply
 */
open class Reply
protected constructor(map: Map<String, Any> = mapOf()): Map<String, Any> by map {


    companion object {
        /**
         * 获得builder
         */
        @JvmStatic
        val builder get() = Builder()

        /**
         * 获得消息回复的Reply
         */
        @JvmStatic
        @JvmOverloads
        fun getMessageReply(reply: String, quote: Boolean = true, at: Boolean = true): Reply {
            val builder = Builder().reply(reply)
            if(quote){
               builder.quote()
            }
            if(at){
                builder.at()
            }
            return builder.build()
        }

        /**
         * @see AgreeReply
         */
        @JvmStatic
        val agreeReply = AgreeReply

        /**
         * @see IgnoreReply
         */
        @JvmStatic
        val ignoreReply = IgnoreReply

        /**
         * @see RejectReply
         */
        @JvmStatic
        val rejectReply = RejectReply
    }

    /**
     * [Reply] builder
     */
    class Builder {
        private val map: MutableMap<String, Any> = mutableMapOf()

        /**
         * 回复消息
         * @see com.simbot.component.mirai.quickReplyMessage
         */
        fun reply(reply: String): Builder{
            map["reply"] = reply
            return this
        }

        /**
         * 回复的时候是否进行引用
         * 需要在[reply]之后使用，且只支持在消息回复中生效
         */
        fun quote(): Builder {
            map["quote"] = true
            return this
        }

        fun quote(id: String): Builder {
            map["quote"] = id
            return this
        }

        /**
         * at回复的人。只会在群聊生效
         */
        fun at(): Builder {
            map["at"] = true
            return this
        }


        /**
         * 同意申请
         * @see com.simbot.component.mirai.quickReply
         */
        @Deprecated("use [AgreeReply]", replaceWith = ReplaceWith("AgreeReply", "com.simbot.component.mirai.messages.AgreeReply"))
        fun agree(): Builder {
            map["reply"] = true
            return this
        }

        /**
         * 拒绝申请
         * @see RejectReply
         * @see com.simbot.component.mirai.quickReply
         */
        @Deprecated("use [RejectReply]", replaceWith = ReplaceWith("RejectReply", "com.simbot.component.mirai.messages.RejectReply"))
        fun reject(): Builder {
            map["reply"] = false
            return this
        }

        /**
         * 忽略申请
         * @see com.simbot.component.mirai.quickReply
         */
        @Deprecated("use [IgnoreReply]", replaceWith = ReplaceWith("IgnoreReply", "com.simbot.component.mirai.messages.IgnoreReply"))
        fun ignore(): Builder {
            map["reply"] = "ignore"
            return this
        }


        /**
         * 构建一个[Reply]
         */
        fun build(): Reply = Reply(map)
    }
}

/** 忽略请求的[Reply] */
object IgnoreReply: Reply(SingletonMap("reply", "ignore"))
/** 拒绝请求的[Reply] */
object RejectReply: Reply(SingletonMap("reply", false))
/** 同意请求的[Reply] */
object AgreeReply : Reply(SingletonMap("reply", true))


