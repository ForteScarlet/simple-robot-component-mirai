package com.simbot.component.mirai

import com.forte.qqrobot.beans.messages.ThisCodeAble
import com.forte.qqrobot.beans.messages.result.*
import com.forte.qqrobot.beans.messages.types.GroupAddRequestType
import com.forte.qqrobot.bot.BotInfo
import com.forte.qqrobot.bot.BotManager
import com.forte.qqrobot.sender.senderlist.BaseRootSenderList
import com.simbot.component.mirai.messages.*
import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.mute
import net.mamoe.mirai.event.events.BotInvitedJoinGroupRequestEvent
import net.mamoe.mirai.event.events.MemberJoinRequestEvent


/**
 * mirai bot sender
 * @param bot 虽然可以为null，但是此null仅为子类重写而用，构建此类不可使用null值。
 * @author ForteScarlet <\[email]ForteScarlet@163.com>
 **/
open class MiraiBotSender(bot: Bot?, val contact: Contact? = null): BaseRootSenderList() {

    /** 幕后真实字段 */
    private val _bot: Bot? = bot
    /** 获取到bot对象 */
    open val bot: Bot
    get() = _bot!!



    /** 获取登录信息 */
    override fun getLoginQQInfo(): LoginQQInfo = MiraiLoginInfo(bot)

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
    override fun getGroupMemberInfo(group: String, QQ: String, cache: Boolean): GroupMemberInfo = MiraiGroupMemberInfo(bot.getGroup(group.toLong())[QQ.toLong()])

    /** 群成员列表 */
    override fun getGroupMemberList(group: String): GroupMemberList = MiraiGroupMemberList(bot.getGroup(group.toLong()))

    /** 群进群公告 */
    override fun getGroupTopNote(group: String): GroupTopNote = MiraiGroupTopNote(bot.getGroup(group.toLong()))

    /** 权限信息，不支持 */
    @Deprecated("Unsupported API: authInfo")
    override fun getAuthInfo(): AuthInfo = super.getAuthInfo()

    /**
     * 公告列表，就返回一个TopNote
     */
    override fun getGroupNoteList(group: String, number: Int): GroupNoteList = MiraiGroupNoteList(bot.getGroup(group.toLong()))

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
    override fun sendDiscussMsg(group: String, msg: String): String = sendGroupMsg(group, msg)

    /** 发送群消息 */
    override fun sendGroupMsg(group: String, msg: String): String {
        val g = bot.getGroup(group.toLong())
        // 阻塞发送
        val result = runBlocking {
            g.sendMsg(msg)
        }
        // 缓存消息id并返回
        return RecallCache.cache(result)
    }


    /** 发送私信消息 */
    override fun sendPrivateMsg(QQ: String, msg: String): String {
        // 获取QQ号
        val code = QQ.toLong()
        // 没有这个人则可能抛出异常
        // 默认认为是给好友发消息
        val to: Contact = try {
            bot.getFriend(code)
        }catch (fe: NoSuchElementException){
            // 不是好友
            // 如果当前contact是群消息，则尝试获取群员
            if(contact != null && contact is Group){
                contact.getOrNull(code) ?: run{
                    // 可能不是这个群里的人，开始缓存查询，查询不到缓存则会抛出异常
                    ContactCache[code, bot] ?: throw fe
                }
            }else{
                // 不是好友，开始扫描全群缓存，查询不到缓存则会抛出异常
               ContactCache[code, bot] ?: throw fe
            }
        }
        val result = runBlocking {
            to.sendMsg(msg)
        }
        // 缓存消息id并返回
        return RecallCache.cache(result)
    }

    /** 发布群公告 */
    @Deprecated("Unsupported API: sendGroupNotice")
    override fun sendGroupNotice(group: String?, title: String?, text: String?, top: Boolean, toNewMember: Boolean, confirm: Boolean): Boolean = super.sendGroupNotice(group, title, text, top, toNewMember, confirm)

    /** 点赞 */
    @Deprecated("Unsupported API: sendLike")
    override fun sendLike(QQ: String?, times: Int): Boolean = super.sendLike(QQ, times)


    /** 设置全群禁言 */
    override fun setGroupWholeBan(group: String, `in`: Boolean): Boolean {
        val groupId = group.toLong()
        val settings = bot.getGroup(groupId).settings
        val muteAll = settings.isMuteAll
        if(muteAll != `in`){
            settings.isMuteAll = `in`
        }
        return true
    }

    /** 设置匿名聊天ban */
    @Deprecated("Unsupported API: setGroupAnonymousBan")
    override fun setGroupAnonymousBan(group: String?, flag: String?, time: Long): Boolean = super.setGroupAnonymousBan(group, flag, time)

    /** 踢出群员 */
    override fun setGroupMemberKick(group: String, QQ: String, dontBack: Boolean): Boolean {
        runBlocking {
            bot.getGroup(group.toLong())[QQ.toLong()].kick()
        }
        return true
    }

    /** 退出讨论组，直接使用退出群 */
    @Deprecated("just see group leave", ReplaceWith("setGroupLeave(group, false)"))
    override fun setDiscussLeave(group: String): Boolean = setGroupLeave(group, false)

    /** 退群 */
    override fun setGroupLeave(group: String, dissolve: Boolean): Boolean {
        val g = bot.getGroup(group.toLong())
        // 如果为解散, 似乎不支持解散
//        if(dissolve){
//            g.quit()
//        }else{
            return runBlocking { g.quit() }
//        }
    }


    /** 设置/取消管理员 */
    @Deprecated("Unsupported API: setGroupAnonymousBan")
    override fun setGroupAdmin(group: String, QQ: String, set: Boolean): Boolean = super.setGroupAdmin(group, QQ, set)


    /** 设置群匿名聊天 */
    override fun setGroupAnonymous(group: String, agree: Boolean): Boolean {
        val settings = bot.getGroup(group.toLong()).settings
        if(settings.isAllowMemberInvite != agree){
            settings.isAllowMemberInvite = agree
        }
        return true
    }

    /** 处理好友申请 */
    override fun setFriendAddRequest(flag: String, friendName: String, agree: Boolean): Boolean {
        val botId = bot.id
        val request = RequestCache.getFriendRequest(botId, flag)
        return if(request!=null){
            if(agree){
                runBlocking { bot.acceptNewFriendRequest(request) }
            }else{
                runBlocking { bot.rejectNewFriendRequest(request, false) }
            }
            RequestCache.removeFriendRequest(botId, flag)
            true
        }else{
            false
        }
    }

    /** 处理加群申请 */
    override fun setGroupAddRequest(flag: String, requestType: GroupAddRequestType, agree: Boolean, why: String): Boolean {
        val botId = bot.id
        val request = RequestCache.getJoinRequest(botId, flag)
        return if(request!=null){
            when(request) {
                // 是加群申请
                is MemberJoinRequestEvent -> {
                    if(agree){
                        // 同意
                        runBlocking { bot.acceptMemberJoinRequest(request) }
                    }else{
                        // 不同意
                        runBlocking { bot.rejectMemberJoinRequest(request) }
                    }
                    RequestCache.removeJoinRequest(botId, flag)
                    true
                }
                // 是别人的邀请
                is BotInvitedJoinGroupRequestEvent -> {
                    if(agree){
                        // 同意
                        runBlocking { bot.acceptInvitedJoinGroupRequest(request) }
                    }else{
                        // 不同意, 即忽略
                        runBlocking { bot.ignoreInvitedJoinGroupRequest(request) }
                    }
                    RequestCache.removeJoinRequest(botId, flag)
                    true
                }
                else -> { throw IllegalArgumentException("unknown join request type: $request") }
            }
        }else{
            false
        }
    }

    /** 删除群文件 */
    @Deprecated("Unsupported API: setGroupFileDelete")
    override fun setGroupFileDelete(group: String?, flag: String?): Boolean = super.setGroupFileDelete(group, flag)


    /** 撤回消息 */
    override fun setMsgRecall(flag: String): Boolean {
        val botId = bot.id
        val source = RecallCache.get(flag, botId)
        return if(source != null){
            // 有
            runBlocking {
                bot.recall(source)
            }
            RecallCache.remove(flag, botId)
            true
        }else{
            false
        }
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
        runBlocking {
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
open class MultipleMiraiBotSender(contact: Contact? = null,
                             private val thisCodeAble: ThisCodeAble,
                             private val botManager: BotManager,
                             private val conf: MiraiConfiguration): MiraiBotSender(null, contact){
    /** 通过thisCode动态获取 */
    override val bot: Bot
        get() {
            val id = thisCodeAble.thisCode
            val info: BotInfo? = botManager.getBot(id)
            return if(info != null){
                // 存在此信息，获取bot信息
                MiraiBots.get(info, conf.botConfiguration).bot
            }else{
                // 不存在，抛出异常。一般不会出现这种情况，因为默认情况下ListenerManager会拦截未验证信息
                throw NoSuchElementException("can not found bot $id")
            }
        }
}



/**
 * 默认送信器，bot通过BotManager的default动态获取
 */
open class DefaultMiraiBotSender(contact: Contact? = null, botManager: BotManager, conf: MiraiConfiguration): MultipleMiraiBotSender(contact, DefaultThisCode(botManager), botManager, conf)



/** 根据BotManager获取默认bot的账号信息 */
internal class DefaultThisCode(private val botManager: BotManager): ThisCodeAble {
    /**
     * 获取默认bot的账号信息
     */
    override fun getThisCode(): String? = botManager.defaultBot()?.botCode

    /**
     * 允许重新定义Code以实现在存在多个机器人的时候切换处理。
     * @param code code
     */
    @Deprecated("cannot set code")
    override fun setThisCode(code: String?) { }

}

