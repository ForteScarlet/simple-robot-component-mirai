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

import com.forte.qqrobot.beans.messages.result.StrangerInfo

/**
 * 定义mirai组件所提供的额外监听事件
 */
object MiraiEvents {
    /**
     * h好友删除事件，对应的数据接口为 [FriendDelete]，其内容与 [StrangerInfo] 一致 。
     * mirai中的部分限制也是相同的，例如无法获取age与sex。
     */
    const val friendDeleteEvent: String = "FRIEND_EVENT"
}
