package com.simbot.component.mirai

import com.forte.qqrobot.MsgProcessor
import com.simbot.component.mirai.messages.MiraiFriendMsg
import net.mamoe.mirai.event.subscribeFriendMessages

/**
 * 为bot注册对应监听的工具类，为Java用
 */
object MiraiBotListenRegister {
    /** 注册监听 */
    @JvmStatic
    fun register(info: MiraiBotInfo, msgProcessor: MsgProcessor){
        info.register(msgProcessor)
    }
}


/** 注册监听 */
fun MiraiBotInfo.register(msgProcessor: MsgProcessor){
    val bot = this.bot

    // 好友消息
    bot.subscribeFriendMessages {
        this.always {
            val result = msgProcessor.onMsgSelected(MiraiFriendMsg(this))
            if(result is Map<*, *>){
                // 尝试获取reply
                val reply = result["reply"]?.toString()
                if(reply != null){
                    this.reply(reply)
                }
            }
        }



    }


}