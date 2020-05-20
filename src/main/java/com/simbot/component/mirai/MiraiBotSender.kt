package com.simbot.component.mirai

import com.forte.qqrobot.beans.messages.result.*
import com.forte.qqrobot.beans.messages.result.inner.BanInfo
import com.forte.qqrobot.beans.messages.types.GroupAddRequestType
import com.forte.qqrobot.sender.senderlist.BaseRootSenderList
import com.simbot.component.mirai.results.*
import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.*

/**
 * mirai bot sender
 * @author ForteScarlet <\[email]ForteScarlet@163.com>
 **/
class MiraiBotSender(val bot: Bot): BaseRootSenderList() {
    /** 获取登录信息 */
    override fun getLoginQQInfo(): LoginQQInfo = MiraiLoginInfo(bot)

    /** 获取群链接列表 不支持的API */
    @Deprecated("Unsupported API: groupLinkList")
    override fun getGroupLinkList(group: String, number: Int) = super.getGroupLinkList(group, number)

    /** 禁言列表*/
    override fun getBanList(group: String): BanList = MiraiGroupBanList(group.toLong())

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
    override fun getGroupList(): GroupList {
        TODO("Not yet implemented")
    }

    override fun getStrangerInfo(QQ: String?, cache: Boolean): StrangerInfo {
        TODO("Not yet implemented")
    }

    override fun getFileInfo(flag: String?): FileInfo {
        TODO("Not yet implemented")
    }

    override fun getFriendList(): FriendList {
        TODO("Not yet implemented")
    }

    override fun setGroupSign(group: String?): Boolean {
        TODO("Not yet implemented")
    }

    override fun setSign(): Boolean {
        TODO("Not yet implemented")
    }

    override fun sendDiscussMsg(group: String?, msg: String?): String {
        TODO("Not yet implemented")
    }

    override fun sendGroupNotice(group: String?, title: String?, text: String?, top: Boolean, toNewMember: Boolean, confirm: Boolean): Boolean {
        TODO("Not yet implemented")
    }

    override fun sendLike(QQ: String?, times: Int): Boolean {
        TODO("Not yet implemented")
    }

    override fun setGroupWholeBan(group: String?, `in`: Boolean): Boolean {
        TODO("Not yet implemented")
    }

    override fun sendPrivateMsg(QQ: String?, msg: String?): String {
        TODO("Not yet implemented")
    }

    override fun setGroupAnonymousBan(group: String?, flag: String?, time: Long): Boolean {
        TODO("Not yet implemented")
    }

    override fun setGroupMemberKick(group: String?, QQ: String?, dontBack: Boolean): Boolean {
        TODO("Not yet implemented")
    }

    override fun setDiscussLeave(group: String?): Boolean {
        TODO("Not yet implemented")
    }

    override fun setGroupAdmin(group: String?, QQ: String?, set: Boolean): Boolean {
        TODO("Not yet implemented")
    }

    override fun setGroupAnonymous(group: String?, agree: Boolean): Boolean {
        TODO("Not yet implemented")
    }

    override fun setFriendAddRequest(flag: String?, friendName: String?, agree: Boolean): Boolean {
        TODO("Not yet implemented")
    }

    override fun setGroupFileDelete(group: String?, flag: String?): Boolean {
        TODO("Not yet implemented")
    }

    override fun sendGroupMsg(group: String?, msg: String?): String {
        TODO("Not yet implemented")
    }

    override fun setMsgRecall(flag: String?): Boolean {
        TODO("Not yet implemented")
    }

    override fun setGroupCard(group: String?, QQ: String?, card: String?): Boolean {
        TODO("Not yet implemented")
    }

    override fun setGroupAddRequest(flag: String?, requestType: GroupAddRequestType?, agree: Boolean, why: String?): Boolean {
        TODO("Not yet implemented")
    }

    override fun setGroupLeave(group: String?, dissolve: Boolean): Boolean {
        TODO("Not yet implemented")
    }

    override fun setGroupExclusiveTitle(group: String?, QQ: String?, title: String?, time: Long): Boolean {
        TODO("Not yet implemented")
    }

    override fun setGroupBan(group: String?, QQ: String?, time: Long): Boolean {
        TODO("Not yet implemented")
    }

    override fun sendFlower(group: String?, QQ: String?): Boolean {
        TODO("Not yet implemented")
    }


    /////// inner class ///////


    /** 禁言列表 */
    inner class MiraiGroupBanList(groupId: Long): BanList {

        /** 禁言列表 */
        private val banList: Array<BanInfo> by lazy {
            val group = bot.getGroup(groupId)
            group.members.asSequence().filter { it.isMuted }.map { MiraiGroupBanInfo(it) }.toList().toTypedArray()
        }

        /** 获取原本的数据 originalData  */
        override fun getOriginalData(): String? = null

        /**
         * 获取列表, 极度不建议返回为null
         * non-null
         */
        override fun getList(): Array<BanInfo> = banList

    }

    /** 禁言信息 */
    class MiraiGroupBanInfo(val member: Member):  BanInfo {
        /**
         * 被禁言者的QQ
         */
        override fun getQQ(): String = member.id.toString()
        /** 获取原本的数据 originalData  */
        override fun getOriginalData(): String = member.toString()

        override fun toString(): String = member.toString()

        /**
         * 是否为管理员
         */
        override fun isManager(): Boolean = member.permission.isOperator()

        /**
         * 被禁言成员昵称
         */
        override fun getNickName(): String = member.nameCardOrNick
        /**
         * 禁言剩余时间
         */
        override fun lastTime(): Long = member.muteTimeRemaining.toLong()
    }

}



