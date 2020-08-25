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
import com.forte.qqrobot.bot.BotInfoImpl
import com.forte.qqrobot.exception.ConfigurationException
import com.simbot.component.mirai.logger.SimbotMiraiLogger
import net.mamoe.mirai.utils.*
import java.io.File
import java.util.AbstractMap.SimpleEntry
import kotlin.random.Random
import kotlin.random.nextInt

/**
 * Mirai配置类
 *
 *
 */
class MiraiConfiguration: BaseConfiguration<MiraiConfiguration>(){

    @field:Conf("mirai.senderType", comment = "送信器类型")
    var senderType: SenderRunnerType = SenderRunnerType.BLOCK

    /**
     * mirai心跳周期. 过长会导致被服务器断开连接. 单位毫秒
     * @see BotConfiguration.heartbeatPeriodMillis
     */
    @field:Conf("mirai.heartbeatPeriodMillis", comment = "mirai心跳周期. 过长会导致被服务器断开连接.")
    var heartbeatPeriodMillis: Long = BotConfiguration.Default.heartbeatPeriodMillis

    /**
     * 每次心跳时等待结果的时间.
     * 一旦心跳超时, 整个网络服务将会重启 (将消耗约 1s). 除正在进行的任务 (如图片上传) 会被中断外, 事件和插件均不受影响.
     * @see BotConfiguration.heartbeatTimeoutMillis
     */
    @field:Conf("mirai.heartbeatTimeoutMillis", comment = "每次心跳时等待结果的时间.")
    var heartbeatTimeoutMillis: Long = BotConfiguration.Default.heartbeatTimeoutMillis

    /** 心跳失败后的第一次重连前的等待时间. */
    @field:Conf("mirai.firstReconnectDelayMillis")
    var firstReconnectDelayMillis: Long = BotConfiguration.Default.firstReconnectDelayMillis

    /** 重连失败后, 继续尝试的每次等待时间 */
    @field:Conf("mirai.reconnectPeriodMillis")
    var reconnectPeriodMillis: Long = BotConfiguration.Default.reconnectPeriodMillis

    /** 最多尝试多少次重连 */
    @field:Conf("mirai.reconnectionRetryTimes")
    var reconnectionRetryTimes: Int = BotConfiguration.Default.reconnectionRetryTimes


    /** 使用协议类型 */
    @field:Conf("mirai.protocol")
    var protocol: BotConfiguration.MiraiProtocol = BotConfiguration.Default.protocol

    /** 关闭mirai bot logger */
    @field:Conf("mirai.noBotLog")
    var noBotLog: Boolean = false

    /** 关闭mirai网络日志 */
    @field:Conf("mirai.noNetworkLog")
    var noNetworkLog: Boolean = false

    /** mirai bot log切换使用simbot的log */
    @field:Conf("mirai.useSimbotBotLog")
    var useSimbotBotLog: Boolean = false

    /** mirai 网络log 切换使用simbot的log */
    @field:Conf("mirai.useSimbotNetworkLog")
    var useSimbotNetworkLog: Boolean = false

    /** mirai配置自定义deviceInfoSeed的时候使用的随机种子。默认为1.  */
    @field:Conf("mirai.deviceInfoSeed", comment = "mirai配置自定义deviceInfoSeed的时候使用的随机种子。默认为1. ")
    var deviceInfoSeed: Long = 1L


    @field:Conf("mirai.autoRelogin", comment = "是否在bot掉线的时候自动重启.")
    var autoRelogin: Boolean = false

    @field:Conf("mirai.cacheType", comment = "mirai的缓存策略。可选为FILE和MEMORY")
    var cacheType: MiraiCacheType = MiraiCacheType.FILE


    @field:Conf("mirai.cacheDirectory", comment = "如果cacheType为FILE，则此处为路径。如果不填或者为null则默认为系统临时文件夹.")
    var cacheDirectory: String? = null


    /**
     * 在[botConfiguration]配置完成后可以提供一些后置处理进行配置覆盖.
     */
    var postBotConfigurationProcessor: (String, BotConfiguration) -> BotConfiguration = {_,botConfiguration -> botConfiguration }

    /**
     * mirai官方配置类获取函数，默认为其默认值
     * 函数参数为bot的账号，得到一个config实例
     * 如果需要对[BotConfiguration]做自定义处理，请使用[postBotConfigurationProcessor]
     *
     * java 则使用
     * ```java
     * setPostBotConfigurationProcessor(code, conf -> {
     *      // do some for conf
     *      return conf;
     * })
     * ```
     *
     * @see postBotConfigurationProcessor
     * */
    @set:Deprecated("use setPostBotConfigurationProcessor((code, conf) -> {...})")
    var botConfiguration: (String) -> BotConfiguration = {
        code ->
        val conf = BotConfiguration()
        conf.deviceInfo = { MiraiSystemDeviceInfo(code, deviceInfoSeed) }
        conf.heartbeatPeriodMillis = this.heartbeatPeriodMillis
        conf.heartbeatTimeoutMillis = this.heartbeatTimeoutMillis
        conf.firstReconnectDelayMillis = this.firstReconnectDelayMillis
        conf.reconnectPeriodMillis = this.reconnectPeriodMillis
        conf.reconnectionRetryTimes = this.reconnectionRetryTimes
        conf.protocol = this.protocol
        conf.fileCacheStrategy = when(this.cacheType) {
            // 内存缓存
            MiraiCacheType.MEMORY -> FileCacheStrategy.MemoryCache
            // 文件缓存
            MiraiCacheType.FILE, -> {
                val cacheDir = this.cacheDirectory
                if(cacheDir?.isNotBlank() == true){
                    val directory = File(cacheDir)
                    if(!directory.exists()){
                        // 不存在，创建
                        directory.mkdirs()
                    }
                    if(!directory.isDirectory){
                        throw IllegalArgumentException("'$cacheDir' is not a directory.")
                    }
                    FileCacheStrategy.TempCache(directory)
                }else{
                    FileCacheStrategy.MemoryCache
                }
            }
        }


        if(noBotLog){
            conf.noBotLog()
        }
        if(noNetworkLog){
            conf.noNetworkLog()
        }
        // MiraiLoggerWithSwitch
        // 默认情况下都是关闭状态的log
        if(useSimbotBotLog){
            conf.botLoggerSupplier = { SimbotMiraiLogger.withSwitch(true) }
        }else{
            val oldBotLoggerSup = conf.botLoggerSupplier
            conf.botLoggerSupplier = {
                val logger = oldBotLoggerSup(it)
                if(logger is MiraiLoggerWithSwitch) logger else logger.withSwitch(true)
            }
        }
        if(useSimbotNetworkLog){
            conf.networkLoggerSupplier = { SimbotMiraiLogger.withSwitch(true) }
        }else{
            val oldNetworkLoggerSup = conf.networkLoggerSupplier
            conf.networkLoggerSupplier = {
                val logger = oldNetworkLoggerSup(it)
                if(logger is MiraiLoggerWithSwitch) logger else logger.withSwitch(true)
            }
        }

        // post processor
        postBotConfigurationProcessor(code, conf)
    }
    @Suppress("SetterBackingFieldAssignment")
    set(value) {
        postBotConfigurationProcessor = {
            code, _ ->
            value(code)
        }
    }


    /**
     * 通过实例设置configuration.
     * 此方法将会完全覆盖原有的所有默认配置。
     */
    fun setBotConfiguration(configuration: BotConfiguration){
        val confNetworkLoggerSup = configuration.networkLoggerSupplier
        val confBotLoggerSup = configuration.botLoggerSupplier
        // 设置日志开关
        postBotConfigurationProcessor = {_,_ ->
            configuration.networkLoggerSupplier = {
                val netWorkLogger = confNetworkLoggerSup(it)
                if (netWorkLogger is MiraiLoggerWithSwitch) netWorkLogger else netWorkLogger.withSwitch(true)
            }
            configuration.botLoggerSupplier = {
                val botLogger = confBotLoggerSup(it)
                if (botLogger is MiraiLoggerWithSwitch) botLogger else botLogger.withSwitch(true)
            }
            configuration
        }
    }

    /** 账号不可为null */
    override fun registerBot(botCode: String?, path: String) {
        if(botCode == null){
            throw IllegalArgumentException("bot code can not be null.")
        }
        doRegisterBot(botCode, path)
    }

    /**
     * 使用[FixBotInfoImpl]来代替[BotInfoImpl]
     */
    override fun registerBotAsDefault(botCode: String, path: String) {
        val botInfo = FixBotInfoImpl(botCode, path, null, null)
        setDefaultBotInfo(botInfo)
        // 注册一个bot信息
        advanceBotInfo.add(SimpleEntry<String, BotInfo>(botCode, botInfo))
    }

    /**
     * 使用[FixBotInfoImpl]来代替[BotInfoImpl]
     */
    private fun doRegisterBot(botCode: String, path: String){
        val botInfo = FixBotInfoImpl(botCode, path, null, null)
        if (getDefaultBotInfo() == null) {
            setDefaultBotInfo(botInfo)
        }
        // 注册一个bot信息
        advanceBotInfo.add(SimpleEntry<String, BotInfo>(botCode, botInfo))
    }

    /** 变更切割方式 */
    override fun registerBotsFormatter(registerBots: String?) {
        if (registerBots?.isBlank() != false) {
            return
        }
        // 替换特殊字符：转义:\\, 逗号:\,
        val registerBotsStr = registerBots.replace("\\\\", "转义").replace("\\,", "逗号")

        // 根据逗号切割
        for (botInfo in registerBotsStr.split(",").toTypedArray()) {
            if (botInfo.isBlank()) {
                throw ConfigurationException("configuration 'core.bots' is malformed.")
            }
            val botInfoStr = botInfo.replace("逗号", ",").replace("转义", "\\")

            val first = botInfoStr.indexOf(":")
            val code = botInfoStr.substring(0, first).trim { it <= ' ' }
            val path = botInfoStr.substring(first + 1).trim { it <= ' ' }
//            if (path.endsWith("/")) {
//                path = path.substring(0, path.length - 1)
//            }
//            println(path)
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
constructor(
        code: Long,
        seed: Long,
        randomFactory: (code: Long, seed: Long) -> Random = { c,s -> Random(c * s) }
): SystemDeviceInfo() {
    constructor(codeId: String, seedNum: Long): this(codeId.toLong(), seedNum)
    constructor(codeId: String, seedNum: Long, randomFactory: (code: Long, seed: Long) -> Random):
            this(codeId.toLong(), seedNum, randomFactory)


    private val random: Random = randomFactory(code, seed)


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
 * mirai的图片文件缓存策略
 */
enum class MiraiCacheType {
    /** 文件缓存 */
    FILE,
    /** 内存缓存 */
    MEMORY
}
