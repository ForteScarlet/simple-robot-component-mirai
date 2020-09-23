/*
 * Copyright (c) 2020. ForteScarlet All rights reserved.
 * Project  component-mirai
 * File     KtorHttpClient.kt
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

import com.forte.qqrobot.anno.HttpTemplate
import com.forte.qqrobot.sender.HttpClientAble
import io.ktor.client.*


// @HttpTemplate(KtorHttpClient.NAME)
// object KtorHttpClient : HttpClientAble {
    // const val NAME = "KTOR_CLIENT"
    //
    // /**
    //  * ktor http client
    //  */
    // private val httpClient: HttpClient = HttpClient()
    //
    // /**
    //  * 使用get的方式进行网络请求
    //  * @param url       送信网络路径
    //  * @param params    参数列表，默认为空map，可以为null
    //  * @param cookies   所携带的cookie列表，默认为空map，可以为null
    //  * @param header    头信息，默认为空map，可以为null
    //  * @return 网页的返回值字符串
    //  */
    // override fun get(
    //     url: String?,
    //     params: MutableMap<String, String>?,
    //     cookies: MutableMap<String, String>?,
    //     header: MutableMap<String, String>?
    // ): String {
    //     if (!header!!.containsKey(HttpClientAble.USER_AGENT_KEY_NAME)) {
    //         header[HttpClientAble.USER_AGENT_KEY_NAME] = HttpClientAble.USER_AGENT_WIN10_CHROME
    //     }
    //     val _header = header?.run {
    //
    //     } ?: mutableMapOf()
    //
    //
    // }
    //
    // /**
    //  * 使用post的方式进行网络请求
    //  * 一般header中会提供一些json或者from的参数
    //  * @param url       送信网络路径
    //  * @param params    参数列表，默认为空map，可以为null
    //  * @param cookies   所携带的cookie列表，默认为空map，可以为null
    //  * @param header    头信息，默认为空map，可以为null
    //  * @return 网页的返回值字符串
    //  */
    // override fun post(
    //     url: String?,
    //     params: String?,
    //     cookies: MutableMap<String, String>?,
    //     header: MutableMap<String, String>?
    // ): String {
    //     TODO("Not yet implemented")
    // }
// }