package com.simbot.component.mirai.messages

import com.forte.qqrobot.beans.messages.msgget.EventGet
import com.forte.qqrobot.beans.messages.msgget.MsgGet
import com.forte.qqrobot.beans.messages.msgget.PrivateMsg
import com.forte.qqrobot.beans.messages.types.PrivateMsgType
import com.simbot.component.mirai.MiraiCodeFormatUtils
import com.simbot.component.mirai.RecallCache
import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.User
import net.mamoe.mirai.message.FriendMessageEvent
import net.mamoe.mirai.message.MessageEvent
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.source

/**
 * mirai 的msgGet的父类，可获取contact对象
 * 且实现[MsgGet]接口
 */
abstract class MiraiMsgGet(val event: MessageEvent): MsgGet {
    /**
     * 获取contact
     * > 联系对象, 即可以与 [Bot] 互动的对象. 包含 [用户][User], 和 [群][Group].
     * @see [Contact]
     */
    val contact: Contact = event.sender
    val message: MessageChain = event.message
    // bot id
    private var botId: String = contact.bot.id.toString()

    /** 获取原本的数据 originalData  */
    override fun getOriginalData(): String = toString()
    override fun toString(): String = contact.toString()
    /**
     * 此消息获取的时候，代表的是哪个账号获取到的消息。
     * @return 接收到此消息的账号。
     */
    override fun getThisCode(): String = botId

    /** bot id */
    override fun setThisCode(code: String) {
        botId = code
    }

}

/**
 *  mirai 的EventGet的父类，可获取contact对象
 *  且实现[EventGet]接口
 */
abstract class MiraiEventGet(event: MessageEvent): MiraiMsgGet(event), EventGet



/**
 * Mirai的好友消息事件
 * @param event 监听到的事件
 */
open class MiraiFriendMsg(event: FriendMessageEvent): MiraiMsgGet(event), PrivateMsg {

    /** 消息id */
    private val msgId: String by lazy { RecallCache.cache(message.source) }

    /** 消息正文，目前会将mirai码替换为CQ码 */
    private var eventMsg: String? = MiraiCodeFormatUtils.mi2cq(message)

    /** 获取发送人的QQ号  */
    override fun getQQ(): String = contact.id.toString()

    /** 获取ID, 可用于撤回  */
    override fun getId(): String = msgId


    /** 获取私聊消息类型，固定为好友 */
    override fun getType(): PrivateMsgType = PrivateMsgType.FROM_FRIEND

    /**
     * 重新设置消息
     * @param newMsg msg
     * @since 1.7.x
     */
    override fun setMsg(newMsg: String?) {
        eventMsg = newMsg
    }

    /** 获取到的时间, 代表某一时间的秒值。*/
    override fun getTime(): Long = event.time.toLong()

    /** 获取消息的字体  */
    override fun getFont(): String? = null

    /**
     * 一般来讲，监听到的消息大部分都会有个“消息内容”。定义此方法获取消息内容。
     * 如果不存在，则为null。（旧版本推荐为空字符串，现在不了。我变卦了）
     */
    override fun getMsg(): String? = eventMsg

}

