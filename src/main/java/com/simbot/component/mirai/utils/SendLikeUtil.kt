/*
 * Copyright (c) 2020. ForteScarlet All rights reserved.
 * Project  component-mirai
 * File     SendLikeUtil.kt
 *
 * You can contact the author through the following channels:
 * github https://github.com/ForteScarlet
 * gitee  https://gitee.com/ForteScarlet
 * email  ForteScarlet@163.com
 * QQ     1149159218
 *
 *
 */

package com.simbot.component.mirai.utils

import com.forte.qqrobot.sender.HttpClientAble
import com.simbot.component.mirai.security.cookies
import net.mamoe.mirai.Bot


object SendLikeUtil {
    private const val URL = "https://club.vip.qq.com/visitor/like"
    private const val URL2 = "https://r.vip.qq.com/report/dc"
    // private val COOKIES_SPLIT_REGEX = Regex("; *")

    /**
     * to like url.
     */
    private fun toUrl(gtk: String, toCode: String): String =
        "$URL?g_tk=$gtk&u=&nav=0&uin=$toCode&t=${System.currentTimeMillis()}"

    /**
     * to like url2.
     *
     * @param uin 发送like的人
     */
    private fun toUrl2(gtk: String, uin: String, toUin: String): String{
        // https://r.vip.qq.com/report/dc?
        // uin=1149159218&
        // domain=android&
        // dcid=dc02842&
        // biz_id=10002009&
        // mqq_ver=0&
        // page_id=heatdetail&
        // oper_id=click_like&
        // to_uin=2240189254&
        // g_tk=1302669044
        return "$URL2?uin=$uin&domain=android&dcid=dc02842&biz_id=10002009&mqq_ver=0&page_id=heatdetail&oper_id=click_like&to_uin=$toUin&g_tk=$gtk"
    }


    /**
     * send like
     */
    fun sendLike(times: Int, code: String, bot: Bot, http: HttpClientAble) : Boolean {
        val cookies = bot.cookies
        val cookiesMap: MutableMap<String, String> = cookies.cookiesMap
        val header: MutableMap<String, String> = mutableMapOf("Referer" to "https://club.vip.qq.com/visitor/index")

        // 响应值
        val url = toUrl(cookies.gTk.toString(), code)
        val url2 = toUrl2(cookies.gTk.toString(), bot.id.toString(), code)

        http.get(url, null, cookiesMap, header) ?: return false
        http.get(url2, null, cookiesMap, header) ?: return false
        return true

        // val resultJson = JSONObject.parseObject(result)

        // val msg: String? = resultJson["msg"]?.toString()

        // if(resultJson.getIntValue("code") in -499 .. -400) {
        //     throw APIFailedException("失败 : $msg")
        // }

        // return when(resultJson.getIntValue("result")) {
        //     20003 -> throw APIFailedException("点赞上限 : $msg")
        //     10003 -> throw APIFailedException("权限异常 : $msg")
        //     0 -> true
        //     else -> throw APIFailedException("未知异常 : $msg")
        // }
    }


}