/*
 * Copyright (c) 2020. ForteScarlet All rights reserved.
 * Project  component-mirai (Codes other than Mirai)
 * File     MiraiApplication.kt (Codes other than Mirai)
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

import com.forte.qqrobot.*
import com.forte.qqrobot.beans.messages.msgget.MsgGet
import com.forte.qqrobot.bot.BotInfo
import com.forte.qqrobot.bot.BotManager
import com.forte.qqrobot.depend.DependCenter
import com.forte.qqrobot.exception.BotVerifyException
import com.forte.qqrobot.listener.invoker.AtDetection
import com.forte.qqrobot.listener.invoker.ListenerFilter
import com.forte.qqrobot.listener.invoker.ListenerManager
import com.forte.qqrobot.log.QQLog
import com.forte.qqrobot.sender.senderlist.RootSenderList
import com.simbot.component.mirai.messages.*
import com.simbot.component.mirai.utils.ListenRegisterUtil
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.event.Listener
import net.mamoe.mirai.event.events.BotOfflineEvent
import net.mamoe.mirai.event.subscribeAlways
import net.mamoe.mirai.message.data.At
import java.util.concurrent.TimeUnit
import java.util.function.Function
import kotlin.reflect.KClass

/**
 * mirai的context
 */
class MiraiContext(
        sender: MiraiBotSender,
        setter: MiraiBotSender,
        getter: MiraiBotSender,
        manager: BotManager,
        msgParser: MsgParser,
        processor: MsgProcessor,
        dependCenter: DependCenter,
        config: MiraiConfiguration,
        application: MiraiApplication
) : SimpleRobotContext<MiraiBotSender, MiraiBotSender, MiraiBotSender, MiraiConfiguration, MiraiApplication>(
        sender,
        setter,
        getter,
        manager,
        msgParser,
        processor,
        dependCenter,
        config,
        application
)


/**
 * mirai app
 */
interface MiraiApp: Application<MiraiConfiguration> {
    override fun before(configuration: MiraiConfiguration)
}


/**
 *
 * Mirai组件启动器
 *
 * @author ForteScarlet <\[email]ForteScarlet@163.com>
 * @since JDK1.8
 **/
@Suppress("jol")
class MiraiApplication : BaseApplication<MiraiConfiguration, MiraiBotSender, MiraiBotSender, MiraiBotSender, MiraiApplication, MiraiContext>() {

    private lateinit var botThread: Thread

    @Volatile
    private var doClosed: Boolean = false

    @Deprecated("just see getRootSenderFunction", ReplaceWith("null"))
    override fun getSetter(msgGet: MsgGet?, botManager: BotManager?): MiraiBotSender? = null

    @Deprecated("just see getRootSenderFunction", ReplaceWith("null"))
    override fun getSender(msgGet: MsgGet?, botManager: BotManager?): MiraiBotSender? = null

    @Deprecated("just see getRootSenderFunction", ReplaceWith("null"))
    override fun getGetter(msgGet: MsgGet?, botManager: BotManager?): MiraiBotSender? = null

    /** 配置类实例 */
    private val config: MiraiConfiguration = MiraiConfiguration()

    /**
     * 是否注册额外监听成功
     */
    private var registeredSpecialListenerSuccess: Boolean = false

    /**
     * 开发者实现的获取Config对象实例的方法
     * 此方法将会最先被执行，并会将值保存，使用时可使用[getConf] 方法获取
     */
    override fun getConfiguration(): MiraiConfiguration = config


    /**
     * 开发者实现的资源初始化
     * 此方法将会在所有的初始化方法最后执行
     * 增加一个参数
     * 此资源配置将会在配置之后执行
     */
    override fun resourceInit(config: MiraiConfiguration) {
        registerMiraiEvent()
    }

    /**
     * 开发者实现的资源初始化
     * 此方法将会在所有的无配置初始化方法最后执行
     * 将会在用户配置之前执行
     */
    override fun resourceInit() {
        // add shutdown hook for close.
        Runtime.getRuntime().addShutdownHook(Thread{
            this.close()
        })
        registerMiraiAtFilter()
    }

    /**
     * 注册Mirai的at判断，追加当为[MiraiMessageGet]的时候的判断
     */
    private fun registerMiraiAtFilter(){
        ListenerFilter.updateAtDetectionFunction { old ->
            Function { msg ->
                if(msg is MiraiMessageGet<*>){
                    AtDetection {
                        (msg.message.find { msg -> msg is At } as? At)?.target == msg.event.bot.id
                    }
                }else{
                    old.apply(msg)
                }
            }
        }
    }

    /**
     * 注册mirai可提供的额外事件
     */
    private fun registerMiraiEvent(){
        val specialListeners: Map<String, KClass<out MsgGet>> = mapOf(
                // 头像变更事件
                MiraiEvents.friendAvatarChangedEvent to FriendAvatarChanged::class,
                // 昵称变更事件
                MiraiEvents.friendNicknameChangedEvent to FriendNicknameChanged::class,
                // 输入状态变更事件
                MiraiEvents.friendInputStatusChangedEvent to FriendInputStatusChanged::class,
                // bot离线事件
                MiraiEvents.botOfflineEvent to BotOffline::class,
                // bot重新登录事件
                MiraiEvents.botReloginEvent to BotRelogin::class,
                // 群名称变更事件
                MiraiEvents.groupNameChangedEvent to GroupNameChanged::class,
                // 群员群昵称变更事件
                MiraiEvents.memberRemarkChangedEvent to MemberRemarkChanged::class,
                // 群员群头衔变更事件
                MiraiEvents.memberSpecialTitleChangedEvent to MemberSpecialTitleChanged::class,
        )

        if(!ListenRegisterUtil.usable){
            val show = specialListeners.values.joinToString(",\n", transform = {
                it.simpleName as CharSequence
            })
            QQLog.warning("mirai.event.register.unavailable", show)
            QQLog.debug("mirai.event.register.unavailable", ListenRegisterUtil.cause, "")
            return
        }
        registeredSpecialListenerSuccess = true

        // 注册额外的事件
        specialListeners.forEach {
            ListenRegisterUtil.registerListen(it.key, it.value)
        }
    }

    /**
     * 根据 [getSender], [getSetter], [getGetter] 三个函数构建一个RootSenderList
     * 参数分别为一个[BotManager]和一个[MsgGet]对象
     * 如果组件不是分为三个部分而构建，则可以考虑重写此函数
     * 此函数最终会被送入组件实现的[runServer]中
     *
     * @return RootSenderList构建函数
     */
    override fun getRootSenderFunction(botManager: BotManager): Function<MsgGet, RootSenderList> {
        val cacheMaps = dependCenter.get(CacheMaps::class.java)
        val senderRunner = dependCenter.get(SenderRunner::class.java)
        return Function {
            //            var bot: Bot
            var contact: Contact? = null
            if (it is MiraiMessageGet<*>) {
                contact = it.contact
            }
            MultipleMiraiBotSender(contact, it, botManager, cacheMaps, senderRunner, registeredSpecialListenerSuccess, conf)
        }
    }

    /**
     * 获取一个组件专属的SimpleRobotContext对象
     * @param defaultSenders 函数[.getDefaultSenders]的最终返回值
     * @param manager       botManager对象
     * @param msgParser     消息字符串转化函数
     * @param processor     消息处理器
     * @param dependCenter  依赖中心
     * @return 组件的Context对象实例
     */
    override fun getComponentContext(defaultSenders: DefaultSenders<MiraiBotSender, MiraiBotSender, MiraiBotSender>,
                                     manager: BotManager,
                                     msgParser: MsgParser, processor: MsgProcessor,
                                     dependCenter: DependCenter, config: MiraiConfiguration): MiraiContext
            = MiraiContext(defaultSenders.sender, defaultSenders.getter, defaultSenders.setter, manager, msgParser, processor, dependCenter, config, this)


    @Deprecated("just see getDefaultSenders", ReplaceWith("null"))
    override fun getDefaultSetter(botManager: BotManager?): MiraiBotSender? = null

    @Deprecated("just see getDefaultSenders", ReplaceWith("null"))
    override fun getDefaultGetter(botManager: BotManager?): MiraiBotSender? = null

    @Deprecated("just see getDefaultSenders", ReplaceWith("null"))
    override fun getDefaultSender(botManager: BotManager?): MiraiBotSender? = null

    /**
     * 构建一个默认的RootSenderList
     * 参数分别为一个BotManager和一个MsgGet对象
     * 如果组件不是分为三个部分而构建，则可以考虑重写此函数
     * @return RootSenderList构建函数
     */
    override fun getDefaultSenders(botManager: BotManager): DefaultSenders<MiraiBotSender, MiraiBotSender, MiraiBotSender> {
        val cacheMaps = dependCenter.get(CacheMaps::class.java)
        val senderRunner = dependCenter.get(SenderRunner::class.java)
        val defaultSender = DefaultMiraiBotSender(null, cacheMaps, senderRunner, registeredSpecialListenerSuccess, botManager, conf)
        return DefaultSenders(defaultSender, defaultSender, defaultSender)
    }

    /**
     * 启动一个服务，这有可能是http或者是ws的监听服务
     * @param dependCenter   依赖中心
     * @param manager        监听管理器
     * @param msgProcessor   送信解析器
     * @return
     */
    override fun runServer(dependCenter: DependCenter, manager: ListenerManager, msgProcessor: MsgProcessor, msgParser: MsgParser): String {
        val cacheMaps = dependCenter.get(CacheMaps::class.java)
        // 启动服务，即注册监听
        MiraiBots.startListen(msgProcessor, cacheMaps, registeredSpecialListenerSuccess)

        if(config.autoRelogin) {
            // 如果要自动重启，此处注册自动重启事件
            MiraiBots.forEach {
                _, info ->
                val id: Long = info.bot.id
                // 注册被动下线事件
                info.bot.subscribeAlways<BotOfflineEvent.Dropped>(priority = Listener.EventPriority.HIGHEST) {
                    val app = this@MiraiApplication
                    if(this.bot.id == id && !app.doClosed){
                        QQLog.debug("mirai.relogin.off", this.cause, id)
                        bot.login()
                        QQLog.debug("mirai.relogin.on")
                    }
                }
            }
        }


        // 在一条新线程中执行挂起，防止程序终止
        botThread = Thread({
            val app = this
            while(!MiraiBots.empty && !app.isClosed){
                // QQLog.debug("join all............................")
                MiraiBots.joinAll()
                // QQLog.debug("join all end.")
            }
            QQLog.debug("bots all shutdown.")
        }, "Mirai-bot-join")
        botThread.start()
        return "mirai bot server"
    }


    /**
     * 验证账号
     * @param code 用户账号，不可为null
     * @param info 用于验证的bot，使用启动的path作为密码来执行登录
     */
    override fun verifyBot(code: String, info: BotInfo): BotInfo {
        val cacheMaps = dependCenter.get(CacheMaps::class.java)
        val senderRunner = dependCenter.get(SenderRunner::class.java)
        // 验证账号, 构建一个BotInfo
        // 如果验证失败，会抛出异常的
        try {
            QQLog.debug("验证账号$code...")
            return MiraiBotInfo(code, info.path, conf.botConfiguration(code), cacheMaps, senderRunner, registeredSpecialListenerSuccess)
        } catch (e: Exception) {
            throw BotVerifyException("failed", e, code, e.localizedMessage)
        }
    }


    /**
     * 字符串转化为MsgGet的方法，最终会被转化为[MsgParser]函数，
     * 此函数返回值永远为null
     */
    @Deprecated("no, just null", ReplaceWith("null"))
    override fun msgParse(str: String?): MsgGet? = null


    /**
     * 当关闭的时候执行的方法，会退出所有的bot，然后执行父类的close
     */
    override fun doClose() {
        doClosed = true
        MiraiBots.closeAll()
        val timeUnit = TimeUnit.SECONDS
        val waitTime = 30L
        // join 10 seconds
        QQLog.info("mirai.bot.thread.shutdown", waitTime, timeUnit.name.toLowerCase())
        // 等待1分钟
        botThread.join(TimeUnit.SECONDS.toMillis(waitTime))
        if(botThread.isAlive){
            QQLog.warning("mirai.bot.thread.mandatoryTermination")
            try{
                // 强制终止
                @Suppress("DEPRECATION")
                botThread.stop()
            }catch(e: Exception){
                QQLog.error(e)
            }
        }
        try{
            botThread.interrupt()
        }catch(e: Exception){
            QQLog.error(e)
        }
    }

}









