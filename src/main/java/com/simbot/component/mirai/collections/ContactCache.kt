/*
 *
 * Copyright (c) 2020. ForteScarlet All rights reserved.
 * Project  component-mirai
 * File     ContactCache.kt
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

import com.simbot.component.mirai.ContactCacheConfiguration
import com.simbot.component.mirai.LRUCacheMap
import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.Contact
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger


/**
 * 发送消息的缓存器，只会在发送给非当前消息群的人的消息的时候触发缓存
 */
open class ContactCache(
        /**
         * 清理缓存临界值, 当计数器达到1000则触发一次清理
         */
        check: Int,
        /**
         * 默认缓存15分钟
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
)
{

    constructor(config: ContactCacheConfiguration): this(config.check, config.cacheTime, config.initialCapacity, config.max)

    /**
     * [CacheCheck]
     * 代表检测当前计数是否可以进行清理
     */
    private val cacheCheck: CacheCheck = CacheCheck(check)

    // contact缓存map
//    @JvmStatic
    private val contactCacheMap: MutableMap<Long, LRUCacheMap<Long, Contact>> by lazy { ConcurrentHashMap<Long, LRUCacheMap<Long, Contact>>() }


    private val lruCacheMap: LRUCacheMap<Long, Contact>
    get() = LRUCacheMap(initialCapacity, max)


    /** 计数器，当计数器达到100的时候，触发缓存清除 */
//    @JvmStatic
    private val counter: AtomicInteger = AtomicInteger(0)

    /**
     * 获取某个bot的缓存时间
     */
    private fun getBotCacheMap(botId: Long): LRUCacheMap<Long, Contact> {
        return contactCacheMap[botId] ?: run {
            val newMap = lruCacheMap
            contactCacheMap.compute(botId) {_, old -> old ?: lruCacheMap }
            newMap
        }
    }

    /**
     * 缓存所有信息
     */
    open fun cache(bot: Bot) {
        val cacheMap = getBotCacheMap(bot.id)
        bot.groups.asSequence().flatMap { g -> g.members.asSequence() }.forEach {
            // 计入缓存
            val memberId = it.id
            cacheMap.putIfAbsent(memberId, it)
        }
    }

    /**
     * 获取
     * @param key 要获取的contact的id
     * @param bot 哪个bot要获取contact，之所以是bot是因为如果获取不到的话要用bot来缓存信息
     */
//    @JvmStatic
    open operator fun get(key: Long, bot: Bot): Contact? {
        val botId = bot.id
        // 此bot的缓存map
        val cacheMap = getBotCacheMap(botId)
        // 获取缓存
        return cacheMap[key]?.also {
            // 如果存在，刷新其缓存时间
            cacheMap.putPlusMinutes(key, it, cacheTime)
        } ?: run {
            // 查询不到，尝试遍历并缓存所有的群聊群员。
            var find: Contact? = null
            bot.groups.asSequence().flatMap { g -> g.members.asSequence() }.forEach {
                // 计入缓存
                val memberId = it.id
                if(memberId == key){
                    find = it
                }
                cacheMap.putIfAbsent(memberId, it)
            }
            // 此时进行缓存清理计数
            if(cacheCheck.clearCheck(counter.addAndGet(1))){
                counter.set(0)
                synchronized(contactCacheMap){
                    contactCacheMap.forEach { it.value.detect() }
                }
            }
            find
        }
    }
}
