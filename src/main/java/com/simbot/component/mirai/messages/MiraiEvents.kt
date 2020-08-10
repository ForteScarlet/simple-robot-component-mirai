/*
 *
 * Copyright (c) 2020. ForteScarlet All rights reserved.
 * Project  component-mirai
 * File     MiraiEvents.kt
 *
 * You can contact the author through the following channels:
 * github https://github.com/ForteScarlet
 * gitee  https://gitee.com/ForteScarlet
 * email  ForteScarlet@163.com
 * QQ     1149159218
 *
 * The Mirai code is copyrighted by mamoe-mirai
 * you can see mirai at https://github.com/mamoe/mirai
 *
 *
 *
 */

package com.simbot.component.mirai.messages

import com.forte.qqrobot.anno.Listen
import kotlin.annotation.AnnotationTarget.*

/**
 * 定义mirai组件所提供的额外监听事件
 */
object MiraiEvents {
    /**
     * 核心1.6.2后已经内置了此类型的监听。
     */
    @Deprecated("just use FriendDelete")
    const val friendDeleteEvent: String = "FRIEND_EVENT"

    /**
     * 好友更换头像事件
     */
    const val friendAvatarChangedEvent: String = "FRIEND_AVATAR_CHANGED_EVENT"


}

/*
    此处提供一些额外的监听事件的整合注解
 */
@Retention(AnnotationRetention.RUNTIME) //注解会在class字节码文件中存在，在运行时可以通过反射获取到
@Target(allowedTargets = [CLASS, FUNCTION, PROPERTY_GETTER, PROPERTY_SETTER]) //接口、类、枚举、注解、方法
@Listen.ByName(MiraiEvents.friendAvatarChangedEvent)
annotation class OnFriendAvatarChanged

