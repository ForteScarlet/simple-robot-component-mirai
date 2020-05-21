/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package com

import kotlinx.coroutines.CompletableJob
import kotlinx.coroutines.launch
import net.mamoe.mirai.alsoLogin
import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.Friend
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.contact.isOperator
import net.mamoe.mirai.event.*
import net.mamoe.mirai.join
import net.mamoe.mirai.message.*
import net.mamoe.mirai.message.data.*
import java.io.File


suspend fun main() {
    val bot = Bot( // JVM 下也可以不写 `QQAndroid.` 引用顶层函数
            2370606773,
            "LiChengYang9983."
    ) {
        // 覆盖默认的配置
        fileBasedDeviceInfo("device.json") // 使用 "device.json" 保存设备信息
        // networkLoggerSupplier = { SilentLogger } // 禁用网络层输出
    }.alsoLogin()

    bot.messageDSL()
    directlySubscribe(bot)

    bot.join()//等到直到断开连接
}

/**
 * 使用 dsl 监听消息事件
 *
 * @see subscribeFriendMessages
 * @see subscribeMessages
 * @see subscribeGroupMessages
 * @see subscribeTempMessages
 *
 * @see MessageSubscribersBuilder
 */
fun Bot.messageDSL() {


    // 监听这个 bot 的来自所有群和好友的消息
    this.subscribeMessages {
        // 当接收到消息 == "你好" 时就回复 "你好!"
        "你好" reply "你好!"

        // 当消息 == "查看 subject" 时, 执行 lambda
        case("查看 subject") {
            when (subject) {
                is Friend -> {
                    reply("消息主体为 Friend，你在发私聊消息")
                }
                is Group -> {
                    reply("消息主体为 Group，你在群里发消息")
                }
                is Member -> {
                    reply("消息主体为 Member，你在发临时消息")
                }
            }

            // 在回复的时候, 一般使用 subject 来作为回复对象.
            // 因为当群消息时, subject 为这个群.
            // 当好友消息时, subject 为这个好友.
            // 所有在 MessagePacket(也就是此时的 this 指代的对象) 中实现的扩展方法, 如刚刚的 "reply", 都是以 subject 作为目标
        }


        // 当消息里面包含这个类型的消息时
        has<Image> {
            // this: MessagePacket
            // message: MessageChain
            // sender: QQ
            // it: String (MessageChain.toString)


            // message[Image].download() // 还未支持 download
            if (this is GroupMessageEvent) {
                //如果是群消息
                // group: Group
                this.group.sendMessage("你在一个群里")
                // 等同于 reply("你在一个群里")
            }

            reply("图片, ID= ${message[Image]}")//获取第一个 Image 类型的消息
            reply(message)
        }

        Regex("hello.*world") matchingReply {
            "Hello!"
        }

        "123" containsReply "你的消息里面包含 123"


        // 当收到 "我的qq" 就执行 lambda 并回复 lambda 的返回值 String
        "我的qq" reply { sender.id }

        "at all" reply AtAll // at 全体成员

        // 如果是这个 QQ 号发送的消息(可以是好友消息也可以是群消息)
        sentBy(123456789) {
        }


        // 当消息前缀为 "我是" 时
        startsWith("我是", removePrefix = true) {
            // it: 删除了消息前缀 "我是" 后的消息
            // 如一条消息为 "我是张三", 则此时的 it 为 "张三".

            reply("你是$it")
        }


        // listener 管理

        var repeaterListener: CompletableJob? = null
        contains("开启复读") {
            repeaterListener?.complete()
            bot.subscribeGroupMessages {
                repeaterListener = contains("复读") {
                    reply(message)
                }
            }

        }

        contains("关闭复读") {
            if (repeaterListener?.complete() == null) {
                reply("没有开启复读")
            } else {
                reply("成功关闭复读")
            }
        }



        case("上传好友图片") {
            val filename = it.substringAfter("上传好友图片")
            File("C:\\Users\\Him18\\Desktop\\$filename").sendAsImageTo(subject)
        }

        case("上传群图片") {
            val filename = it.substringAfter("上传好友图片")
            File("C:\\Users\\Him18\\Desktop\\$filename").sendAsImageTo(subject)
        }
    }

    subscribeMessages {
        case("你好") {
            // this: MessagePacket
            // message: MessageChain
            // sender: QQ
            // it: String (来自 MessageChain.toString)
            // group: Group (如果是群消息)
            reply("你好")
        }
    }

    subscribeFriendMessages {
        contains("A") {
            // this: FriendMessage
            // message: MessageChain
            // sender: QQ
            // it: String (来自 MessageChain.toString)
            reply("B")
        }
    }

    launch {
        // channel 风格
        for (message in this@messageDSL.incoming<FriendMessageEvent>()) {
            println(message)
        }
        // 这个 for 循环不会结束.
    }

    subscribeGroupMessages {
        // this: FriendMessage
        // message: MessageChain
        // sender: QQ
        // it: String (来自 MessageChain.toString)
        // group: Group

        case("recall") {
            reply("😎").recallIn(3000) // 3 秒后自动撤回这条消息
        }

        case("禁言") {
            // 挂起当前协程, 等待下一条满足条件的消息.
            // 发送 "禁言" 后需要再发送一条消息 at 一个人.
            val value: At? = nextMessage { message.any(At) }[At]
            value?.asMember()?.mute(10)
        }

        startsWith("群名=") {
            if (!sender.isOperator()) {
                sender.mute(5)
                return@startsWith
            }
            group.name = it
        }
    }
}

/**
 * 监听单个事件
 */
@Suppress("UNUSED_VARIABLE")
suspend fun directlySubscribe(bot: Bot) {
    // 在当前协程作用域 (CoroutineScope) 下创建一个子 Job, 监听一个事件.
    //
    // 手动处理消息
    //
    // subscribeAlways 函数返回 Listener, Listener 是一个 CompletableJob.
    //
    // 例如:
    // ```kotlin
    // runBlocking {// this: CoroutineScope
    //     subscribeAlways<FriendMessage> {
    //     }
    // }
    // ```
    // 则这个 `runBlocking` 永远不会结束, 因为 `subscribeAlways` 在 `runBlocking` 的 `CoroutineScope` 下创建了一个 Job.
    // 正确的用法为:
    // 在 Bot 的 CoroutineScope 下创建一个监听事件的 Job, 则这个子 Job 会在 Bot 离线后自动完成 (complete).
    bot.subscribeAlways<FriendMessageEvent> {
        // this: FriendMessageEvent
        // event: FriendMessageEvent

        // 获取第一个纯文本消息, 获取不到会抛出 NoSuchElementException
        // val firstText = message.first<PlainText>()

        val firstText = message.firstOrNull(PlainText)

        // 获取第一个图片
        val firstImage = message.firstOrNull(Image)

        when {
            message.contentToString() == "你好" -> reply("你好!")

            "复读" in message.contentToString() -> sender.sendMessage(message)

            "发群消息" in message.contentToString() -> {
                bot.getGroup(580266363).sendMessage(message.toString().substringAfter("发群消息"))
            }
        }
    }
}
