/*
 *
 *  Copyright (c) 2020. ForteScarlet All rights reserved.
 *  Project  component-mirai
 *  File     collections.kt
 *
 *  You can contact the author through the following channels:
 *  github https://github.com/ForteScarlet
 *  gitee  https://gitee.com/ForteScarlet
 *  email  ForteScarlet@163.com
 *  QQ     1149159218
 *
 *
 */

package com.simbot.component.mirai.collections

import java.io.Serializable


/**
 * 一个单值Map，是一个不可变map
 * key不可为null且不可变
 * value不可为null
 */
class SingletonMap<K, V>(val key: K, var value: V) : Map<K, V>, Serializable, Cloneable {
    constructor(entry: Map.Entry<K, V>): this(entry.key, entry.value)

    private val keySet: Set<K> = setOf(key)

    /**
     * 判断是否与key相同
     */
    private fun isKey(other: Any?): Boolean {
        return key == other
    }

    /**
     * 判断是否与value相同
     */
    private fun isValue(other: Any?): Boolean {
        return value == other
    }

    /**
     * 大小固定为1
     */
    override val size: Int = 1

    /**
     * 得到一个entries
     */
    override val entries: MutableSet<MutableMap.MutableEntry<K, V>>
        get() = mutableSetOf(SingletonMapEntry(key, value))

    /**
     * keys
     */
    override val keys: MutableSet<K>
        get() = keySet.toMutableSet()

    /**
     * values
     */
    override val values: MutableCollection<V>
        get() = mutableListOf(value)

    /**
     * 是否存在key
     */
    override fun containsKey(key: K): Boolean = isKey(key)

    override fun containsValue(value: V): Boolean = isValue(value)

    override fun get(key: K): V? = if(isKey(key)) value else null

    override fun isEmpty(): Boolean = false
}

/**
 * 单值Map的map entry
 */
class SingletonMapEntry<K, V>(override val key: K, override var value: V): MutableMap.MutableEntry<K, V> {
    override fun setValue(newValue: V): V {
        val oldValue = value
        value = newValue
        return oldValue
    }

}