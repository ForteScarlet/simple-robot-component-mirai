/*
 * Copyright (c) 2020. ForteScarlet All rights reserved.
 * Project  component-mirai (Codes other than Mirai)
 * File     MiraiConfiguration.kt (Codes other than Mirai)
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
 */

package com.simbot.component.mirai

import cn.hutool.crypto.SecureUtil
import com.forte.config.Conf
import com.forte.qqrobot.BaseConfiguration
import com.forte.qqrobot.bot.BotInfo
import com.forte.qqrobot.exception.ConfigurationException
import net.mamoe.mirai.utils.BotConfiguration
import net.mamoe.mirai.utils.ExternalImage
import net.mamoe.mirai.utils.SystemDeviceInfo
import java.io.Serializable
import kotlin.coroutines.CoroutineContext
import kotlin.random.Random
import kotlin.random.nextInt

/**
 * Mirai配置类
 *
 *
 */
class MiraiConfiguration: BaseConfiguration<MiraiConfiguration>(){

    @Conf("mirai.senderType", comment = "送信器类型")
    var senderType: SenderRunnerType = SenderRunnerType.BLOCK


    /**
     * mirai官方配置类获取函数，默认为其默认值
     * 函数参数为bot的账号，得到一个config实例
     * */
    var botConfiguration: (String) -> BotConfiguration = {
        code ->
        val conf = BotConfiguration()
        conf.deviceInfo = { MiraiSystemDeviceInfo(code) }
//        conf.parentCoroutineContext = SimpleEmptyCoroutineContext()
        conf
    }

    /**
     * 通过实例设置configuration
     */
    fun setBotConfiguration(configuration: BotConfiguration){
        botConfiguration = { configuration }
    }

    /** 账号不可为null */
    override fun registerBot(botCode: String?, path: String?) {
        if(botCode == null){
            throw IllegalArgumentException("bot code can not be null.")
        }
        super.registerBot(botCode, path)
    }

    /** 变更切割方式 */
    override fun registerBotsFormatter(registerBots: String?) {
        if (registerBots?.isBlank() != false) {
            return
        }
        // 替换特殊字符：转义:\\, 逗号:\,
        var registerBotsStr = registerBots.replace("\\\\", "转义").replace("\\,", "逗号")

        // 根据逗号切割
        for (botInfo in registerBotsStr.split(",").toTypedArray()) {
            if (botInfo.isBlank()) {
                throw ConfigurationException("configuration 'core.bots' is malformed.")
            }
            val botInfoStr = botInfo.replace("逗号", ",").replace("转义", "\\")

            val first = botInfoStr.indexOf(":")
            val code = botInfoStr.substring(0, first).trim { it <= ' ' }
            var path = botInfoStr.substring(first + 1).trim { it <= ' ' }
            if (path.endsWith("/")) {
                path = path.substring(0, path.length - 1)
            }
            registerBot(code, path)
        }
    }

    /**
     * 获取预先注册的bot信息。
     */
    override fun getAdvanceBotInfo(): MutableMap<String, MutableList<BotInfo>> { // 如果没有任何信息，注册一个127:5700的默认地址
        // 将数据转化为map，key为bot的账号（如果存在的话）
        // 不存在账号信息的，key将会为null，只有key为null的时候，list才可以有多个参数，其余情况下，一个key只能对应一个地址。
        val botInfoMap: MutableMap<String, MutableList<BotInfo>> = mutableMapOf()
        // 不注册多次相同的code
        val pathSet: MutableSet<String> = mutableSetOf()

        for ((code, botInfo) in advanceBotInfo) {
            val botInfos = botInfoMap.computeIfAbsent(code) { mutableListOf() }
                if (botInfos.size > 0) { // 已经存在bot信息，抛出异常
                    throw ConfigurationException("Cannot register the same code multiple times: $code")
                } else {
                    // 有code
                    if (pathSet.add(code)) { // 保存成功，无重复code，则记录这个botInfo
                        botInfos.add(botInfo)
                    } else {
                        throw ConfigurationException("Cannot register the same code multiple times: $code")
                    }
                }
        }
        // 返回最终结果
        return botInfoMap
    }

}


/**
 * [SystemDeviceInfo] 实例，尝试着固定下随机值
 * @param code bot的账号
 */
open class MiraiSystemDeviceInfo
@JvmOverloads
constructor(code: String, seed: Long = 1): SystemDeviceInfo() {
    private val random: Random = Random(code.toLong() * seed)

    override val display: ByteArray = "MIRAI-SIMBOT.200122.001".toByteArray()
    override val product: ByteArray = "mirai-simbot".toByteArray()
    override val device: ByteArray = "mirai-simbot".toByteArray()
    override val board: ByteArray = "mirai-simbot".toByteArray()
    override val model: ByteArray = "mirai-simbot".toByteArray()

    override val fingerprint: ByteArray =
            "mamoe/mirai/mirai:10/MIRAI.200122.001/${getRandomString(7, '0'..'9', random)}:user/release-keys".toByteArray()
    override val bootId: ByteArray = ExternalImage.generateUUID(SecureUtil.md5().digest(getRandomByteArray(16, random))).toByteArray()
    override val procVersion: ByteArray =
            "Linux version 3.0.31-${getRandomString(8, random)} (android-build@xxx.xxx.xxx.xxx.com)".toByteArray()

    override val imsiMd5: ByteArray = SecureUtil.md5().digest(getRandomByteArray(16, random))
    override val imei: String = getRandomString(15, '0'..'9', random)
}


/*
 * 以下源代码修改自 net.mamoe.mirai.utils.SystemDeviceInfo.kt、
 *
 * 原源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

/**
 * 生成长度为 [length], 元素为随机 `0..255` 的 [ByteArray]
 */
internal fun getRandomByteArray(length: Int, r: Random): ByteArray = ByteArray(length) { r.nextInt(0..255).toByte() }

/**
 * 随机生成长度为 [length] 的 [String].
 */
internal fun getRandomString(length: Int, r: Random): String =
        getRandomString(length, r, *defaultRanges)

private val defaultRanges: Array<CharRange> = arrayOf('a'..'z', 'A'..'Z', '0'..'9')

/**
 * 根据所给 [charRange] 随机生成长度为 [length] 的 [String].
 */
internal fun getRandomString(length: Int, charRange: CharRange, r: Random): String =
        String(CharArray(length) { charRange.random(r) })

/**
 * 根据所给 [charRanges] 随机生成长度为 [length] 的 [String].
 */
internal fun getRandomString(length: Int, r: Random, vararg charRanges: CharRange): String =
        String(CharArray(length) { charRanges[r.nextInt(0..charRanges.lastIndex)].random(r) })



/**
 * An empty coroutine context.
 * @see kotlin.coroutines.EmptyCoroutineContext
 */
@SinceKotlin("1.3")
class SimpleEmptyCoroutineContext : CoroutineContext, Serializable {
    companion object {
        private const val serialVersionUID:Long = 0x0123456
    }
    override fun <E : CoroutineContext.Element> get(key: CoroutineContext.Key<E>): E? = null
    override fun <R> fold(initial: R, operation: (R, CoroutineContext.Element) -> R): R = initial
    override fun plus(context: CoroutineContext): CoroutineContext = context
    override fun minusKey(key: CoroutineContext.Key<*>): CoroutineContext = this
    override fun hashCode(): Int = 0
    override fun equals(other: Any?): Boolean {
        return other is SimpleEmptyCoroutineContext
    }
    public override fun toString(): String = "SimpleEmptyCoroutineContext"
}
