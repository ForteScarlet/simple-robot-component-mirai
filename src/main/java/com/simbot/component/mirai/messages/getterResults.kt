/*
 * Copyright (c) 2020. ForteScarlet All rights reserved.
 * Project  component-mirai (Codes other than Mirai)
 * File     getterResults.kt (Codes other than Mirai)
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

@file:Suppress("MemberVisibilityCanBePrivate")

package com.simbot.component.mirai.messages

import com.forte.qqrobot.beans.messages.result.*
import com.forte.qqrobot.beans.messages.result.inner.BanInfo
import com.forte.qqrobot.beans.messages.result.inner.GroupMember
import com.forte.qqrobot.beans.messages.result.inner.GroupNote
import com.forte.qqrobot.beans.messages.types.PowerType
import com.forte.qqrobot.beans.messages.types.SexType
import com.forte.qqrobot.bot.LoginInfo
import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.*

/** 权限信息转化 */
@Deprecated("use 'MemberPermission.powerType'", replaceWith = ReplaceWith("powerType"))
fun MemberPermission.toPowerType(): PowerType = this.powerType


/** 权限信息转化 */
val MemberPermission.powerType: PowerType get() = when(this){
    MemberPermission.ADMINISTRATOR -> PowerType.ADMIN
    MemberPermission.MEMBER -> PowerType.MEMBER
    MemberPermission.OWNER -> PowerType.OWNER
    else -> PowerType.MEMBER
}

/** 权限信息转化 */
@Deprecated("use 'Member.powerType'", replaceWith = ReplaceWith("powerType"))
fun Member.toPowerType(): PowerType = this.powerType


/** 权限信息转化 */
val Member.powerType: PowerType get() = this.permission.powerType


/**
 * mirai bot信息
 */
open class MiraiLoginInfo(
        val bot: Bot
) : LoginInfo {
    private val selfId: Long = bot.id
    private val selfNick: String = bot.nick
    private val selfLevel: Int = -1

    /*
        接口实现
     */
    override fun getOriginalData(): String = toString()
    override fun toString(): String = bot.toString()
    override fun getQQ(): String = selfId.toString()
    override fun getName(): String = selfNick
    override fun getLevel(): Int = selfLevel
    override fun getCodeNumber(): Long = selfId
}

/** 群信息 */
open class MiraiGroupInfo(
        val baseGroup: Group
) : GroupInfo, com.forte.qqrobot.beans.messages.result.inner.Group {

    private val groupName: String = baseGroup.name
    private val entranceAnnouncement: String = baseGroup.settings.entranceAnnouncement
    private val members: ContactList<Member> = baseGroup.members

    /** 群主与管理员的map */
    private val managersIdAndNick: MutableMap<String, String> by lazy {
        baseGroup.members.asSequence()
                .filter { it.isOperator() }
                .sortedBy { -it.permission.level }
                .map { it.id.toString() to it.nameCardOrNick }.toMap().toMutableMap()
    }

    /** 管理员列表 */
    private val administratorList: Array<String> by lazy {
        baseGroup.members.asSequence()
                .filter { it.isAdministrator() }
                .map { it.id.toString() }
                .toList().toTypedArray()
    }

    /** 群类型ID  */
    @Deprecated("just -1", ReplaceWith("-1"))
    override fun getTypeId(): Int = DeprecatedAPI.groupInfoTypeId

    /** 群名称  */
    override fun getName(): String = groupName

    /** 建群时间  */
    @Deprecated("just -1L", ReplaceWith("-1L"))
    override fun getCreateTime() = DeprecatedAPI.groupInfoCreateTime

    /** 入群公告 */
    override fun getBoard(): String = entranceAnnouncement

    override fun getGroupCode(): String = baseGroup.id.toString()


    /** 群成员上限  */
    @Deprecated("just -1", ReplaceWith("-1"))
    override fun getMaxMember(): Int = DeprecatedAPI.groupInfoMaxMember

    /** 群搜索类型  */
    @Deprecated("just -1", ReplaceWith("-1"))
    override fun getSearchType(): Int = DeprecatedAPI.groupInfoSearchType

    /**
     * [getLevelNames]
     */
    private val _levelNames: MutableMap<String, String> by DeprecatedAPI.deprecated("GroupInfo.getLevelNames()"){ mutableMapOf<String, String>() }

    /** 等级信息 */
    @Deprecated("just empty", ReplaceWith("mutableMapOf()"))
    override fun getLevelNames(): MutableMap<String, String> = _levelNames

    /** 群等级  */
    @Deprecated("just -1", ReplaceWith("-1"))
    override fun getLevel(): Int = DeprecatedAPI.groupInfoLevel

    /** 获取群主和管理的QQ与昵称列表  */
    override fun getAdminNickList(): MutableMap<String, String> = managersIdAndNick


    /** 获取原本的数据 originalData  */
    override fun getOriginalData(): String = toString()


    override fun toString(): String = baseGroup.toString()

    /** 加群方式  */
    @Deprecated("just -1", ReplaceWith("-1"))
    override fun getOpenType(): Int = DeprecatedAPI.groupInfoOpenType

    /** 获取群地址、坐标信息  */
    @Deprecated("just null", ReplaceWith("null"))
    override fun getPos(): String? = DeprecatedAPI.groupInfoPos

    /** 群成员数量  */
    override fun getMemberNum(): Int = members.size

    /** 群类型  */
    @Deprecated("just null", ReplaceWith("null"))
    override fun getType(): String? = DeprecatedAPI.groupInfoType

    /** 获取群介绍-完整  */
    @Deprecated("just null", ReplaceWith("null"))
    override fun getCompleteIntro(): String? = DeprecatedAPI.groupInfoCompleteIntro

    /** 获取群介绍-简略  */
    @Deprecated("just null", ReplaceWith("null"))
    override fun getSimpleIntro(): String? = DeprecatedAPI.groupInfoSimpleIntro

    /** 群主QQ号  */
    override fun getOwnerQQ(): String = baseGroup.owner.id.toString()

    /** 群号  */
    override fun getCode(): String = baseGroup.id.toString()

    /** 获取群管理列表  */
    override fun getAdminList(): Array<String> = administratorList

    /** 获取群标签  */
    @Deprecated("empty", ReplaceWith("emptyArray()"))
    override fun getTags(): Array<String> = DeprecatedAPI.groupInfoTags


    override fun getHeadUrl(): String = baseGroup.avatarUrl

}


/**
 * 好友信息。
 */
open class MiraiFriends(val friend: Friend): StrangerInfo {
    /** QQ号  */
    override fun getQQ(): String = friend.id.toString()

    /** 获取原本的数据 originalData  */
    override fun getOriginalData(): String = friend.toString()

    /** 年龄  */
    @Deprecated("just -1", ReplaceWith("-1"))
    override fun getAge(): Int = DeprecatedAPI.friendAge

    /** 性别  */
    @Deprecated("just UNKNOWN", ReplaceWith("SexType.UNKNOWN", "com.forte.qqrobot.beans.messages.types.SexType"))
    override fun getSex(): SexType = SexType.UNKNOWN

    /** 等级  */
    @Deprecated("just -1", ReplaceWith("-1"))
    override fun getLevel(): Int = -DeprecatedAPI.friendLevel

    /**
     * 获取备注信息，例如群备注，或者好友备注。
     * @return 备注信息
     */
    override fun getRemark(): String = nickname

    /**
     * 可以获取昵称
     * @return nickname
     */
    override fun getNickname(): String = friend.nick

    override fun getCodeNumber(): Long = friend.id

}


/** 群成员信息 */
open class MiraiGroupMemberInfo(
        val member: Member
): GroupMemberInfo {
    /** 获取专属头衔  */
    override fun getExTitle(): String = member.specialTitle

    /** 成员QQ号  */
    override fun getQQ(): String = member.id.toString()

    /** 群名片  */
    override fun getCard(): String = member.nameCard

    /** 权限类型  */
    override fun getPowerType(): PowerType = member.powerType

    /** 群成员等级名称  */
    @Deprecated("just null", ReplaceWith("null"))
    override fun getLevelName(): String? = DeprecatedAPI.memberLevelName

    /** 加群时间  */
    @Deprecated("just -1L", ReplaceWith("-1L"))
    override fun getJoinTime(): Long = DeprecatedAPI.memberJoinTime

    /** 头像地址  */
    override fun getHeadImgUrl(): String = member.avatarUrl

    /** 获取性别 -1:男，1:女，0:未知   */
    @Deprecated("just unknwon", ReplaceWith("SexType.UNKNOWN", "com.forte.qqrobot.beans.messages.types.SexType"))
    override fun getSex(): SexType = DeprecatedAPI.memberSex

    /** 头衔的有效期  */
    @Deprecated("just -1L", ReplaceWith("-1L"))
    override fun getExTitleTime(): Long = DeprecatedAPI.memberExTitleTime

    /** 获取原本的数据 originalData  */
    override fun getOriginalData(): String = toString()

    override fun toString(): String = member.toString()

    /** 是否允许修改群昵称  */
    @Deprecated("just true", ReplaceWith("true"))
    override fun isAllowChangeNick(): Boolean = DeprecatedAPI.memberAllowChangeNick

    /** 最后一次发言时间  */
    @Deprecated("just -1L", ReplaceWith("-1L"))
    override fun getLastTime(): Long = DeprecatedAPI.memberLastTime

    /** 禁言剩余时间  */
    override fun getBanTime(): Long = member.muteTimeRemaining.toLong()

    /** 是否为不良用户  */
    @Deprecated("just false", ReplaceWith("false"))
    override fun isBlack(): Boolean = DeprecatedAPI.memberBlack

    /** 获取群号  */
    override fun getCode(): String = member.group.id.toString()

    /** 所在城市  */
    @Deprecated("just null", ReplaceWith("null"))
    override fun getCity(): String? = DeprecatedAPI.memberCity

    /**
     * 获取备注信息，例如群备注，或者好友备注。
     * @return 备注信息
     */
    override fun getRemark(): String = member.nameCard

    /**
     * 可以获取昵称
     * @return nickname
     */
    override fun getNickname(): String = member.nick


    override fun getRemarkOrNickname(): String = member.nameCardOrNick
    override fun getCodeNumber(): Long = member.id
}




/** 群成员列表 */
open class MiraiGroupMemberList(
        val group: Group
): GroupMemberList {

    /** 群成员列表 */
    private val groupMemberArray: Array<GroupMember> by lazy {
        group.members.asSequence().map { MiraiGroupMember(it) as GroupMember }.toList().toTypedArray()
    }

    /** 获取原本的数据 originalData  */
    override fun getOriginalData(): String = group.toString()

    /**
     * 获取列表, 极度不建议返回为null
     * non-null
     */
    override fun getList(): Array<GroupMember> = groupMemberArray


    /** 群成员信息 */
    open class MiraiGroupMember(val member: Member): GroupMember {
        /** 获取专属头衔  */
        override fun getExTitle(): String = member.specialTitle

        /** QQ号  */
        override fun getQQ(): String = member.id.toString()

        /** 群号  */
        override fun getGroup(): String = member.group.id.toString()

        /** 等级对应名称  */
        @Deprecated("just null", ReplaceWith("null"))
        override fun getLevelName(): String? = DeprecatedAPI.memberListLevelName

        /** 加群时间  */
        @Deprecated("just -1L", ReplaceWith("-1L"))
        override fun getJoinTime(): Long? = DeprecatedAPI.memberListJoinTime

        /** 获取性别  */
        @Deprecated("just unknown", ReplaceWith("SexType.UNKNOWN", "com.forte.qqrobot.beans.messages.types.SexType"))
        override fun getSex(): SexType = DeprecatedAPI.memberListSex

        /** 权限类型  */
        override fun getPower(): PowerType = member.powerType

        /** 头衔到期时间  */
        @Deprecated("just -1L", ReplaceWith("-1L"))
        override fun getExTitleTime(): Long = DeprecatedAPI.memberListExTitleTime

        /** 获取原本的数据 originalData  */
        override fun getOriginalData(): String = toString()

        /** toString */
        override fun toString(): String = member.toString()

        /** 是否允许修改群名片  */
        @Deprecated("just true", ReplaceWith("true"))
        override fun isAllowChangeNick(): Boolean = DeprecatedAPI.memberListAllowChangeNick

        /** 最后发言时间  */
        @Deprecated("just -1L", ReplaceWith("-1L"))
        override fun getLastTime(): Long = DeprecatedAPI.memberListLastTime

        /** 是否为不良用户  */
        @Deprecated("just false", ReplaceWith("false"))
        override fun isBlack(): Boolean = DeprecatedAPI.memberListBlack

        /** 所在城市  */
        @Deprecated("just null", ReplaceWith("null"))
        override fun getCity(): String? = DeprecatedAPI.memberListCity

        /** 头像  */
        override fun getHeadUrl(): String = member.avatarUrl

        /**
         * 获取备注信息，例如群备注，或者好友备注。
         * @return 备注信息
         */
        override fun getRemark(): String = member.nameCard

        /**
         * 可以获取昵称
         * @return nickname
         */
        override fun getNickname(): String = member.nick


        override fun getRemarkOrNickname(): String = member.nameCardOrNick

        override fun getCodeNumber(): Long = member.id
    }

}

/** 入群公告 */
open class MiraiGroupTopNote(val group: Group): GroupTopNote{
    /**
     * 预览文
     */
    override fun getFaceMsg(): String = group.settings.entranceAnnouncement

    /**
     * 发布者QQ
     */
    @Deprecated("just null", ReplaceWith("null"))
    override fun getQQ(): String? = DeprecatedAPI.groupNoteQQ

    /** 获取原本的数据 originalData  */
    override fun getOriginalData(): String = toString()


    override fun toString(): String = group.toString()

    /**
     * 已读人数数量
     */
    @Deprecated("just -1", ReplaceWith("-1"))
    override fun getReadNum(): Int = DeprecatedAPI.groupNoteReadNum

    /**
     * 是否提醒群员修改群名片
     */
    @Deprecated("just false", ReplaceWith("false"))
    override fun isShowEditCard(): Boolean = DeprecatedAPI.groupNoteShowEditCard

    /**
     * 公告类型ID
     */
    @Deprecated("just null", ReplaceWith("null"))
    override fun getTypeId(): String? = DeprecatedAPI.groupNoteTypeId

    /**
     * ID
     */
    @Deprecated("just null", ReplaceWith("null"))
    override fun getId(): String? = DeprecatedAPI.groupNoteId

    /**
     * 发布时间
     */
    @Deprecated("just -1L", ReplaceWith("-1L"))
    override fun getTime(): Long = DeprecatedAPI.groupNoteTime

    /**
     * 完整正文
     */
    override fun getMsg(): String = group.settings.entranceAnnouncement

    /**
     * 标题
     */
    @Deprecated("just null", ReplaceWith("null"))
    override fun getTitle(): String? = DeprecatedAPI.groupNoteTitle


}


/**
 * 群公告列表
 */
open class MiraiGroupNoteList(val group: Group): GroupNoteList {

    private val groupNoteArray: Array<GroupNote> = arrayOf(MiraiGroupTopNote(group))

    /** 获取原本的数据 originalData  */
    override fun getOriginalData(): String = toString()


    override fun toString(): String = group.toString()

    /**
     * 获取列表, 极度不建议返回为null
     * non-null
     */
    override fun getList(): Array<GroupNote> = groupNoteArray
}

/**
 * 群列表
 */
open class MiraiGroupList(val groups: ContactList<Group>): GroupList {
    override fun getOriginalData(): String = toString()
    override fun toString(): String = groups.toString()

    private val groupList: Array<com.forte.qqrobot.beans.messages.result.inner.Group> by lazy {
        groups.asSequence().map { MiraiGroupInfo(it) as com.forte.qqrobot.beans.messages.result.inner.Group }.toList().toTypedArray()
    }

    /**
     * 获取群列表
     */
    override fun getList(): Array<com.forte.qqrobot.beans.messages.result.inner.Group> = groupList

}


/** 获取好友列表 */
open class MiraiFriendList(val friends: ContactList<Friend>): FriendList {
    override fun getOriginalData(): String = toString()
    override fun toString(): String = friends.toString()

    /** 好友数组 */
    val friendList: Array<com.forte.qqrobot.beans.messages.result.inner.Friend> by lazy {
        friends.asSequence().map { MiraiFriend(it) as com.forte.qqrobot.beans.messages.result.inner.Friend }.toList().toTypedArray()
    }
    /** 好友分组, 无分组信息 */
    private val friendListMap: MutableMap<String, Array<com.forte.qqrobot.beans.messages.result.inner.Friend>> by lazy { mutableMapOf("" to friendList) }

    /** 无分组信息 */
    @Deprecated("just getFriendList()", ReplaceWith("friendList"))
    override fun getFirendList(group: String?): Array<com.forte.qqrobot.beans.messages.result.inner.Friend> = friendList

    /** 无分组信息 */
    @Deprecated("just getFriendList()")
    override fun getFriendList(): MutableMap<String, Array<com.forte.qqrobot.beans.messages.result.inner.Friend>> = friendListMap

    /** 好友信息 */
    open inner class MiraiFriend(val friend: Friend): com.forte.qqrobot.beans.messages.result.inner.Friend {
        override fun getQQ(): String = friend.id.toString()
        override fun getOriginalData(): String = toString()
        override fun toString(): String = friend.toString()
        override fun getName(): String = friend.nick

        override fun getCodeNumber(): Long = friend.id

        /**
         * 获取备注信息，例如群备注，或者好友备注。
         * @return 备注信息
         */
        override fun getRemark(): String = nickname

        override fun getRemarkOrNickname(): String = nickname

        /**
         * 可以获取昵称
         * @return nickname
         */
        override fun getNickname(): String = friend.nick
    }

}


/** 禁言列表 */
open class MiraiGroupBanList(group: Group): BanList {

    /** 禁言列表 */
    private val banList: Array<BanInfo> by lazy {
        group.members.asSequence().filter { it.isMuted }.map { MiraiGroupBanInfo(it) as BanInfo }.toList().toTypedArray()
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
open class MiraiGroupBanInfo(val member: Member): BanInfo {
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

    override fun getCodeNumber(): Long = member.id
}






