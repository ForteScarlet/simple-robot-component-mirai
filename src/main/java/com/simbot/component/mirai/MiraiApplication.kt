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
import com.simbot.component.mirai.messages.MiraiMessageGet
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.message.data.At
import net.mamoe.mirai.message.data.firstIsInstanceOrNull
import java.io.Closeable
import java.util.concurrent.Executors
import java.util.function.Function

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
        config: MiraiConfiguration
) : SimpleRobotContext<MiraiBotSender, MiraiBotSender, MiraiBotSender, MiraiConfiguration>(
        sender,
        setter,
        getter,
        manager,
        msgParser,
        processor,
        dependCenter,
        config
), Closeable {
     /**
      * close able, close all bots
     */
    override fun close() {
         MiraiBots.closeAll()
    }

}

/**
 * mirai app
 */
interface MiraiApp: Application<MiraiConfiguration>


/**
 *
 * Mirai组件启动器
 *
 * @author ForteScarlet <\[email]ForteScarlet@163.com>
 * @since JDK1.8
 **/
class MiraiApplication : BaseApplication<MiraiConfiguration, MiraiBotSender, MiraiBotSender, MiraiBotSender, MiraiContext>() {

    @Deprecated("just see getRootSenderFunction", ReplaceWith("null"))
    override fun getSetter(msgGet: MsgGet?, botManager: BotManager?): MiraiBotSender? = null

    @Deprecated("just see getRootSenderFunction", ReplaceWith("null"))
    override fun getSender(msgGet: MsgGet?, botManager: BotManager?): MiraiBotSender? = null

    @Deprecated("just see getRootSenderFunction", ReplaceWith("null"))
    override fun getGetter(msgGet: MsgGet?, botManager: BotManager?): MiraiBotSender? = null

    /** 配置类实例 */
    private val config: MiraiConfiguration by lazy { MiraiConfiguration() }

    /**
     * 开发者实现的获取Config对象实例的方法
     * 此方法将会最先被执行，并会将值保存，使用时可使用[.getConf] 方法获取
     */
    override fun getConfiguration(): MiraiConfiguration = config


    /**
     * 开发者实现的资源初始化
     * 此方法将会在所有的初始化方法最后执行
     * 增加一个参数
     * 此资源配置将会在配置之后执行
     */
    override fun resourceInit(config: MiraiConfiguration) {  }

    /**
     * 开发者实现的资源初始化
     * 此方法将会在所有的无配置初始化方法最后执行
     * 将会在用户配置之前执行
     */
    override fun resourceInit() {
        registerMiraiAtFilter()
    }

    /**
     * 注册Mirai的at判断，追加当为[MiraiMessageGet]的时候的判断
     */
    private fun registerMiraiAtFilter(){
        ListenerFilter.updateAtDetectionFunction { old ->
            Function { msg ->
                if(msg is MiraiMessageGet<*>){
                    AtDetection { msg.message.firstIsInstanceOrNull<At>()?.target == msg.event.bot.id }
                }else{
                    old.apply(msg)
                }
            }
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
        return Function {
            //            var bot: Bot
            var contact: Contact? = null
            if (it is MiraiMessageGet<*>) {
                contact = it.contact
            }
            MultipleMiraiBotSender(contact, it, botManager, conf)
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
                                     dependCenter: DependCenter, config: MiraiConfiguration): MiraiContext = MiraiContext(defaultSenders.sender, defaultSenders.getter, defaultSenders.setter, manager, msgParser, processor, dependCenter, config)


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
        val defaultSender = DefaultMiraiBotSender(null, botManager, conf)
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
        // 启动服务，即注册监听
        MiraiBots.startListen(msgProcessor)

//         在一条新线程中执行挂起，防止程序终止
        Thread({
            runBlocking(Executors.newFixedThreadPool(1).asCoroutineDispatcher()) {
                MiraiBots.joinAll()
            }
            QQLog.debug("bots all shundown")
        }, "Mirai-bot-join").start()

        return "mirai bot server"
    }


    /**
     * 验证账号
     * @param code 用户账号，不可为null
     * @param info 用于验证的bot，使用启动的path作为密码来执行登录
     */
    override fun verifyBot(code: String, info: BotInfo): BotInfo {
        // 验证账号, 构建一个BotInfo
        // 如果验证失败，会抛出异常的
        try {
            QQLog.debug("验证账号$code...")
            return MiraiBotInfo(code, info.path, conf.botConfiguration)
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
    override fun close() {
        botManager.bots().forEach { it.close() }
    }

}









