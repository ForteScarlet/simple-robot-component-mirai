/*
 * Copyright (c) 2020. ForteScarlet All rights reserved.
 * Project  component-mirai (Codes other than Mirai)
 * File     MiraiBots.kt (Codes other than Mirai)
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
import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.Bot
import net.mamoe.mirai.alsoLogin
import net.mamoe.mirai.closeAndJoin
import net.mamoe.mirai.join
import net.mamoe.mirai.utils.BotConfiguration
import net.mamoe.mirai.utils.MiraiLoggerWithSwitch
import java.util.concurrent.ConcurrentHashMap


/**
 * 记录登录的bot列表以及监听注册的状态
 * 一般会通过[com.forte.qqrobot.bot.BotManager]类记录注册信息，此处记录实际的Bot信息
 */
object MiraiBots {

    /** 记录登录的bot */
    private val bots: MutableMap<String, MiraiBotInfo> = ConcurrentHashMap()

    /** 消息处理器 */
    @Volatile
    private lateinit var msgProcessor: MsgProcessor

    /** 未注册监听的bot列表 */
    private val noListenBots: MutableMap<String, MiraiBotInfo> = ConcurrentHashMap()

    /**
     * 遍历[MiraiBotInfo]
     */
    fun forEach(action: (String, MiraiBotInfo) -> Unit) {
        bots.forEach(action)
    }

    /** 增加一个bot，如果此bot已经存在则会抛出异常 */
    fun set(id: String, bot: MiraiBotInfo, cacheMaps: CacheMaps){
        bots[id] = bot
        // 注册或等待
        registerOrWait(bot, cacheMaps)
    }

    /**
     * 尝试获取一个bot，如果获取不到则会尝试构建一个。
     * 需要在从BotManager中验证存在后在通过此获取，否则BotManager中可能会缺失
     */
    fun get(info: BotInfo, botConfiguration: (String) -> BotConfiguration, cacheMaps: CacheMaps, senderRunner: SenderRunner): MiraiBotInfo {
        val id = info.botCode
        // 构建一个，构建失败会抛出异常
        val miraiBotInfo = bots[id]
        return if(miraiBotInfo == null){
            // 不存在，尝试获取
            val newBotInfo = MiraiBotInfo(info, botConfiguration(id), cacheMaps, senderRunner)
            // 注册/等待并返回
            registerOrWait(newBotInfo, cacheMaps)
            newBotInfo
        }else{
            miraiBotInfo
        }
    }

    /** 注册监听或等待 */
    private fun registerOrWait(info: MiraiBotInfo, cacheMaps: CacheMaps){
        if(started()){
            // 启动了监听，注册
            registerListen(info, cacheMaps)
        }else{
            noListenBots[info.botCode] = info
        }
    }

    /** get 根据id获取一个botInfo */
    operator fun get(id: String) = bots[id]


    /** 移除一个bot，移除的时候记得登出 */
    fun remove(id: String): MiraiBotInfo? = bots.remove(id)


    /** 是否启用了监听，即判断消息处理器是否初始化 */
    fun started(): Boolean = ::msgProcessor.isInitialized


    /** 启用监听 */
    fun startListen(msgProcessor: MsgProcessor, cacheMaps: CacheMaps){
        // 初始化
        this.msgProcessor = msgProcessor
        // 等待区注册监听
        noListenBots.forEach{
            registerListen(it.value, cacheMaps)
            val bot = it.value.bot
            val logger = bot.logger
            if(logger is MiraiLoggerWithSwitch){
                // 如果是可开关的，开启日志
                logger.enable()
            }

            QQLog.debug("run.cache.contact", bot.id.toString())
            cacheMaps.contactCache.cache(bot)
        }
        // 清空等待区
        noListenBots.clear()
    }

    /** 注册监听 */
    private fun registerListen(info: MiraiBotInfo, cacheMaps: CacheMaps){
        info.register(msgProcessor, cacheMaps)
    }

    /** 等待所有bot下线 */
    suspend fun joinAll(){
        while(bots.isNotEmpty()){
            bots.forEach {
                it.value.join()
            }
        }
    }

    fun closeAll(){
        bots.forEach { it.value.close() }
        bots.clear()
    }


}


/**
 * miraiBotInfo, 传入bot的账号密码，内部会创建并启动一个Bot
 * @param id                // 账号
 * @param pwd               // 密码
 * @param botConfiguration  // 账号配置
 *
 */
class MiraiBotInfo(private val id: String,
                   private val pwd: String,
                   private val botConfiguration: BotConfiguration,
                   cacheMaps: CacheMaps,
                   senderRunner: SenderRunner
): BotInfo {

    /** 使用info的构造 */
    constructor(botInfo: BotInfo, botConfiguration: BotConfiguration,
                cacheMaps: CacheMaps, senderRunner: SenderRunner): this(botInfo.botCode, botInfo.path, botConfiguration, cacheMaps, senderRunner)

    /** bot信息 */
    val bot: Bot
    /** 送信器 */
    val botSender: BotSender
    /**  登录信息 */
    val loginInfo: LoginInfo

    init {
        // 先验证此bot是否已经被注册或是否仍然在线
        val botGet = MiraiBots[id]
        if(botGet != null && botGet.bot.isOnline){
            // 已经注册
            throw IllegalArgumentException("id [$id] has already login")
        }
        // 输入账号密码，填入配置，阻塞登录
        bot = runBlocking { Bot(id.toLong(), pwd, botConfiguration).alsoLogin() }
        // 将自己记录在MiraiBots中
        MiraiBots.set(id, this, cacheMaps)
        // bot sender
        botSender = BotSender(MiraiBotSender(bot, null, cacheMaps, senderRunner))

        // login info
        val http = HttpClientHelper.getDefaultHttp()
        loginInfo = MiraiLoginInfo(bot, BotLevelUtil.level(bot, http))

        // 登录后，如果日志可以关闭，暂时关闭
        val logger = bot.logger
        if(logger is MiraiLoggerWithSwitch){
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
            // close and remove
            bot.closeAndJoin()
            MiraiBots.remove(id)
        }
    }

    /** 一直等待到bot下线 */
    suspend fun join(){
        bot.join()
    }

}






