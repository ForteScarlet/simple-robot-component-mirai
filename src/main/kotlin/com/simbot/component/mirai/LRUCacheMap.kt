package com.simbot.component.mirai

import java.time.LocalDateTime
import java.time.temporal.TemporalAmount
import java.time.temporal.TemporalUnit
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import java.util.function.BiConsumer

/**
 * 使用LRU的缓存Map
 * @param max 最大值上限
 */
open class LRUCacheMap<T, R>(initialCapacity: Int = 8, loadFactor: Float =  0.75f, private val max: Int = 102400): AbstractMap<T, R>(), ConcurrentMap<T, R> by ConcurrentHashMap<T, R>() {

    private var lruLinkedMap: MutableMap<T, CacheReturn<R>> = LRULinkedHashMap(initialCapacity, loadFactor, max)

    /**
     * 获取
     */
    override fun get(key: T?): R? {
        val cacheReturn = lruLinkedMap[key]
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
    fun put(t: T, r: R, expireDate: LocalDateTime): R? {
        return put(t, CacheReturn(expireDate, r))
    }

    /** 添加一个，指定过期规则  */
    fun put(t: T, r: R, amount: TemporalAmount?): R? {
        return put(t, r, LocalDateTime.now().plus(amount))
    }

    /** 存值并指定过期规则  */
    fun put(t: T, r: R, amountToAdd: Long, unit: TemporalUnit?): R? {
        return put(t, r, LocalDateTime.now().plus(amountToAdd, unit))
    }

    /** 过xx纳秒之后过期  */
    fun putPlusNanos(t: T, r: R, nanos: Long): R? {
        return put(t, r, LocalDateTime.now().plusNanos(nanos))
    }

    /** 过xx秒之后过期  */
    fun putPlusSeconds(t: T, r: R, seconds: Long): R? {
        return put(t, r, LocalDateTime.now().plusSeconds(seconds))
    }

    /** 过xx分钟后过期  */
    fun putPlusMinutes(t: T, r: R, minutes: Long): R? {
        return put(t, r, LocalDateTime.now().plusMinutes(minutes))
    }

    /** 过xx小时之后过期  */
    fun putPlusHours(t: T, r: R, hours: Long): R? {
        return put(t, r, LocalDateTime.now().plusHours(hours))
    }

    /** 过xx天之后过期  */
    fun putPlusDays(t: T, r: R, days: Long): R? {
        return put(t, r, LocalDateTime.now().plusDays(days))
    }

    /** 过xx月之后过期  */
    fun putPlusMonth(t: T, r: R, month: Long): R? {
        return put(t, r, LocalDateTime.now().plusMonths(month))
    }

    /** 过xx年之后过期，如果你能等到那时候的话  */
    fun putPlusYear(t: T, r: R, year: Long): R? {
        return put(t, r, LocalDateTime.now().plusYears(year))
    }


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
     *
     */
    @Deprecated(" 推荐使用指定时间的方法而非此方法", ReplaceWith("putPlusDays(t, r, 1)"))
    override fun put(t: T, r: R): R? {
        return putPlusDays(t, r, 1)
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
                    SimpleEntry<T, R>(e.key, value?.cache) as MutableMap.MutableEntry<T, R>
                }.toMutableSet()


    /**
     * 手动触发一次检测，检测全部的缓存中是否存在过期值
     */
    fun detect() {
        lruLinkedMap.forEach { (k: T, v: CacheReturn<R>) ->
            if (v.isExpired) {
                remove(k)
            }
        }
    }

    /**
     * 就是foreach
     * @param consumer
     */
    fun cacheForEach(consumer: BiConsumer<T, CacheReturn<R>?>) {
        lruLinkedMap.forEach{ consumer.accept(it.key, it.value) }
    }

    /**
     * 缓存对象，记录过期时间和记录的缓存返回值
     */
    class CacheReturn<T>(expireTime: LocalDateTime, obj: T) {
        /** 过期时间  */
        val expireTime: LocalDateTime
        val cache: T

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
open class LRULinkedHashMap<T, R>
@JvmOverloads
constructor(initialCapacity: Int = 8, loadFactor: Float =  0.75f, private val maxSize: Int = 102400):
        LinkedHashMap<T, R>(initialCapacity, loadFactor, true) {

    override fun removeEldestEntry(eldest: MutableMap.MutableEntry<T, R>?): Boolean {
        // 如果数量超过最大上限，移除
        return size > maxSize
    }

}