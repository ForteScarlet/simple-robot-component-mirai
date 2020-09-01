/*
 *
 * Copyright (c) 2020. ForteScarlet All rights reserved.
 * Project  component-mirai
 * File     RecallCache.kt
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
import com.simbot.component.mirai.RecallCacheConfiguration
import net.mamoe.mirai.message.MessageReceipt
import net.mamoe.mirai.message.data.MessageSource
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger


/** 缓存撤回消息用的id，每一个bot都有一个Map */
open class RecallCache(
        /**
         * 清理缓存临界值, 当计数器达到1000则触发一次清理
         */
        check: Int,
        /**
         * 默认缓存30分钟
         */
        private val cacheTime: Long,
        /**
         * 内部缓存的初始容量
         */
        private val initialCapacity: Int,

        /**
         * 缓存的最大容量
         */
        private val max: Long
) {
    /** by config */
    constructor(config: RecallCacheConfiguration): this(config.check, config.cacheTime, config.initialCapacity, config.max)

    /**
     * [CacheCheck]
     * 代表检测当前计数是否可以进行清理
     */
    private val cacheCheck: CacheCheck = CacheCheck(check)

    /** botCacheMap */
//    @JvmStatic
    private val botCacheMap: MutableMap<Long, LRUCacheMap<String, MessageSource>> = ConcurrentHashMap()

    /** 计数器，当计数器达到指定次数的时候，触发缓存清除 */
//    @JvmStatic
    private val counter: AtomicInteger = AtomicInteger(0)

    /**
     * lru cache
     */
    private val lruCacheMap: LRUCacheMap<String, MessageSource>
    get() = LRUCacheMap(initialCapacity, max)

    /** 缓存消息记录 */
//    @JvmStatic
    open fun cache(receipt: MessageReceipt<*>): String = cache(receipt.source)

    /** 缓存消息记录 */
//    @JvmStatic
    open fun cache(source: MessageSource): String {
        val id = source.bot.id
        val key = source.toCacheKey()
        return cache(id, key, source)
    }

    /**
     * 记录一个缓存
     */
    private fun cache(botId: Long, key: String, source: MessageSource): String{
        // 获取缓存map
        val cacheMap = botCacheMap.computeIfAbsent(botId) { lruCacheMap }

        // 缓存
        cacheMap.putPlusMinutes(key, source, cacheTime)

        // 计数+1, 如果大于100，清除缓存
        if(cacheCheck.clearCheck(counter.addAndGet(1))){
            counter.set(0)
            synchronized(botCacheMap){
                botCacheMap.forEach{it.value.detect()}
            }
        }
        return key
    }


    /** 获取缓存 */
//    @JvmStatic
    open fun get(key: String, botId: Long): MessageSource? {
        // 获取缓存值，可能为null
        return botCacheMap[botId]?.get(key)
    }

    /** 移除缓存 */
//    @JvmStatic
    open fun remove(key: String, botId: Long): MessageSource? {
        // 获取缓存值，可能为null
        return botCacheMap[botId]?.remove(key)
    }

}

/**
 * 通过[MessageSource]得到一个消息的ID
 * 1.8.2+之后不再将 `time`作为id的一部分
 */
fun MessageSource.toCacheKey() = "${this.id}.${this.internalId}"
// fun MessageSource.toCacheKey() = "${this.id}.${this.internalId}.${this.time}"

