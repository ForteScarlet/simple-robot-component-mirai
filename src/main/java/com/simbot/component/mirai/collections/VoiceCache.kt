/*
 *
 * Copyright (c) 2020. ForteScarlet All rights reserved.
 * Project  component-mirai
 * File     VoiceCache.kt
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
import com.simbot.component.mirai.VoiceCacheConfiguration
import net.mamoe.mirai.message.data.Voice
import java.util.concurrent.atomic.AtomicInteger


/**
 * 语音缓存器
 * @author ForteScarlet <ForteScarlet@163.com>
 * @date 2020/8/26
 *
 */
open class VoiceCache(
        /**
         * 清理缓存临界值, 当计数器达到1000则触发一次清理
         * 会被转化为[CacheCheck]
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
    constructor(config: VoiceCacheConfiguration): this(config.check, config.cacheTime, config.initialCapacity, config.max)

    /**
     * [CacheCheck]
     * 代表检测当前计数是否可以进行清理
     */
    private val cacheCheck: CacheCheck = CacheCheck(check)


    /** voice缓存map */
    private val voiceCacheMap by lazy { LRUCacheMap<String, Voice>(initialCapacity, max) }

    /** 计数器，当计数器达到100的时候，触发缓存清除 */
    private val counter: AtomicInteger = AtomicInteger(0)

    /** 获取 */
    open operator fun get(key: String): Voice? {
        // 获取缓存, 并刷新时间
        val voice = voiceCacheMap[key] ?: return null
        voiceCacheMap.putPlusMinutes(key, voice, cacheTime)
        return voice
    }

    /** 获取 */
    open fun getGroup(key: String): Voice? {
        val getKey = key.toGroupKey()
        // 获取缓存, 并刷新时间
        val voice = voiceCacheMap[getKey] ?: return null
        voiceCacheMap.putPlusMinutes(getKey, voice, cacheTime)
        return voice
    }

    /** 获取 */
    open fun getPrivate(key: String): Voice? {
        val getKey = key.toPrivateKey()
        // 获取缓存, 并刷新时间
        val voice = voiceCacheMap[getKey] ?: return null
        voiceCacheMap.putPlusMinutes(getKey, voice, cacheTime)
        return voice
    }


    /** 记录一个map */
    open operator fun set(key: String, voice: Voice): Voice? {
        val putVoice = voiceCacheMap.putPlusMinutes(key, voice, cacheTime)
        if(cacheCheck.clearCheck(counter.addAndGet(1))){
            counter.set(0)
            voiceCacheMap.detect()
        }
        return putVoice
    }


}

/**
 * cache [Voice] like
 * `if([test]) { [VoiceCache]\[[Voice.fileName]] = [Voice] }`
 */
fun Voice.alsoCache(cache: VoiceCache, test: Boolean = true): Voice = also {
    if(test){
        cache[it.fileName] = it
    }
}

