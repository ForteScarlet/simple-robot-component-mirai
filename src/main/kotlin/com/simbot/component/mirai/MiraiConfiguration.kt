package com.simbot.component.mirai

import com.forte.qqrobot.BaseConfiguration
import com.forte.qqrobot.exception.ConfigurationException
import net.mamoe.mirai.utils.BotConfiguration

/**
 * Mirai配置类
 * 暂时没什么能配置的
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


}


