/*
 *
 * Copyright (c) 2020. ForteScarlet All rights reserved.
 * Project  component-mirai
 * File     CacheMapConfigurations.kt
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

import com.forte.config.Conf
import com.forte.config.ConfigurationHelper
import com.forte.config.InjectableConfig
import java.util.concurrent.TimeUnit


/**
 * 几大缓存类的配置类
 */
class CacheMapConfigurationInjectable {
    val recallCacheConfigurationInjectableConfig: InjectableConfig<RecallCacheConfiguration> = ConfigurationHelper.toInjectable(RecallCacheConfiguration::class.java)

    val requestCacheConfigurationInjectableConfig: InjectableConfig<RequestCacheConfiguration> = ConfigurationHelper.toInjectable(RequestCacheConfiguration::class.java)

    val imageCacheConfigurationInjectableConfig: InjectableConfig<ImageCacheConfiguration> = ConfigurationHelper.toInjectable(ImageCacheConfiguration::class.java)

    val voiceCacheConfigurationInjectableConfig: InjectableConfig<VoiceCacheConfiguration> = ConfigurationHelper.toInjectable(VoiceCacheConfiguration::class.java)

    val contactCacheConfigurationInjectableConfig: InjectableConfig<ContactCacheConfiguration> = ConfigurationHelper.toInjectable(ContactCacheConfiguration::class.java)
}

/**
 * [com.simbot.component.mirai.collections.RecallCache]的配置类
 */
@Conf("simbot.mirai.cache.recall")
class RecallCacheConfiguration(
        /**
         * 清理缓存临界值, 当计数器达到指定值则触发一次清理
         */
        @Conf("check", comment = "清理缓存临界值, 当计数器达到指定值则触发一次清理")
        var check: Int = 1000,
        /**
         * 缓存时间
         */
        @Conf("cacheTime", comment = "缓存时间(ms)")
        var cacheTime: Long = TimeUnit.MINUTES.toMillis(30),
        /**
         * 内部缓存的初始容量
         */
        @Conf("initialCapacity", comment = "内部缓存的初始容量")
        var initialCapacity: Int = 32,

        /**
         * 缓存的最大容量
         */
        @Conf("max", comment = "缓存的最大容量")
        var max: Long = 102400
)


/**
 * [com.simbot.component.mirai.collections.RequestCache] 使用的配置类
 */
@Conf("simbot.mirai.cache.request")
class RequestCacheConfiguration(
        /**
         * 清理缓存临界值, 当计数器达到1000则触发一次清理
         */
        @Conf("check", comment = "清理缓存临界值, 当计数器达到指定值则触发一次清理")
        var check: Int = 1000,
        /**
         * 默认缓存5分钟
         */
        @Conf("cacheTime", comment = "缓存时间(ms)")
        var cacheTime: Long = TimeUnit.MINUTES.toMillis(5),
        /**
         * 内部缓存的初始容量
         */
        @Conf("friend.initialCapacity", comment = "好友相关的请求的内部缓存的初始容量")
        var friendRequestInitialCapacity: Int = 32,
        /**
         * 内部缓存的初始容量
         */
        @Conf("join.initialCapacity", comment = "群相关的请求的内部缓存的初始容量")
        var joinRequestInitialCapacity: Int = 32,
        /**
         * 缓存的最大容量
         */
        @Conf("friend.max", comment = "好友相关的缓存的最大容量")
        var friendRequestMax: Long = 102400,
        /**
         * 缓存的最大容量
         */
        @Conf("join.max", comment = "群相关的缓存的最大容量")
        var joinRequestMax: Long = 102400
)

/**
 * [com.simbot.component.mirai.collections.ImageCache] 使用的配置类
 */
@Conf("simbot.mirai.cache.image")
class ImageCacheConfiguration(
        /**
         * 清理缓存临界值, 当计数器达到1000则触发一次清理
         */
        @Conf("check", comment = "清理缓存临界值, 当计数器达到指定值则触发一次清理")
        var check: Int = 1000,
        /**
         * 默认缓存30分钟
         */
        @Conf("cacheTime", comment = "缓存时间(ms)")
        var cacheTime: Long = TimeUnit.MINUTES.toMillis(30),
        /**
         * 内部缓存的初始容量
         */
        @Conf("initialCapacity", comment = "内部缓存的初始容量")
        var initialCapacity: Int = 32,
        /**
         * 缓存的最大容量
         */
        @Conf("max", comment = "缓存的最大容量")
        var max: Long = 102400
)

/**
 * [com.simbot.component.mirai.collections.VoiceCache] 使用的配置类
 */
@Conf("simbot.mirai.cache.voice")
class VoiceCacheConfiguration(
        /**
         * 清理缓存临界值, 当计数器达到1000则触发一次清理
         * 如果小于0，则不会检查, 如果等于0，则每次都会检查
         */
        @Conf("check", comment = "清理缓存临界值, 当计数器达到指定值则触发一次清理")
        var check: Int = 1000,
        /**
         * 默认缓存30分钟
         */
        @Conf("cacheTime", comment = "缓存时间(ms)")
        var cacheTime: Long = TimeUnit.MINUTES.toMillis(30),
        /**
         * 内部缓存的初始容量
         */
        @Conf("initialCapacity", comment = "内部缓存的初始容量")
        var initialCapacity: Int = 32,
        /**
         * 缓存的最大容量
         */
        @Conf("max", comment = "缓存的最大容量")
        var max: Long = 102400
)

/**
 * [com.simbot.component.mirai.collections.ContactCache] 使用的配置类
 */
@Conf("simbot.mirai.cache.contact")
class ContactCacheConfiguration(
        /**
         * 清理缓存临界值, 当计数器达到1000则触发一次清理
         */
        @Conf("check", comment = "清理缓存临界值, 当计数器达到指定值则触发一次清理")
        var check: Int = 1000,
        /**
         * 默认缓存15分钟
         */
        @Conf("cacheTime", comment = "缓存时间(ms)")
        var cacheTime: Long = TimeUnit.MINUTES.toMillis(15),
        /**
         * 内部缓存的初始容量
         */
        @Conf("initialCapacity", comment = "内部缓存的初始容量")
        var initialCapacity: Int = 32,

        /**
         * 缓存的最大容量
         */
        @Conf("max", comment = "缓存的最大容量")
        var max: Long = 102400
)





