/*
 *
 * Copyright (c) 2020. ForteScarlet All rights reserved.
 * Project  component-mirai
 * File     SingletonMap.kt
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

import java.io.Serializable


/**
 * 一个单值Map, 只可以改变value值.
 * key不可为null
 * value不可为null
 */
@Suppress("MemberVisibilityCanBePrivate")
class SingletonMap<K, V>(val key: K, var value: V) : Map<K, V>, Serializable, Cloneable {
    constructor(entry: Map.Entry<K, V>): this(entry.key, entry.value)

    private val keySet: Set<K> = setOf(key)

    private val singletonEntry = SingletonMapEntry()

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
        get() = mutableSetOf(singletonEntry)

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


    /**
     * entry
     */
    inner class SingletonMapEntry: MutableMap.MutableEntry<K, V> {

        /**
         * set value for [super map value][SingletonMap.value]
         */
        override fun setValue(newValue: V): V {
            val oldValue = value
            this@SingletonMap.value = newValue
            return oldValue
        }

        /**
         * Returns the key of this key/value pair.
         * just return [super map key][SingletonMap.key]
         */
        override val key: K = this@SingletonMap.key

        /**
         * Returns the value of this key/value pair.
         * just return [super map value][SingletonMap.value]
         */
        override val value: V
            get() = this@SingletonMap.value
    }

}
