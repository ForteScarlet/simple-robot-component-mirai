package com.simbot.component.mirai

import com.forte.qqrobot.BaseConfiguration
import com.forte.qqrobot.bot.BotInfo
import com.forte.qqrobot.exception.ConfigurationException
import net.mamoe.mirai.utils.BotConfiguration

/**
 * Mirai配置类
 *
 * 可以配置各个缓存类的信息
 *
 */
class MiraiConfiguration: BaseConfiguration<MiraiConfiguration>(){
    /** mirai官方配置类，默认为其默认值 */
    val botConfiguration: BotConfiguration = BotConfiguration.Default

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


