/*
 *
 * Copyright (c) 2020. ForteScarlet All rights reserved.
 * Project  component-mirai
 * File     MiraiEvents.kt
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

package com.simbot.component.mirai.messages

import com.forte.qqrobot.anno.Listen
import kotlin.annotation.AnnotationTarget.*

/**
 * 定义mirai组件所提供的额外监听事件
 */
object MiraiEvents {
    /**
     * 核心1.6.2后已经内置了此类型的监听。
     * @since 1.1.0-1.16
     * @deprecated 核心已整合此事件。
     * 1.3.0-1.16+ 之后重新进行向下兼容，但是依旧为过时状态
     */
    @Deprecated("just use friendDelete", replaceWith = ReplaceWith("@OnFriendDelete", "com.forte.qqrobot.anno.template.OnFriendDelete"))
    const val friendDeleteEvent: String = "friendDelete"

    /**
     * 好友更换头像事件
     * @since 1.3.0-1.16
     * @see FriendAvatarChanged
     */
    const val friendAvatarChangedEvent: String = "friendAvatarChanged"


    /**
     * 好友昵称变动事件
     * @since 1.8.0-1.16
     * @see MiraiFriendNicknameChangedEvent
     */
    const val friendNicknameChangedEvent: String = "friendNicknameChanged"


    /**
     * 好友输入状态变更事件
     * @since 1.8.0-1.16
     * @see MiraiFriendInputStatusChangedEvent
     */
    const val friendInputStatusChangedEvent: String = "friendInputStatusChanged"

    /**
     * bot离线事件
     * @see BotOffline
     * @see MiraiBotOfflineEvent
     */
    const val botOfflineEvent: String = "botOffline"

    /**
     * bot重新登录事件
     * @see BotRelogin
     * @see MiraiBotReloginEvent
     */
    const val botReloginEvent: String = "botRelogin"


    /**
     * 群名称变更事件
     * @since 1.8.0-1.16
     * @see GroupNameChanged
     * @see MiraiGroupNameChangedEvent
     */
    const val groupNameChangedEvent: String = "groupNameChanged"

    /**
     * 群员备注变更事件
     * @since 1.8.0-1.16
     * @see MemberRemarkChanged
     * @see MiraiMemberRemarkChangedEvent
     */
    const val memberRemarkChangedEvent: String = "memberRemarkChanged"

    /**
     * 群成员头衔变更事件
     * @since 1.8.0-1.16
     * @see MemberSpecialTitleChanged
     * @see MiraiMemberRemarkChangedEvent
     */
    const val memberSpecialTitleChangedEvent: String = "memberSpecialTitleChanged"
}

/*
    此处提供一些额外的监听事件的整合注解
 */

/**
 * 针对于额外的事件[MiraiEvents.friendAvatarChangedEvent]的整合注解。
 */
@Retention(AnnotationRetention.RUNTIME) //注解会在class字节码文件中存在，在运行时可以通过反射获取到
@Target(allowedTargets = [CLASS, FUNCTION, PROPERTY_GETTER, PROPERTY_SETTER]) //接口、类、枚举、注解、方法
@Listen.ByName(MiraiEvents.friendAvatarChangedEvent)
annotation class OnFriendAvatarChanged

