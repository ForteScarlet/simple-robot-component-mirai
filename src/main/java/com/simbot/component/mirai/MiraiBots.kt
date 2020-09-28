/*
 * Copyright (c) 2020. ForteScarlet All rights reserved.
 * Project  component-mirai (Codes other than Mirai)
 * File     MiraiBots2.kt (Codes other than Mirai)
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

import com.forte.qqrobot.MsgProcessor
import com.forte.qqrobot.bot.BotInfo
import com.forte.qqrobot.bot.BotSender
import com.forte.qqrobot.bot.LoginInfo
import com.forte.qqrobot.log.QQLog
import com.forte.qqrobot.sender.HttpClientHelper
import com.simbot.component.mirai.messages.MiraiLoginInfo
import com.simbot.component.mirai.utils.BotLevelUtil
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.isActive
import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.Bot
import net.mamoe.mirai.alsoLogin
import net.mamoe.mirai.closeAndJoin
import net.mamoe.mirai.join
import net.mamoe.mirai.utils.BotConfiguration
import net.mamoe.mirai.utils.MiraiLogger
import net.mamoe.mirai.utils.MiraiLoggerWithSwitch
import java.util.concurrent.ConcurrentHashMap

object MiraiBots {

    /**
     * 缓存bot的登录信息
     */
    internal val botSimpleInfo: MutableMap<Long, MiraiBotSimpleInfo> = ConcurrentHashMap()

    /** 判断[Bot]中是否存在登录的bot */
    val empty: Boolean get() = Bot.botInstances.isEmpty()

    @Volatile
    private var _closed = false
    val closed: Boolean get() = _closed

    /** 未注册监听的bot列表 */
    private val noListenBots: MutableSet<Long> = mutableSetOf()


    // val instances: List<Bot> get() = Bot.botInstances

    val instances: List<Bot> get() = botSimpleInfo.map { it.value.bot }

    @Volatile
    private lateinit var cacheMaps: CacheMaps
    @Volatile
    private lateinit var senderRunner: SenderRunner

    /** 消息处理器 */
    @Volatile
    private lateinit var msgProcessor: MsgProcessor


    fun contains(id: String): Boolean = Bot.getInstanceOrNull(id.toLong()) != null

    fun containsAndActive(id: String): Boolean = Bot.getInstanceOrNull(id.toLong())?.isActive != null

    fun containsAndOnline(id: String): Boolean = Bot.getInstanceOrNull(id.toLong())?.isOnline != null


    /**
     * 遍历 [Bot]
     */
    fun forEach(action: (String, Bot) -> Unit) {
        Bot.forEachInstance { action(it.id.toString(), it) }
    }


    /** 增加一个bot */
    fun set(bot: MiraiBotSimpleInfo, cacheMaps: CacheMaps, registeredSpecialListener: Boolean) {
        botSimpleInfo[bot.id] = bot
        // 注册或等待
        registerOrWait(bot, cacheMaps, registeredSpecialListener)
    }

    /** 注册监听或等待 */
    private fun registerOrWait(info: MiraiBotSimpleInfo, cacheMaps: CacheMaps, registeredSpecialListener: Boolean) {
        if (MiraiBots.started()) {
            // 启动了监听，注册
            val bot = Bot.getInstance(info.id)
            registerListen(bot, msgProcessor, cacheMaps, registeredSpecialListener)
        } else {
            noListenBots.add(info.id)
        }
    }

    /** get 根据id获取一个botInfo */
    fun getBotInfo(id: String): BotInfo? = Bot.getInstanceOrNull(id.toLong())?.toBotInfo(cacheMaps, senderRunner)

    fun getBotOrLogin(
        info: BotInfo, botConfiguration: (String) -> BotConfiguration,
        cacheMaps: CacheMaps, senderRunner: SenderRunner, registeredSpecialListener: Boolean
    ): Bot {
        val botId = info.botCode
        val containsAndOnline = containsAndOnline(botId)
        return if (containsAndOnline) {
            Bot.getInstance(botId.toLong())
        } else {
            MiraiBotInfo(info, botConfiguration(info.botCode), cacheMaps, senderRunner, registeredSpecialListener).bot
        }
    }

    /** 移除一个bot，移除的时候记得登出 */
    fun logout(id: String): Boolean = runBlocking {
        Bot.getInstanceOrNull(id.toLong())?.closeAndJoin() ?: return@runBlocking false
        true
    }


    /** 是否启用了监听，即判断消息处理器是否初始化 */
    fun started(): Boolean = ::msgProcessor.isInitialized

    /** 启用监听 */
    fun startListen(
        msgProcessor: MsgProcessor,
        cacheMaps: CacheMaps,
        senderRunner: SenderRunner,
        registeredSpecialListener: Boolean
    ) {
        // 初始化
        this.msgProcessor = msgProcessor
        this.cacheMaps = cacheMaps
        this.senderRunner = senderRunner

        // 等待区注册监听
        noListenBots.forEach {
            val bot: Bot = Bot.getInstance(it)
            registerListen(bot, msgProcessor, cacheMaps, registeredSpecialListener)
            val logger: MiraiLogger = bot.logger
            if (logger is MiraiLoggerWithSwitch) {
                // 如果是可开关的，开启日志
                logger.enable()
            }

            QQLog.debug("run.cache.contact", bot.id.toString())
            cacheMaps.contactCache.cache(bot)
        }
        // 清空等待区
        noListenBots.clear()
    }

    fun joinAll() {
        Bot.botInstances.forEach {
            runBlocking {
                it.join()
            }
        }
    }

    fun closeAll() {
        Bot.botInstances.map {
            GlobalScope.async {
                val id = it.id
                val idStr = it.id.toString()
                it.closeAndJoin()
                QQLog.debug("bot.close", idStr)
                botSimpleInfo.remove(id)
            }
        }.forEach {
            runBlocking { it.await() }
        }
    }

    /** 注册监听 */
    private fun registerListen(
        bot: Bot,
        msgProcessor: MsgProcessor,
        cacheMaps: CacheMaps,
        registeredSpecialListener: Boolean
    ) {
        bot.register(msgProcessor, cacheMaps, registeredSpecialListener)
    }
}


/**
 * 将一个已经存在的bot转化为BotInfo实例
 */
private fun Bot.toBotInfo(cacheMaps: CacheMaps, senderRunner: SenderRunner): BotInfo =
    MiraiBotProxyInfo(MiraiBots.botSimpleInfo[this.id]!!, cacheMaps, senderRunner)


/**
 * BotInfo 实例
 */
public data class MiraiBotProxyInfo(
    private val botSimpleInfo: MiraiBotSimpleInfo,
    private val cacheMaps: CacheMaps,
    private val senderRunner: SenderRunner,
) : BotInfo {
    override fun close() {
        runBlocking {
            Bot.getInstanceOrNull(botSimpleInfo.id)?.closeAndJoin()
        }
    }

    private lateinit var loginInfo: LoginInfo
    private lateinit var botSender: BotSender

    override fun getBotCode(): String = botSimpleInfo.id.toString()

    override fun getPath(): String = botSimpleInfo.pwd

    override fun getInfo(): LoginInfo {
        if (!::loginInfo.isInitialized) {
            val bot: Bot = Bot.getInstance(botSimpleInfo.id)
            val http = HttpClientHelper.getDefaultHttp()
            loginInfo = MiraiLoginInfo(bot, BotLevelUtil.level(bot, http))
        }
        return loginInfo
    }

    override fun getSender(): BotSender {
        if (!::loginInfo.isInitialized) {
            val bot: Bot = Bot.getInstance(botSimpleInfo.id)
            // val http = HttpClientHelper.getDefaultHttp()
            botSender = BotSender(MiraiBotSender(bot, null, cacheMaps, senderRunner))
        }
        return botSender


    }
}

/**
 * bot的简易info信息。
 *
 * @param bot 记录一个Bot实体, 以防止[Bot._instances]中的弱引用丢失
 */
data class MiraiBotSimpleInfo(internal val id: Long, internal val pwd: String, internal val bot: Bot)


/**
 * miraiBotInfo, 传入bot的账号密码，内部会启动一个Bot, 并记录简单信息。
 * @param id                // 账号
 * @param pwd               // 密码
 * @param botConfiguration  // 账号配置
 *
 */
public class MiraiBotInfo(
    private val id: String,
    private val pwd: String,
    private val botConfiguration: BotConfiguration,
    cacheMaps: CacheMaps,
    senderRunner: SenderRunner,
    registeredSpecialListener: Boolean
) : BotInfo {

    /** 使用info的构造 */
    constructor(
        botInfo: BotInfo, botConfiguration: BotConfiguration,
        cacheMaps: CacheMaps, senderRunner: SenderRunner,
        registeredSpecialListener: Boolean
    ) :
            this(botInfo.botCode, botInfo.path, botConfiguration, cacheMaps, senderRunner, registeredSpecialListener)


    /** bot信息, 通过Mirai获取 */
    val bot: Bot get() = Bot.getInstance(id.toLong())

    /** 送信器 */
    private val botSender: BotSender

    /**  登录信息 */
    private val loginInfo: LoginInfo

    init {
        // 先验证此bot是否已经被注册或是否仍然在线
        val containsAndOnline: Boolean = MiraiBots.containsAndOnline(id)

        if (containsAndOnline) {
            // 已经注册
            throw IllegalArgumentException("id [$id] has already login")
        }
        // 输入账号密码，填入配置，阻塞登录
        val bot: Bot = runBlocking {
            Bot(id.toLong(), pwd, botConfiguration).alsoLogin()
        }

        // 将自己的登录信息缓存在MiraiBots中
        MiraiBots.set(MiraiBotSimpleInfo(bot.id, pwd, bot), cacheMaps, registeredSpecialListener)

        // bot sender
        botSender = BotSender(MiraiBotSender(bot, null, cacheMaps, senderRunner))

        // login info
        val http = HttpClientHelper.getDefaultHttp()
        loginInfo = MiraiLoginInfo(bot, BotLevelUtil.level(bot, http))

        // 登录后，如果日志可以关闭，暂时关闭
        val logger = bot.logger
        if (logger is MiraiLoggerWithSwitch) {
            logger.disable()
        }
    }

    /**
     * 获取此bot的上报信息
     * 此处为账号的密码
     * @return bot上报信息
     */
    override fun getPath(): String = pwd

    /**
     * 获取此账号的登录信息
     * @return 获取登录信息
     */
    override fun getInfo(): LoginInfo = loginInfo

    /**
     * 获取Bot的账号信息
     * @return Bot账号信息
     */
    override fun getBotCode(): String = id

    /**
     * 获取当前bot所对应的送信器
     * @return 当前账号送信器
     */
    override fun getSender(): BotSender = botSender

    /**
     * 关闭bot，并从[MiraiBots]中移除此bot信息
     */
    override fun close() {
        // 关闭bot，并移除其相关信息
        runBlocking {
            val botId: Long = bot.id
            MiraiBots.botSimpleInfo.remove(botId)
            bot.closeAndJoin()
        }
    }

    /** 一直等待到bot下线 */
    fun join() {
        runBlocking { bot.join() }
    }

}

