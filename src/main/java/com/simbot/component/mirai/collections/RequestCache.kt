/*
 *
 * Copyright (c) 2020. ForteScarlet All rights reserved.
 * Project  component-mirai
 * File     RequestCache.kt
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

package com.simbot.component.mirai.collections

import com.simbot.component.mirai.LRUCacheMap
import com.simbot.component.mirai.RequestCacheConfiguration
import net.mamoe.mirai.event.events.BotInvitedJoinGroupRequestEvent
import net.mamoe.mirai.event.events.MemberJoinRequestEvent
import net.mamoe.mirai.event.events.NewFriendRequestEvent
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger


/** 缓存请求相关消息用的id，每一个bot都有一个Map */
open class RequestCache(
        /**
         * 清理缓存临界值, 当计数器达到1000则触发一次清理
         */
        check: Int,
        /**
         * 默认缓存5分钟
         */
        private val cacheTime: Long,
        /**
         * 内部缓存的初始容量
         */
        private val friendRequestInitialCapacity: Int,
        /**
         * 缓存的最大容量
         */
        private val friendRequestMax: Long,
        /**
         * 内部缓存的初始容量
         */
        private val joinRequestInitialCapacity: Int,
        /**
         * 缓存的最大容量
         */
        private val joinRequestMax: Long
) {
    constructor(config: RequestCacheConfiguration): this(config.check, config.cacheTime, config.friendRequestInitialCapacity, config.friendRequestMax, config.joinRequestInitialCapacity, config.joinRequestMax)

    /**
     * [CacheCheck]
     * 代表检测当前计数是否可以进行清理
     */
    private val cacheCheck: CacheCheck = CacheCheck(check)

    /** botCacheMap */
//    @JvmStatic
    private val friendRequestCacheMap: MutableMap<Long, LRUCacheMap<String, NewFriendRequestEvent>> = ConcurrentHashMap()

    /** 可能是[MemberJoinRequestEvent] 其他人入群 或者 [BotInvitedJoinGroupRequestEvent] 被邀请入群 */
//    @JvmStatic
    private val joinRequestCacheMap: MutableMap<Long, LRUCacheMap<String, Any>> = ConcurrentHashMap()

    /**
     * [friendRequestCacheMap] 使用的缓存
     */
    private val friendRequestLruCacheMap: LRUCacheMap<String, NewFriendRequestEvent>
        get() = LRUCacheMap(friendRequestInitialCapacity, friendRequestMax)

    /**
     * [MemberJoinRequestEvent] 使用的缓存
     */
    private val joinRequestLruCacheMap: LRUCacheMap<String, Any>
        get() = LRUCacheMap(joinRequestInitialCapacity, joinRequestMax)



    /** 计数器，当计数器达到100的时候，触发缓存清除 */
//    @JvmStatic
    private val counter: AtomicInteger = AtomicInteger(0)

    /** 缓存friend request，消息记录1小时 */
//    @JvmStatic
    open fun cache(request: NewFriendRequestEvent): String {
        // bot id
        val id = request.botId()
        val key = request.toKey()
        friendRequestCacheMap.cache(id, key, request) { friendRequestLruCacheMap }
        return key
    }


    /** 缓存 join request，消息记录1小时 */
//    @JvmStatic
    open fun cache(request: MemberJoinRequestEvent): String {
        // bot id
        val id = request.botId()
        val key = request.toKey()
        joinRequestCacheMap.cache(id, key, request) { joinRequestLruCacheMap }
        return key
    }

    /** 缓存invited join request，消息记录1小时 */
//    @JvmStatic
    open fun cache(request: BotInvitedJoinGroupRequestEvent): String {
        // bot id
        val id = request.botId()
        val key = request.toKey()
        // 缓存30分钟
        joinRequestCacheMap.cache(id, key, request) { joinRequestLruCacheMap }
        return key
    }

    /** 进行缓存 */
    private inline fun <V> MutableMap<Long, LRUCacheMap<String, V>>.cache(botId: Long, key: String, value: V, crossinline lruFactory: () -> LRUCacheMap<String, V>){
        val cacheMap = this.computeIfAbsent(botId) { lruFactory() }
        // 缓存30分钟
        cacheMap.put(key, value, LocalDateTime.now().plus(cacheTime, ChronoUnit.MILLIS))

        // 计数+1, 如果大于100，清除缓存
//        if(check > 0 && counter.addAndGet(1) >= check){
        if(cacheCheck.clearCheck(counter.addAndGet(1))){
            counter.set(0)
            synchronized(this){
                this.forEach { it.value.detect() }
            }
        }
    }

    /** 获取friend request缓存 */
//    @JvmStatic
    open fun getFriendRequest(botId: Long, key: String): NewFriendRequestEvent? {
        // 获取缓存值，可能为null
        return friendRequestCacheMap[botId]?.get(key)
    }

    /**
     * 获取join request缓存
     * 可能是[MemberJoinRequestEvent] 其他人入群 或者 [BotInvitedJoinGroupRequestEvent] 被邀请入群
     *
     */
//    @JvmStatic
    open fun getJoinRequest(botId: Long, key: String): Any? {
        // 获取缓存值，可能为null
        return joinRequestCacheMap[botId]?.get(key)
    }

    /** 移除一个friend request */
//    @JvmStatic
    open fun removeFriendRequest(botId: Long, key: String): Any? {
        return friendRequestCacheMap[botId]?.remove(key)
    }

    /** 移除一个join request */
//    @JvmStatic
    open fun removeJoinRequest(botId: Long, key: String): Any? {
        return joinRequestCacheMap[botId]?.remove(key)
    }
}



/** 转化为key */
fun BotInvitedJoinGroupRequestEvent.toKey(): String = this.eventId.toString()

/** 转化为key */
fun MemberJoinRequestEvent.toKey(): String = this.eventId.toString()

/** 转化为key */
fun NewFriendRequestEvent.toKey(): String = this.eventId.toString()

/** 获取botid */
fun BotInvitedJoinGroupRequestEvent.botId(): Long = this.bot.id

/** 获取botid */
fun MemberJoinRequestEvent.botId(): Long = this.bot.id

/** 获取botid */
fun NewFriendRequestEvent.botId(): Long = this.bot.id

