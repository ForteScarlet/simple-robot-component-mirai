/*
 *
 * Copyright (c) 2020. ForteScarlet All rights reserved.
 * Project  component-mirai
 * File     ImageCache.kt
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

import com.simbot.component.mirai.ImageCacheConfiguration
import com.simbot.component.mirai.LRUCacheMap
import net.mamoe.mirai.message.data.Image
import java.util.concurrent.atomic.AtomicInteger


/**
 * 图片缓存器
 */
open class ImageCache(
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
    constructor(config: ImageCacheConfiguration): this(config.check, config.cacheTime, config.initialCapacity, config.max)

    /**
     * [CacheCheck]
     * 代表检测当前计数是否可以进行清理
     */
    private val cacheCheck: CacheCheck = CacheCheck(check)

    /** image缓存map */
    private val imageCacheMap by lazy { LRUCacheMap<String, Image>(initialCapacity, max) }


    /** 计数器，当计数器达到100的时候，触发缓存清除 */
    private val counter: AtomicInteger = AtomicInteger(0)

    /** 获取 */
    open operator fun get(key: String): Image? {
        // 获取缓存, 并刷新时间
        val image = imageCacheMap[key] ?: return null
        imageCacheMap.putPlusMinutes(key, image, cacheTime)
        return image
    }

    /** 记录一个map */
    open operator fun set(key: String, image: Image): Image? {
        val putImage = imageCacheMap.putPlusMinutes(key, image, cacheTime)
        if(cacheCheck.clearCheck(counter.addAndGet(1))){
            counter.set(0)
            imageCacheMap.detect()
        }
        return putImage

    }
}

/**
 * cache image like
 * `if([test]) { [ImageCache]\[[Image.imageId]] = [Image] }`
 */
fun Image.alsoCache(cache: ImageCache, test: Boolean = true): Image = also {
    if(test){
        cache[it.imageId] = it
    }
}
