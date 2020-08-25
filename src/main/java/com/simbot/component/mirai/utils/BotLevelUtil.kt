package com.simbot.component.mirai.utils

import com.forte.qqrobot.sender.HttpClientAble
import com.simbot.component.mirai.security.cookies
import net.mamoe.mirai.Bot
import java.util.regex.Pattern

/**
 *
 * 用于获取Bot等级的工具类
 *
 * Created by lcy on 2020/8/25.
 * @author lcy
 */
object BotLevelUtil {
    private val levelPattern: Pattern = Pattern.compile("<em class=\"levelimg\">(\\d+)</em>")
    private const val VIP_URL = "https://vip.qq.com/client/level"
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