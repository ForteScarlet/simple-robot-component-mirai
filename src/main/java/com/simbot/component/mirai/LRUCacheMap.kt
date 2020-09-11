/*
 *
 * Copyright (c) 2020. ForteScarlet All rights reserved.
 * Project  component-mirai
 * File     LRUCacheMap.kt
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

package com.simbot.component.mirai

import com.googlecode.concurrentlinkedhashmap.ConcurrentLinkedHashMap
import com.googlecode.concurrentlinkedhashmap.Weighers
import java.time.LocalDateTime
import java.time.temporal.TemporalAmount
import java.time.temporal.TemporalUnit
import java.util.*
import java.util.concurrent.ConcurrentMap
import java.util.function.BiConsumer

/**
 * 使用LRU的缓存Map
 * @param max 最大值上限
 */
open class LRUCacheMap<T, R>
@JvmOverloads
constructor(initialCapacity: Int = 8, private val max: Long = 102400):
        AbstractMap<T, R>() {

    private var lruLinkedMap: MutableMap<T, CacheReturn<R>> = CacheLRULinkedHashMap<T, R>(initialCapacity, max)

    /**
     * 获取
     */
    override fun get(key: T?): R? {
        val cacheReturn = lruLinkedMap[key]
//        val cacheReturn = lruLinkedMap[key]
        return if (cacheReturn == null) {
            null
        } else { //有东西，判断是否过期
            if (cacheReturn.isExpired) { //如果当前时间在过期时间之后，则为过期，移除
                remove(key)
                null
            } else {
                cacheReturn.cache
            }
        }
    }


    /**
     * 添加一个，如果时间已经过期则不添加
     */
    fun put(t: T, r: R, expireDate: LocalDateTime): R? = put(t, CacheReturn(expireDate, r))

    /** 添加一个，指定过期规则  */
    fun put(t: T, r: R, amount: TemporalAmount?): R? = put(t, r, LocalDateTime.now().plus(amount))

    /** 存值并指定过期规则  */
    fun put(t: T, r: R, amountToAdd: Long, unit: TemporalUnit?): R? = put(t, r, LocalDateTime.now().plus(amountToAdd, unit))

    /** 过xx纳秒之后过期  */
    fun putPlusNanos(t: T, r: R, nanos: Long): R? = put(t, r, LocalDateTime.now().plusNanos(nanos))

    /** 过xx秒之后过期  */
    fun putPlusSeconds(t: T, r: R, seconds: Long): R? = put(t, r, LocalDateTime.now().plusSeconds(seconds))

    /** 过xx分钟后过期  */
    fun putPlusMinutes(t: T, r: R, minutes: Long): R? = put(t, r, LocalDateTime.now().plusMinutes(minutes))

    /** 过xx小时之后过期  */
    fun putPlusHours(t: T, r: R, hours: Long): R? = put(t, r, LocalDateTime.now().plusHours(hours))

    /** 过xx天之后过期  */
    fun putPlusDays(t: T, r: R, days: Long): R? = put(t, r, LocalDateTime.now().plusDays(days))

    /** 过xx月之后过期  */
    fun putPlusMonth(t: T, r: R, month: Long): R? = put(t, r, LocalDateTime.now().plusMonths(month))

    /** 过xx年之后过期，如果你能等到那时候的话  */
    fun putPlusYear(t: T, r: R, year: Long): R? = put(t, r, LocalDateTime.now().plusYears(year))


    /**
     * 添加一个，如果时间已经过期则不添加并尝试移除存在的值
     */
    @Synchronized
    protected open fun put(t: T, cacheReturn: CacheReturn<R>): R? {
        return if (LocalDateTime.now().isBefore(cacheReturn.expireTime)) {
            val put = lruLinkedMap.put(t, cacheReturn)
            put?.cache
        } else {
            remove(t)
            null
        }
    }

    /**
     * 默认过期时间为1天后
     * 如果本身存在此值，则会不刷新时间的覆盖原值
     */
    override fun put(key: T, value: R): R? {
        val old = lruLinkedMap[key]
        return if(old!=null){
            synchronized(old){
                old.cache = value
                old.cache
            }
        }else{
            putPlusDays(key, value, 1)
        }
    }


    override fun remove(key: T?): R? {
        val remove = lruLinkedMap.remove(key)
        return remove?.cache
    }

    override fun clear() {
        lruLinkedMap.clear()
    }


    override val entries: MutableSet<MutableMap.MutableEntry<T, R>>
        get() = lruLinkedMap.entries.asSequence()
                .map { e: Map.Entry<T, CacheReturn<R>?> ->
                    val value = e.value
                    SimpleEntry<T, R>(e.key, value?.cache)
                }.toMutableSet()


    /**
     * 手动触发一次检测，检测全部的缓存中是否存在过期值
     */
    fun detect() {
        synchronized(lruLinkedMap) {
            val it = lruLinkedMap.iterator()
            while (it.hasNext()) {
                val next = it.next()
                if (next.value.isExpired) {
                    it.remove()
                }
            }
        }
    }

    /**
     * 就是foreach
     * @param consumer
     */
    fun cacheForEach(consumer: BiConsumer<T, CacheReturn<R>?>) {
        lruLinkedMap.forEach { consumer.accept(it.key, it.value) }
    }

    /**
     * 缓存对象，记录过期时间和记录的缓存返回值
     */
    class CacheReturn<T>(expireTime: LocalDateTime, obj: T) {
        /** 过期时间  */
        var expireTime: LocalDateTime
        var cache: T

        /**
         * 判断是否过期了
         */
        val isExpired: Boolean
            get() = LocalDateTime.now().isAfter(expireTime)


        init {
            Objects.requireNonNull(expireTime)
            this.expireTime = expireTime
            cache = obj
        }

        override fun toString(): String {
            return "CacheReturn(expireTime=$expireTime, cache=$cache)"
        }
    }

}

/**
 * LRULinkedHashMap
 * 默认最多缓存`102400`个值
 */
@Suppress("DELEGATED_MEMBER_HIDES_SUPERTYPE_OVERRIDE")
open class LRULinkedHashMap<T, R>
@JvmOverloads
constructor(initialCapacity: Int = 8, private val maxSize: Long = 102400, builder: ConcurrentLinkedHashMap.Builder<T, R>.() -> Unit = {
    this.initialCapacity(initialCapacity)
            .maximumWeightedCapacity(maxSize)
            .weigher(Weighers.singleton())
}) :
        AbstractMap<T, R>(),
        ConcurrentMap<T, R> by ConcurrentLinkedHashMap.Builder<T, R>().also(builder).build()



/**
 * 针对[LRUCacheMap.CacheReturn]进行权重判断的缓存Map
 */
open class CacheLRULinkedHashMap<T, R>(initialCapacity: Int = 8, private val maxSize: Long = 102400):
        LRULinkedHashMap<T, LRUCacheMap.CacheReturn<R>>(initialCapacity, maxSize, {
            this.initialCapacity(initialCapacity)
                    .maximumWeightedCapacity(maxSize)
                    .weigher(Weighers.singleton())


        })