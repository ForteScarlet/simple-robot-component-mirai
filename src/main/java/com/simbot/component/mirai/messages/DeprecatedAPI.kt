/*
 * Copyright (c) 2020. ForteScarlet All rights reserved.
 * Project  component-mirai (Codes other than Mirai)
 * File     DeprecatedAPI.kt (Codes other than Mirai)
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

package com.simbot.component.mirai.messages

import com.forte.qqrobot.beans.messages.types.SexType
import com.forte.qqrobot.log.QQLogLang

/**
 * 统一存放不支持的API的信息，使用lazy来使他们被获取的时候提示警告信息。
 */
object DeprecatedAPI {

    /** logger */
    private val logger: QQLogLang = QQLogLang("mirai.api")

    /** print log */
    private fun <T: Any> warning(api: String, def: T? = null): T? {
        logger.warning("deprecated", api, def.toString())
        return def
    }

    private infix fun String.inv(method: String) = "$this.$method()"

    fun deprecated(api: String) = lazy{ warning(api, null) }

    fun <T: Any> deprecated(api: String, def: T): Lazy<T> = lazy{ warning(api, def)!! }

    fun <T: Any> deprecated(api: String, def: () -> T): Lazy<T> = lazy{ warning(api, def())!! }

    //**************** MiraiGroupInfo ****************//

    /**
     * [MiraiMemberJoinEvent.getOperatorQQ]
     */
    val memberJoinOperatorQQ: String? by deprecated("GroupMemberIncrease" inv "getOperatorQQ")


    /**
     * [MiraiGroupInfo.getTypeId]
     */
    val groupInfoTypeId: Int by deprecated("GroupInfo" inv "getTypeId", -1)

    /**
     * [MiraiGroupInfo.getCreateTime]
     */
    val groupInfoCreateTime: Long by deprecated("GroupInfo" inv "getCreateTime", -1L)

    /**
     * [MiraiGroupInfo.getMaxMember]
     */
    val groupInfoMaxMember: Int by deprecated("GroupInfo" inv "getMaxMember", -1)

    /**
     * [MiraiGroupInfo.getSearchType]
     */
    val groupInfoSearchType: Int by deprecated("GroupInfo" inv "getSearchType", -1)

    /**
     * [MiraiGroupInfo.getLevel]
     */
    val groupInfoLevel: Int by deprecated("GroupInfo" inv "getLevel", -1)

    /**
     * [MiraiGroupInfo.getOpenType]
     */
    val groupInfoOpenType: Int by deprecated("GroupInfo" inv "getOpenType", -1)

    /**
     * [MiraiGroupInfo.getPos]
     */
    val groupInfoPos: String? by deprecated("GroupInfo" inv "getPos")

    /**
     * [MiraiGroupInfo.getType]
     */
    val groupInfoType: String? by deprecated("GroupInfo" inv "getType")

    /**
     * [MiraiGroupInfo.getCompleteIntro]
     */
    val groupInfoCompleteIntro: String? by deprecated("GroupInfo" inv "getCompleteIntro")

    /**
     * [MiraiGroupInfo.getSimpleIntro]
     */
    val groupInfoSimpleIntro: String? by deprecated("GroupInfo" inv "getSimpleIntro")

    /**
     * [MiraiGroupInfo.getTags]
     */
    val groupInfoTags: Array<String> by deprecated("GroupInfo" inv "getTags", emptyArray())

    
    //**************** MiraiFriends ****************//

    /**
     * [MiraiFriends.getAge]
     */
    val friendAge: Int by deprecated("StrangerInfo" inv "getAge", -1)

    /**
     * [MiraiFriends.getSex]
     */
    val friendSex: SexType by deprecated("StrangerInfo" inv "getSex", SexType.UNKNOWN)
    /**
     * [MiraiFriends.getLevel]
     */
    val friendLevel: Int by deprecated("StrangerInfo" inv "getLevel", -1)

    
    //**************** MiraiGroupMemberInfo ****************//


    /**
     * [MiraiGroupMemberInfo.getLevelName]
     */
    val memberLevelName: String? by deprecated("StrangerInfo" inv "getLevelName")

    /**
     * [MiraiGroupMemberInfo.getJoinTime]
     */
    val memberJoinTime: Long by deprecated("StrangerInfo" inv "getJoinTime", -1)

    /**
     * [MiraiGroupMemberInfo.getSex]
     */
    val memberSex: SexType by deprecated("StrangerInfo" inv "getSex", SexType.UNKNOWN)

    /**
     * [MiraiGroupMemberInfo.getExTitleTime]
     */
    val memberExTitleTime: Long by deprecated("StrangerInfo" inv "getExTitleTime", -1)

    /**
     * [MiraiGroupMemberInfo.isAllowChangeNick]
     */
    val memberAllowChangeNick: Boolean by deprecated("StrangerInfo" inv "isAllowChangeNick", true)

    /**
     * [MiraiGroupMemberInfo.getLastTime]
     */
    val memberLastTime: Long by deprecated("StrangerInfo" inv "getLastTime", -1)

    /**
     * [MiraiGroupMemberInfo.isBlack]
     */
    val memberBlack: Boolean by deprecated("StrangerInfo" inv "isBlack", false)

    /**
     * [MiraiGroupMemberInfo.getCity]
     */
    val memberCity: String? by deprecated("StrangerInfo" inv "getCity")


    //**************** memberList ****************//

    /**
     * [MiraiGroupMemberList.MiraiGroupMember.getLevelName]
     */
    val memberListLevelName: String? by deprecated("GroupMember" inv "getLevelName")
    /**
     * [MiraiGroupMemberList.MiraiGroupMember.getJoinTime]
     */
    val memberListJoinTime: Long by deprecated("GroupMember" inv "getJoinTime", -1)
    /**
     * [MiraiGroupMemberList.MiraiGroupMember.getSex]
     */
    val memberListSex: SexType by deprecated("GroupMember" inv "getSex", SexType.UNKNOWN)
    /**
     * [MiraiGroupMemberList.MiraiGroupMember.getExTitleTime]
     */
    val memberListExTitleTime: Long by deprecated("GroupMember" inv "getExTitleTime", -1)
    /**
     * [MiraiGroupMemberList.MiraiGroupMember.isAllowChangeNick]
     */
    val memberListAllowChangeNick: Boolean by deprecated("GroupMember" inv "isAllowChangeNick", true)
    /**
     * [MiraiGroupMemberList.MiraiGroupMember.getLastTime]
     */
    val memberListLastTime: Long by deprecated("GroupMember" inv "getLastTime", -1L)
    /**
     * [MiraiGroupMemberList.MiraiGroupMember.isBlack]
     */
    val memberListBlack: Boolean by deprecated("GroupMember" inv "isBlack", false)
    /**
     * [MiraiGroupMemberList.MiraiGroupMember.getCity]
     */
    val memberListCity: String? by deprecated("GroupMember" inv "getCity")


    //**************** group note ****************//

    /**
     * [MiraiGroupTopNote.getQQ]
     */
    val groupNoteQQ: String? by deprecated("GroupNote" inv "getQQ")

    /**
     * [MiraiGroupTopNote.getReadNum]
     */
    val groupNoteReadNum: Int by deprecated("GroupNote" inv "getReadNum", -1)

    /**
     * [MiraiGroupTopNote.isShowEditCard]
     */
    val groupNoteShowEditCard: Boolean by deprecated("GroupNote" inv "isShowEditCard", false)

    /**
     * [MiraiGroupTopNote.getTypeId]
     */
    val groupNoteTypeId: String? by deprecated("GroupNote" inv "getTypeId")

    /**
     * [MiraiGroupTopNote.getId]
     */
    val groupNoteId: String? by deprecated("GroupNote" inv "getId")

    /**
     * [MiraiGroupTopNote.getTime]
     */
    val groupNoteTime: Long by deprecated("GroupNote" inv "getTime", -1)

    /**
     * [MiraiGroupTopNote.getTime]
     */
    val groupNoteTitle: String? by deprecated("GroupNote" inv "getTime")

}


