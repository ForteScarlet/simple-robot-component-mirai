/*
 *
 * Copyright (c) 2020. ForteScarlet All rights reserved.
 * Project  component-mirai
 * File     MiraiBotSender.kt
 *
 * You can contact the author through the following channels:
 *  github https://github.com/ForteScarlet
 *  gitee  https://gitee.com/ForteScarlet
 *  email  ForteScarlet@163.com
 *  QQ     1149159218
 *  The Mirai code is copyrighted by mamoe-mirai
 *  you can see mirai at https://github.com/mamoe/mirai
 *
 *
 */

package com.simbot.component.mirai

import com.forte.qqrobot.beans.messages.GroupCodeAble
import com.forte.qqrobot.beans.messages.QQCodeAble
import com.forte.qqrobot.beans.messages.ThisCodeAble
import com.forte.qqrobot.beans.messages.result.*
import com.forte.qqrobot.beans.messages.types.GroupAddRequestType
import com.forte.qqrobot.bot.BotInfo
import com.forte.qqrobot.bot.BotManager
import com.forte.qqrobot.log.QQLog
import com.forte.qqrobot.sender.HttpClientHelper
import com.forte.qqrobot.sender.senderlist.BaseRootSenderList
import com.simbot.component.mirai.messages.*
import com.simbot.component.mirai.utils.BotLevelUtil
import com.simbot.component.mirai.utils.sendMsg
import kotlinx.coroutines.*
import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.contact.mute
import net.mamoe.mirai.event.events.BotInvitedJoinGroupRequestEvent
import net.mamoe.mirai.event.events.MemberJoinRequestEvent
import net.mamoe.mirai.getFriendOrNull


/**
 * 送信器对于可挂起函数的执行策略
 */
interface SenderRunner {
    fun <T> run(coroutineScope: CoroutineScope = GlobalScope, runner: suspend CoroutineScope.() -> T): T?
}


/**
 * 阻塞送信
 */
object BlockSenderRunner : SenderRunner {
    override fun <T> run(coroutineScope: CoroutineScope, runner: suspend CoroutineScope.() -> T): T? {
        return runBlocking(block = runner)
    }
}

/**
 * 协程launch送信
 */
object CoroutineLaunchSenderRunner : SenderRunner {
    override fun <T> run(coroutineScope: CoroutineScope, runner: suspend CoroutineScope.() -> T): T? {
        coroutineScope.launch { runner(this) }
        return null
    }
}

/**
 * 协程Async送信
 */
object CoroutineAsyncSenderRunner : SenderRunner {
    override fun <T> run(coroutineScope: CoroutineScope, runner: suspend CoroutineScope.() -> T): T? = runBlocking {
        withContext(coroutineScope.coroutineContext) { runner(this) }
    }
}


/**
 * [BlockSenderRunner] [CoroutineLaunchSenderRunner]
 */
enum class SenderRunnerType(val runnerGetter: () -> SenderRunner) {
    BLOCK({ BlockSenderRunner }), COROUTINE({ CoroutineLaunchSenderRunner }), ASYNC({ CoroutineAsyncSenderRunner })


}


/**
 * mirai bot sender
 * @param bot 虽然可以为null，但是此null仅为子类重写而用，构建此类不可使用null值。
 * @author ForteScarlet <\[email]ForteScarlet@163.com>
 **/
open class MiraiBotSender(
    bot: Bot?, val contact: Contact? = null,
    // 缓存Map列表
    protected val cacheMaps: CacheMaps,
    protected val senderRunner: SenderRunner

) : BaseRootSenderList() {

    /** 幕后真实字段 */
    private val _bot: Bot? = bot

    /** 获取到bot对象 */
    open val bot: Bot
        get() = _bot!!


    /** 获取登录信息 */
    override fun getLoginQQInfo(): LoginQQInfo {
        val http = HttpClientHelper.getDefaultHttp()
        return MiraiLoginInfo(bot, BotLevelUtil.level(bot, http))
    }

    /** 获取群链接列表 不支持的API */
    @Deprecated("Unsupported API: groupLinkList")
    override fun getGroupLinkList(group: String, number: Int) = super.getGroupLinkList(group, number)

    /** 禁言列表*/
    override fun getBanList(group: String): BanList = MiraiGroupBanList(bot.getGroup(group.toLong()))

    /** 群作业列表 */
    @Deprecated("Unsupported API: groupHomeworkList")
    override fun getGroupHomeworkList(group: String?, number: Int) = super.getGroupHomeworkList(group, number)

    /** 匿名人信息 */
    @Deprecated("Unsupported API: groupHomeworkList")
    override fun getAnonInfo(flag: String?) = super.getAnonInfo(flag)

    /** 群信息 */
    override fun getGroupInfo(group: String, cache: Boolean): GroupInfo = MiraiGroupInfo(bot.getGroup(group.toLong()))

    /** 群成员信息 */
    override fun getGroupMemberInfo(group: String, QQ: String, cache: Boolean): GroupMemberInfo =
        MiraiGroupMemberInfo(bot.getGroup(group.toLong())[QQ.toLong()])

    /** 群成员列表 */
    override fun getGroupMemberList(group: String): GroupMemberList = MiraiGroupMemberList(bot.getGroup(group.toLong()))

    /** 群进群公告 */
    override fun getGroupTopNote(group: String): GroupTopNote = MiraiGroupTopNote(bot.getGroup(group.toLong()))

    /**
     * 获取权限信息
     */
    override fun getAuthInfo(): AuthInfo = MiraiAuthInfo(bot)

    /**
     * 公告列表，就返回一个TopNote
     */
    override fun getGroupNoteList(group: String, number: Int): GroupNoteList =
        MiraiGroupNoteList(bot.getGroup(group.toLong()))

    /** 群共享文件列表 */
    @Deprecated("Unsupported API: shareList")
    override fun getShareList(group: String?): ShareList = super.getShareList(group)

    /** 获取图片信息 */
    @Deprecated("Unsupported API: imageInfo")
    override fun getImageInfo(flag: String?): ImageInfo = super.getImageInfo(flag)

    /** 群列表 */
    override fun getGroupList(): GroupList = MiraiGroupList(bot.groups)

    /**
     * 陌生人信息
     * @param QQ qq号。说是陌生人信息，但是mirai不能获取陌生人的消息，只能获取好友的。
     * @param cache 此参数无效
     */
    override fun getStrangerInfo(QQ: String, cache: Boolean): StrangerInfo = MiraiFriends(bot.getFriend(QQ.toLong()))

    /** 获取群文件信息 */
    @Deprecated("Unsupported API: getFileInfo")
    override fun getFileInfo(flag: String?): FileInfo = super.getFileInfo(flag)

    /** 获取好友列表 */
    override fun getFriendList(): FriendList = MiraiFriendList(bot.friends)

    /** 群签到 */
    override fun setGroupSign(group: String?): Boolean = super.setGroupSign(group)

    /** 签到 */
    @Deprecated("Unsupported API: setSign")
    override fun setSign(): Boolean = super.setSign()

    /** 讨论组消息，直接使用群消息发送 */
    @Deprecated("just send group msg", ReplaceWith("sendGroupMsg(group, msg)"))
    override fun sendDiscussMsg(group: String, msg: String): String? = sendGroupMsg(group, msg)

    /** 发送群消息 */
    private fun sendGroupMsg(group: Long, msg: String): String? {
        bot.nudge()
        val g = bot.getGroup(group.toLong())
        // 阻塞发送
        val result = senderRunner.run {
            g.sendMsg(msg, cacheMaps)
        }
        // 缓存消息id并返回
        return if (result != null) cacheMaps.recallCache.cache(result) else null
    }

    /** 发送群消息 */
    override fun sendGroupMsg(group: String, msg: String): String? =
        sendGroupMsg(group.toLong(), msg)

    /** 发送群消息 */
    override fun sendGroupMsg(groupCode: GroupCodeAble, msg: String): String? =
        sendGroupMsg(groupCode.groupCodeNumber, msg)


    /**
     * 发送群消息
     * @param code Long
     * @param msg String
     * @return String?
     */
    private fun sendPrivateMsg(code: Long, msg: String): String? {
        // 没有这个人则可能抛出异常
        // 默认认为是给好友发消息
        val to: Contact = bot.getFriendOrNull(code) ?: run {
            if (contact != null) {
                // 回复此member
                if (contact is Member && contact.id == code) {
                    return@run contact
                }
                if (contact is Group) {
                    return@run contact.getOrNull(code) ?: run {
                        // 可能不是这个群里的人，开始缓存查询，查询不到缓存则会抛出异常
                        cacheMaps.contactCache[code, bot] ?: throw NoSuchElementException("friend or member $code")
                    }
                }
                // 一般没有其他可能了。如果有，直接查询所有群
                cacheMaps.contactCache[code, bot] ?: throw NoSuchElementException("friend or member $code")
            } else {
                // 查询所有群
                cacheMaps.contactCache[code, bot] ?: throw NoSuchElementException("friend or member $code")
            }


        }

        val result = senderRunner.run {
            to.sendMsg(msg, cacheMaps)
        }
        // 缓存消息id并返回
        return if (result != null) cacheMaps.recallCache.cache(result) else null
    }

    /** 发送私信消息 */
    override fun sendPrivateMsg(QQ: String, msg: String): String? = sendPrivateMsg(QQ.toLong(), msg)


    /** 发送私聊消息 */
    override fun sendPrivateMsg(qqCode: QQCodeAble, msg: String): String? = sendPrivateMsg(qqCode.codeNumber, msg)


    /** 发布群公告 */
    @Deprecated("Unsupported API: sendGroupNotice")
    override fun sendGroupNotice(
        group: String?,
        title: String?,
        text: String?,
        top: Boolean,
        toNewMember: Boolean,
        confirm: Boolean
    ): Boolean = super.sendGroupNotice(group, title, text, top, toNewMember, confirm)


    /** 点赞 */
    @Deprecated("Unsupported API: sendLike")
    override fun sendLike(QQ: String?, times: Int): Boolean = super.sendLike(QQ, times)


    /** 设置全群禁言 */
    override fun setGroupWholeBan(group: String, `in`: Boolean): Boolean {
        val groupId = group.toLong()
        val settings = bot.getGroup(groupId).settings
        settings.isMuteAll = `in`
        return true
    }



    /** 设置匿名聊天ban */
    @Deprecated("Unsupported API: setGroupAnonymousBan")
    override fun setGroupAnonymousBan(group: String?, flag: String?, time: Long): Boolean {
        setGroupAnonymousBanWarning
        return this.setGroupBan(group!!, flag!!, time)
    }


    /** 踢出群员 */
    override fun setGroupMemberKick(group: String, QQ: String, dontBack: Boolean): Boolean {
        senderRunner.run {
            bot.getGroup(group.toLong())[QQ.toLong()].kick()
        }
        return true
    }


    /** 退出讨论组，直接使用退出群 */
    @Deprecated("just see group leave", ReplaceWith("setGroupLeave(group, false)"))
    override fun setDiscussLeave(group: String): Boolean {
        setDiscussLeaveWarning
        return setGroupLeave(group, false)
    }

    /** 退群 */
    override fun setGroupLeave(group: String, dissolve: Boolean): Boolean {
        val g = bot.getGroup(group.toLong())
        // 如果为解散, 似乎不支持解散
//        if(dissolve){
//            g.quit()
//        }else{
        return senderRunner.run { g.quit() } ?: true
//        }
    }


    /** 设置/取消管理员 */
    @Deprecated("Unsupported API: setGroupAdmin")
    override fun setGroupAdmin(group: String, QQ: String, set: Boolean): Boolean {
        return super.setGroupAdmin(group, QQ, set)
    }


    /** 设置群匿名聊天 */
    override fun setGroupAnonymous(group: String, agree: Boolean): Boolean {
        val settings = bot.getGroup(group.toLong()).settings
//        if(settings.isAllowMemberInvite != agree){
        settings.isAllowMemberInvite = agree
//        }
        return true
    }

    /** 处理好友申请 */
    override fun setFriendAddRequest(flag: String, friendName: String?, agree: Boolean): Boolean {
        val botId = bot.id
        val request = cacheMaps.requestCache.getFriendRequest(botId, flag)
        return if (request != null) {
            if (agree) {
                senderRunner.run { request.accept() }
            } else {
                senderRunner.run { request.reject(false) }
            }
            cacheMaps.requestCache.removeFriendRequest(botId, flag)
            true
        } else {
            false
        }
    }

    /** 处理加群申请 */
    override fun setGroupAddRequest(
        flag: String,
        requestType: GroupAddRequestType,
        agree: Boolean,
        why: String
    ): Boolean {
        val botId = bot.id
        val request = cacheMaps.requestCache.getJoinRequest(botId, flag)
        return if (request != null) {

            when (request) {
                // 是加群申请
                is MemberJoinRequestEvent -> {
                    if (agree) {
                        // 同意
                        senderRunner.run { request.accept() }
                    } else {
                        // 不同意
                        senderRunner.run { request.reject(false) }
                    }
                    cacheMaps.requestCache.removeJoinRequest(botId, flag)
                    true
                }
                // 是别人的邀请
                is BotInvitedJoinGroupRequestEvent -> {
                    if (agree) {
                        // 同意
                        senderRunner.run { request.accept() }
                    } else {
                        // 不同意, 即忽略
                        senderRunner.run { request.ignore() }
                    }
                    cacheMaps.requestCache.removeJoinRequest(botId, flag)
                    true
                }
                else -> {
                    throw IllegalArgumentException("unknown join request type: $request")
                }
            }
        } else {
            false
        }
    }

    /** 删除群文件 */
    @Deprecated("Unsupported API: setGroupFileDelete")
    override fun setGroupFileDelete(group: String?, flag: String?): Boolean = super.setGroupFileDelete(group, flag)


    /** 撤回消息 */
    override fun setMsgRecall(flag: String): Boolean {
        val botId = bot.id
        val source = cacheMaps.recallCache.get(flag, botId)
        return if (source != null) {
            // 有
            senderRunner.run {
                bot.recall(source)
            }
            cacheMaps.recallCache.remove(flag, botId)
            true
        } else false
    }

    /** 设置群昵称 */
    override fun setGroupCard(group: String, QQ: String, card: String): Boolean {
        bot.getGroup(group.toLong())[QQ.toLong()].nameCard = card
        return true
    }

    /** 设置专属头衔 */
    override fun setGroupExclusiveTitle(group: String, QQ: String, title: String, time: Long): Boolean {
        bot.getGroup(group.toLong())[QQ.toLong()].specialTitle = title
        return true
    }

    /** 设置群禁言 */
    override fun setGroupBan(group: String, QQ: String, time: Long): Boolean {
        senderRunner.run {
            bot.getGroup(group.toLong())[QQ.toLong()].mute(time)
        }
        return true
    }

    /** 送花 */
    @Deprecated("Unsupported API: sendFlower")
    override fun sendFlower(group: String?, QQ: String?): Boolean = super.sendFlower(group, QQ)

}


/**
 * 可动态切换当前bot的sender。主要通过[com.forte.qqrobot.bot.BotManager]和[MiraiBots]获取并切换
 */
open class MultipleMiraiBotSender(
    contact: Contact? = null,
    private val thisCodeAble: ThisCodeAble,
    private val botManager: BotManager,
    cacheMaps: CacheMaps,
    senderRunner: SenderRunner,
    private val registeredSpecialListener: Boolean,
    private val conf: MiraiConfiguration
) : MiraiBotSender(null, contact, cacheMaps, senderRunner) {

    /**
     * 上一次获取的bot
     */
    private var _bot: Bot? = null

    /** 通过thisCode动态获取 */
    override val bot: Bot
        get() {
            val id = thisCodeAble.thisCode
            val last = _bot
            if (last != null && id == last.id.toString()) {
                return last
            }
            val info: BotInfo? = botManager.getBot(id)
            return if (info != null) {
                // 存在此信息，获取bot信息
                val getBot =
                    MiraiBots.get(info, conf.botConfiguration, cacheMaps, senderRunner, registeredSpecialListener).bot
                _bot = getBot
                getBot
            } else {
                // 不存在，抛出异常。一般不会出现这种情况，因为默认情况下ListenerManager会拦截未验证信息
                throw NoSuchElementException("can not found bot $id")
            }
        }
}


/**
 * 默认送信器，bot通过BotManager的default动态获取
 */
open class DefaultMiraiBotSender(
    contact: Contact? = null,
    cacheMaps: CacheMaps,
    senderRunner: SenderRunner,
    registeredSpecialListener: Boolean,
    botManager: BotManager, conf: MiraiConfiguration
) :
    MultipleMiraiBotSender(
        contact,
        DefaultThisCode(botManager),
        botManager,
        cacheMaps,
        senderRunner,
        registeredSpecialListener,
        conf
    )


/** 根据BotManager获取默认bot的账号信息 */
internal class DefaultThisCode(private val botManager: BotManager) : ThisCodeAble {
    /**
     * 获取默认bot的账号信息
     */
    override fun getThisCode(): String? = botManager.defaultBot()?.botCode

    /**
     * 允许重新定义Code以实现在存在多个机器人的时候切换处理。
     * @param code code
     */
    @Deprecated("cannot set code")
    override fun setThisCode(code: String?) {
    }

}

/** [setGroupAnonymousBan]的一次性警告日志 */
private val setGroupAnonymousBanWarning by lazy<Byte> {
    /* logger */
    QQLog.warning("mirai.api.deprecated", "setGroupAnonymousBan", "setGroupBan(...)")
    0
}



/** [setDiscussLeave]的一次性警告日志 */
private val setDiscussLeaveWarning by lazy<Byte> {
    /* logger */
    QQLog.warning("mirai.api.deprecated", "setDiscussLeave", "setGroupLeave(...)")
    0
}