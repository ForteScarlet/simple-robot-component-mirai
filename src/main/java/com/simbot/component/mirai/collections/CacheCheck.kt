/*
 *
 * Copyright (c) 2020. ForteScarlet All rights reserved.
 * Project  component-mirai
 * File     CacheCheck.kt
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


/**
 * inline class for check clear
 */
@Suppress("MemberVisibilityCanBePrivate")
internal inline class CacheCheck(val check: Int) {

    /**
     * 判断当前count是否需要进行clear
     * 如果小于0，则不会检查, 如果等于0，则每次都会检查
     */
    fun clearCheck(count: Int): Boolean {
        return when {
            check < 0 -> false
            check == 0 -> true
            else -> check in 1 .. count
        }
    }
}
