/*
 *
 *  * Copyright (c) 2020. ForteScarlet All rights reserved.
 *  * Project  component-mirai
 *  * File     CacheMaps.kt
 *  *
 *  * You can contact the author through the following channels:
 *  * github https://github.com/ForteScarlet
 *  * gitee  https://gitee.com/ForteScarlet
 *  * email  ForteScarlet@163.com
 *  * QQ     1149159218
 *  *
 *  * The Mirai code is copyrighted by mamoe-mirai
 *  * you can see mirai at https://github.com/mamoe/mirai
 *  *
 *  *
 *
 */

package com.simbot.component.mirai

import com.simbot.component.mirai.collections.*

/**
 * 记录缓存库的Data类
 */
open class CacheMaps(
        val recallCache: RecallCache,
        val requestCache: RequestCache,
        val imageCache: ImageCache,
        val voiceCache: VoiceCache,
        val contactCache: ContactCache
)