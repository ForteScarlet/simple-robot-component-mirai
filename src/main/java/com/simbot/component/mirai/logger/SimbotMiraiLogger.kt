/*
 *
 *  * Copyright (c) 2020. ForteScarlet All rights reserved.
 *  * Project  component-mirai
 *  * File     SimbotMiraiLogger.kt
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

package com.simbot.component.mirai.logger

import com.forte.plusutils.consoleplus.console.Colors
import com.forte.plusutils.consoleplus.console.colors.FontColorTypes
import com.forte.qqrobot.log.QQLog
import net.mamoe.mirai.utils.MiraiLoggerPlatformBase

/**
 * 默认使用的普通日志实现，不会区分bot
 */
object SimbotMiraiLogger : MiraiLoggerPlatformBase() {
    /**
     * 日志的标记. 在 Mirai 中, identity 可为
     * - "Bot"
     * - "BotNetworkHandler"
     * 等.
     *
     * 它只用于帮助调试或统计. 十分建议清晰定义 identity
     */
    override val identity: String = "Mirai"

    /**
     * 颜色标记
     */
    private val identityColor: String = Colors.builder()
            .add("[", FontColorTypes.BLUE)
            .add(identity, FontColorTypes.DARK_GREEN)
            .add("]", FontColorTypes.BLUE)
            .build().toString()

    private val verboseColor: String = Colors.builder()
            .add("[", FontColorTypes.BLUE)
            .add("V/B", FontColorTypes.YELLOW)
            .add("]", FontColorTypes.BLUE)
            .build().toString()

    override fun debug0(message: String?, e: Throwable?) {
        QQLog.debug("{0}", e, "$identityColor $message")
    }

    override fun error0(message: String?, e: Throwable?) {
        QQLog.error("{0}", e, "$identityColor $message")
    }

    override fun info0(message: String?, e: Throwable?) {
        QQLog.info("{0}", e, "$identityColor  $message")
    }

    override fun verbose0(message: String?, e: Throwable?) {
        QQLog.debug("{0}", e, "$identityColor $verboseColor $message")
    }

    override fun warning0(message: String?, e: Throwable?) {
        QQLog.warning("{0}", e, "$identityColor  $message")
    }


}