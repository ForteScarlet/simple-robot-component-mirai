/*
 *
 * Copyright (c) 2020. ForteScarlet All rights reserved.
 * Project  component-mirai
 * File     BotLevelUtil.kt
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

package com.simbot.component.mirai.utils

import com.forte.qqrobot.sender.HttpClientAble
import com.simbot.component.mirai.security.cookies
import net.mamoe.mirai.Bot
import java.util.regex.Pattern

/**
 *
 * 通过得到[Bot]的cookies信息以获取Bot的等级信息。
 *
 * Created by lcy on 2020/8/25.
 * @author lcy
 */
object BotLevelUtil {
    private val levelPattern: Pattern = Pattern.compile("<em class=\"levelimg\">(\\d+)</em>")
    private const val VIP_URL = "https://vip.qq.com/client/level"

    /**
     * 获取不到的情况下使用的默认值
     */
    private const val DEFAULT_VALUE = -1

    /**
     * 获取当前bot的等级。
     * @return [bot]的level. 如果获取不到/接口变更/cookie失效等，就会得到-1.
     */
    fun level(bot: Bot, http: HttpClientAble): Int {
        try {
            val cookies = bot.cookies
            val vipHtml = http.get(VIP_URL, null, cookies.cookiesMap) ?: return DEFAULT_VALUE

            val matcher = levelPattern.matcher(vipHtml)
            return if(matcher.find()){
                matcher.group(1).toInt()
            }else DEFAULT_VALUE
        }catch (e: Throwable){
            return DEFAULT_VALUE
        }
    }

}