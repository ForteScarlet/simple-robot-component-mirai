/*
 *
 * Copyright (c) 2020. ForteScarlet All rights reserved.
 * Project  component-mirai
 * File     MsgParseUtils.kt
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

@file:Suppress("unused")
@file:JvmName("MsgParseUtils")

package com.simbot.component.mirai.utils

import cn.hutool.core.io.FileUtil
import com.forte.qqrobot.log.QQLog
import com.simbot.component.mirai.CQCodeParamNullPointerException
import com.simbot.component.mirai.CQCodeParseHandlerRegisterException
import com.simbot.component.mirai.CacheMaps
import com.simbot.component.mirai.collections.ImageCache
import com.simbot.component.mirai.collections.toCacheKey
import com.simbot.component.mirai.collections.toImgVoiceCacheKey
import com.simplerobot.modules.utils.*
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.utils.io.jvm.javaio.*
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.contact.*
import net.mamoe.mirai.message.MessageReceipt
import net.mamoe.mirai.message.action.Nudge
import net.mamoe.mirai.message.action.Nudge.Companion.sendNudge
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.message.data.AtAll
import net.mamoe.mirai.message.uploadAsGroupVoice
import net.mamoe.mirai.utils.toExternalImage
import java.io.*
import java.net.URL
import java.util.function.BiFunction
import kotlin.collections.set


/**
 * 发送消息之前，会将Message通过此处进行处理。
 * 此处可解析部分CQ码并转化为Message
 * 然后发送此消息
 */
suspend fun <C : Contact> C.sendMsg(msg: String, cacheMaps: CacheMaps): MessageReceipt<Contact>? {
    if (msg.isBlank()) {
        throw IllegalArgumentException("msg is empty.")
    }
    val message = msg.toWholeMessage(this, cacheMaps)
    return if (message !is EmptyMessageChain) {
        this.sendMessage(message)
    } else {
        // QQLog.debug("mirai.bot.sender.nothing")
        null
    }
}


/**
 * 字符串解析为 [Message]。
 * 一般解析其中的CQ码
 */
fun String.toWholeMessage(contact: Contact, cacheMaps: CacheMaps): Message {
    // 切割，解析CQ码并拼接最终结果
    return KQCodeUtils.split(this) {
        if (this.trim().startsWith("[CQ:")) {
            // 如果是CQ码，转化为KQCode并进行处理
            KQCode.of(this).toMessageAsync(contact, cacheMaps)
        } else {
            if (this.isBlank()) {
                EmptyMessageChain
            } else {
                PlainText(CQDecoder.decodeText(this))
            }.async()
        }
    }.asSequence().map {
        runBlocking {
            it.await()
        }.also { _ ->
            it.invokeOnCompletion { e ->
                e?.also { ex -> throw ex }
            }
        }
    }.reduce { acc, msg ->
        when {
            acc is EmptyMessageChain && msg is EmptyMessageChain -> EmptyMessageChain
            msg is EmptyMessageChain -> acc
            acc is EmptyMessageChain -> msg
            else -> acc + msg
        }
    }
}

/**
 * KQCode转化为Message对象
 */
fun KQCode.toMessageAsync(contact: Contact, cacheMaps: CacheMaps): Deferred<Message> {
    // 判断类型，有些东西有可能并不存在与CQ码规范中，例如XML
    @Suppress("DuplicatedCode")
    return when (this.type) {
        //region CQ码解析为Message
        //region at
        "at" -> {
            val id = this["qq"] ?: this["at"] ?: throw CQCodeParamNullPointerException("at", "qq", "at")
            if (id == "all") {
                AtAll.async()
            } else {
                when (contact) {
                    is Member -> {
                        At(contact).async()
                    }
                    is Friend -> {
                        PlainText("@${contact.nick} ").async()
                    }
                    is Group -> {
                        At(contact[id.toLong()]).async()
                    }
                    else -> PlainText("@$id").async()
                }
            }
        }
        //endregion

        //region face
        "face" -> Face((this["id"] ?: this["face"])!!.toInt()).async()
        //endregion


        //region image
        "image" -> contact.async {
            // image 类型的CQ码，参数一般是file, destruct
            val fileValue: String =
                this@toMessageAsync["file"] ?: this@toMessageAsync["image"]
                ?: throw CQCodeParamNullPointerException(
                    "image",
                    "file",
                    "image"
                )

            val fileCache: String = fileValue.toImgVoiceCacheKey(contact)


            val imageCache: ImageCache = cacheMaps.imageCache

            // 先查缓存
            val image: Image? = imageCache[fileCache]

            if (image == null) {
                // 是否缓存此上传的图片
                val cache: Boolean = this@toMessageAsync["cache"] != "false"
                return@async if (fileValue.startsWith("http")) {
                    // 网络图片
                    contact.uploadImage(URL(fileValue).toStream().toExternalImage()).also {
                        if (cache) {
                            imageCache[fileCache] = it
                        }
                    }
                } else {
                    var cacheKey = fileCache
                    val localFile: File = FileUtil.file(fileValue)
                    val externalImage = if (!localFile.exists()) {
                        // 尝试看看有没有url参数, 如果没有则抛出异常
                        val url = this@toMessageAsync["url"] ?: throw FileNotFoundException(fileValue)
                        cacheKey = url.toImgVoiceCacheKey(contact)
                        // 如果有，通过url发送
                        URL(url).toStream().toExternalImage()
                    } else {
                        localFile.toExternalImage()
                    }


                    contact.uploadImage(externalImage).also {
                        if (cache) {
                            imageCache[cacheKey] = it
                        }
                    }.run {
                        if (this@toMessageAsync["destruct"] == "true") {
                            this.flash()
                        } else this
                    }
                }
            }

            // 如果是闪照则转化
            if (this@toMessageAsync["destruct"] == "true") {
                image.flash()
            } else {
                image
            }
        }

        //endregion


        //region record 语音
        "voice", "record" -> contact.async {
            // voice 类型的CQ码，参数一般是file
            val fileValue = this@toMessageAsync["file"] ?: this@toMessageAsync["voice"] ?: throw CQCodeParamNullPointerException(
                "file",
                "voice"
            )
            // 先找缓存

            val fileCache = fileValue.toImgVoiceCacheKey(contact)

            val voiceCache = cacheMaps.voiceCache

            // 截止到1.2.0, 只支持Group.uploadVoice
            // see https://github.com/mamoe/mirai/releases/tag/1.2.0
            // return @async
            voiceCache[fileCache] ?: if (contact is Group) {
                val cache: Boolean = this@toMessageAsync["cache"] != "false"
                if (fileValue.startsWith("http")) {
                    // 网络图片
                    val stream = URL(fileValue).toStream()
//                    contact.async {
                    stream.uploadAsGroupVoice(contact).also {
                        kotlin.runCatching { stream.close() }
                        if (cache) {
                            voiceCache[fileCache] = it
                            voiceCache[it.fileName.toImgVoiceCacheKey(contact)] = it
                        }
                    }
                } else {
                    // 本地文件
                    val voiceFile = File(fileValue)
                    val stream = BufferedInputStream(FileInputStream(voiceFile))
                    contact.uploadVoice(stream).also {
                        kotlin.runCatching { stream.close() }
                        if (cache) {
                            voiceCache[fileCache] = it
                            voiceCache[it.fileName.toImgVoiceCacheKey(contact)] = it
                        }
                    }
                }
            } else {
                EmptyMessageChain
            }
        }
        //endregion


        //region rps 猜拳
        "rps" -> {
            EmptyMessageChain.async()
            // 似乎也不支持猜拳
//            PlainText("[猜拳]")
        }
        //endregion

        //region dice 骰子
        "dice" -> {
            EmptyMessageChain.async()
            // 似乎也..
//            PlainText("[骰子]")
        }
        //endregion

        //region shake 戳一戳
        "shake", "poke" -> {
            // 戳一戳进行扩展，可多解析'type'参数与'id'参数。
            // 如果没有type，直接返回戳一戳
            // 如果是再群内发送，则认为是头像戳一戳
            val type = this["type"]?.toInt() // ?: return PokeMessage.Poke.async()
            val id = this["id"]?.toInt() ?: -1

            // 如果目标是一个群成员，则说明使用双击头像的”戳一戳“
            // 此戳一戳将会被立即发送，并返回一个空消息串
            when (contact) {
                is Group -> {
                    val code: Long = this["code"]?.toLong() ?: throw IllegalArgumentException("cannot found nudge target: code is empty.")
                    val nudge: Nudge = contact.getOrNull(code)?.nudge() ?: throw IllegalArgumentException("cannot found nudge target: no such member($code) in group(${contact.id}).")
                    // 获取群员并发送
                    contact.async {
                        contact.sendNudge(nudge)
                        EmptyMessageChain
                    }
                }
                else -> {
                    // 尝试寻找对应的Poke，找不到则返回戳一戳
                    return if (type == null) {
                        PokeMessage.Poke
                    } else {
                        (PokeMessage.values.find { it.type == type && it.id == id } ?: PokeMessage.Poke)
                    }.async()
                }
            }
        }
        //endregion

        //region 双击头像戳一戳
        "nudge" -> {
            when(contact) {
                // 如果是群
                is Group -> {
                    val code: Long = this["target"]?.toLong() ?: throw IllegalArgumentException("cannot found nudge target: target is empty.")
                    val nudge: Nudge = contact.getOrNull(code)?.nudge() ?: throw NoSuchElementException("cannot found nudge target: no such member($code) in group(${contact.id}).")
                    // 获取群员并发送
                    contact.async {
                        contact.sendNudge(nudge)
                        EmptyMessageChain
                    }
                }
                is User -> {
                    val nudge: Nudge = contact.nudge()
                    contact.async {
                        contact.sendNudge(nudge)
                        EmptyMessageChain
                    }
                }
                // 是其他人
                else -> EmptyMessageChain.async()
            }
        }
        //endregion

        //region anonymous
        "anonymous" -> {
            // 匿名消息，不进行解析
            EmptyMessageChain.async()
        }
        //endregion

        //region music
        "music" -> {
            // 音乐，就是分享，应该归类于xml
            // 参数："type", "id", "style*"
            // 或者："type", "url", "audio", "title", "content*", "image*"
            val type = this["type"] ?: ""
            val title = this["title"] ?: ""
            val urlOrId = this["url"] ?: this["id"] ?: ""
            PlainText("""
                |[${type}音乐]
                |$title
                |$urlOrId
            """.trimMargin()).async()
        }
        //endregion

        //region share
        "share" -> {
            EmptyMessageChain.async()
            // 分享
            // 参数："url", "title", "content*", "image*"
//            val type = this["url"]
//            val title = this["title"]
//            PlainText("$title: $type")
        }
        //endregion

        //region emoji
        "emoji" -> {
            EmptyMessageChain.async()
            // emoji, 基本用不到
            // val id = this["id"] ?: ""
            // PlainText("emoji($id)")
        }
        //endregion


        //region location
        "location" -> {
            EmptyMessageChain.async()
            // 地点 "lat", "lon", "title", "content"
//            val lat = this["lat"] ?: ""
//            val lon = this["lon"] ?: ""
//            val title = this["title"] ?: ""
//            val content = this["content"] ?: ""
//            PlainText("位置($lat,$lon)[$title]:$content")
        }
        //endregion

        //region sign
        "sign" -> EmptyMessageChain.async()

        //endregion

        //region show
        "show" -> EmptyMessageChain.async()
        //endregion


        //region contact
        "contact" -> {
            EmptyMessageChain.async()
            // 联系人分享
            // ype一般可能是qq或者group
            // [CQ:contact,id=1234546,type=qq]
            // val id = this["id"] ?: return EmptyMessageChain

            // val typeName = when(this["type"]){
            //     "qq" -> "好友分享"
            //     "group" -> "群聊分享"
            //     else -> "其他分享"
            // }
            // PlainText("$typeName: $id")
        }
        //endregion

        //region xml message
        //对XML类型的CQ码做解析
        "xml" -> {
            val xmlCode = this
            // 解析的参数
//            val action = this["action"] ?: "plugin"
//            val flag: Int = this["flag"]?.toInt() ?: 3
//            val url = this["url"] ?: ""
//            val sourceName = this["sourceName"] ?: ""
//            val sourceIconURL = this["sourceIconURL"] ?: ""

            // 构建xml
            return buildXmlMessage(60) {
                // action
                xmlCode["action"]?.also { this.action = it }
                // 一般为点击这条消息后跳转的链接
                xmlCode["actionData"]?.also { this.actionData = it }
                /*
                   摘要, 在官方客户端内消息列表中显示
                 */
                xmlCode["brief"]?.also { this.brief = it }
                xmlCode["flag"]?.also { this.flag = it.toInt() }
                xmlCode["url"]?.also { this.url = it }
                // sourceName 好像是名称
                xmlCode["sourceName"]?.also { this.sourceName = it }
                // sourceIconURL 好像是图标
                xmlCode["sourceIconURL"]?.also { this.sourceIconURL = it }

                // builder
//                val keys = xmlCode.params.keys

                this.item {
                    xmlCode["bg"]?.also { this.bg = it.toInt() }
                    xmlCode["layout"]?.also { this.layout = it.toInt() }
                    // picture(coverUrl: String)
                    xmlCode["picture_coverUrl"]?.also { this.picture(it) }
                    // summary(text: String, color: String = "#000000")
                    xmlCode["summary_text"]?.also {
                        val color: String = xmlCode["summary_color"] ?: "#000000"
                        this.summary(it, color)
                    }
                    // title(text: String, size: Int = 25, color: String = "#000000")
                    xmlCode["title_text"]?.also {
                        val size: Int = xmlCode["title_size"]?.toInt() ?: 25
                        val color: String = xmlCode["title_color"] ?: "#000000"
                        this.title(it, size, color)
                    }

                }

            }.async()
        }
        //endregion


        //region lightApp小程序 & json
        // 一般都是json消息
        "app", "json" -> {
            val content: String = this["content"] ?: "{}"
            LightApp(content).async()
        }
        //endregion


        //region rich 或 service, 对应serviceMessage。
        "rich", "service" -> {
            val content: String = this["content"] ?: "{}"
            // 如果没有serviceId，认为其为lightApp
            val serviceId: Int = this["serviceId"]?.toInt() ?: return LightApp(content).async()
            ServiceMessage(serviceId, content).async()
        }
        //endregion

        //region quote
        // 引用回复
        "quote" -> {
            val key = this["id"] ?: this["quote"] ?: throw CQCodeParamNullPointerException("quote", "id")
            val source = cacheMaps.recallCache.get(key, contact.bot.id) ?: return EmptyMessageChain.async()
            QuoteReply(source).async()
        }
        //endregion


        //region else
        else -> {
            val handler = CQCodeParsingHandler[this.type]
            return if (handler != null) {
                handler(this, contact)
            } else {
                PlainText(this.toString()).async()
            }
        }
        //endregion
        //endregion


    }


}

/**
 * 一个非挂起返回值得到[Deferred]实例
 */
private fun Message.async(): Deferred<Message> {
    return when (this) {
        is EmptyMessageChain -> EmptyMessageChainDeferred
//        else -> coroutineScope.async(start = CoroutineStart.LAZY) { this@async }
        else -> SimpleDefaultDeferred(this)
    }
}


/**
 * ktor http client
 */
private val httpClient: HttpClient = HttpClient()

/**
 * 通过http网络链接得到一个输入流。
 * 通常认为是一个http-get请求
 */
private suspend fun URL.toStream(): InputStream {
    val urlString = this.toString()
    QQLog.debug("mirai.http.connection.try", urlString)
    val response = httpClient.get<HttpResponse>(this)
    val status = response.status
    if (status.value < 300) {
        QQLog.debug("mirai.http.connection.success", urlString)
        // success
        return response.content.toInputStream()
    } else {
        throw IllegalStateException("connection to '$urlString' failed ${status.value}: ${status.description}")
    }
}


/**
 * 转化函数
 */
@FunctionalInterface
interface CQCodeHandler : (KQCode, Contact) -> Deferred<Message>, BiFunction<KQCode, Contact, Deferred<Message>> {
    @JvmDefault
    override fun apply(code: KQCode, contact: Contact): Deferred<Message> = this.invoke(code, contact)
}


/**
 * 可注册的额外解析器
 */
object CQCodeParsingHandler {

    /** 注册额外的解析器 */
    private val otherHandler: MutableMap<String, CQCodeHandler> by lazy { mutableMapOf<String, CQCodeHandler>() }

    /**
     * get
     */
    operator fun get(cqType: String) = otherHandler[cqType]

    /**
     * set, same as [registerHandler]
     */
    internal operator fun set(cqType: String, handler: CQCodeHandler) {
        registerHandler(cqType, handler)
    }

    /**
     * 注册一个处理器。
     * @param cqType 要解析的类型
     * @param handler 解析器
     */
    @JvmStatic
    fun registerHandler(cqType: String, handler: CQCodeHandler) {
        if (otherHandler.containsKey(cqType)) {
            throw CQCodeParseHandlerRegisterException("failed.existed", cqType)
        } else {
            otherHandler[cqType] = handler
        }
    }

    /**
     * 获取所有处理器
     */
    @JvmStatic
    fun handlers(): Map<String, CQCodeHandler> = otherHandler.toMap()

}


/**
 * mirai码格式化兼容工具
 */
object MiraiCodeFormatUtils {

    /**
     * 字符串替换，替换消息中的`mirai`码为`cq`码
     */
    @JvmStatic
    fun mi2cq(msg: MessageChain?, cacheMaps: CacheMaps): String? {
        if (msg == null) {
            return null
        }

        // 携带mirai码的字符串
        return msg.asSequence()
            .map { it.toCqOrTextString(cacheMaps) }
            .joinToString("")
    }

}

/**
 * [SingleMessage] to cqcode string
 */
fun SingleMessage.toCqOrTextString(cacheMaps: CacheMaps): String {
    return if (this !is MessageSource) {
        when (this) {
            // 普通的文本消息，转义并普通的返回
            is PlainText -> CQEncoder.encodeText(content)

            // voice, 转化为record类型的cq码
            is Voice -> {
                val builder: CodeBuilder<String> = KQCodeUtils.getStringBuilder("record")
                    .key("file").value(fileName)
                    .key("size").value(fileSize)
                url?.run { builder.key("url").value(this) }
                builder.build()
            }

            // 普通image
            is Image -> {
                toCq(cacheMaps.imageCache, false)
            }

            // 闪照
            is FlashImage -> {
                image.toCq(cacheMaps.imageCache, true)
            }

            is At -> {
                KQCodeUtils.getStringBuilder("at")
                    .key("qq").value(target)
                    .key("display").value(display)
                    .build()
            }

            // at all
            is AtAll -> com.simplerobot.modules.utils.AtAll.toString()


            // face -> id
            is Face -> {
                KQCodeUtils.toCq("face", false, "id=$id")
            }

            // poke message, get id & type
            is PokeMessage -> {
                // val pokeMq = MQCodeUtils.toMqCode(this.toString())
                // val pokeKq = pokeMq.toKQCode().mutable()
                // pokeKq["type"] = this.type.toString()
                // pokeKq["id"] = this.id.toString()
                // pokeKq

                KQCodeUtils.toCq("poke", false, "name=$name", "type=$type", "id=$id")
            }

            // 引用
            is QuoteReply -> {
                // val quoteMq = MQCodeUtils.toMqCode(this.toString())
                // val quoteKq = quoteMq.toKQCode().mutable()
                // quoteKq["id"] = this.source.toCacheKey()
                // quoteKq["qq"] = this.source.fromId.toString()
                // quoteKq
                KQCodeUtils.toCq("quote", false, "id=${source.toCacheKey()}", "qq=${source.fromId}")
            }

            // 富文本
            is RichMessage -> when (this) {
                // app
                is LightApp -> {
                    KQCodeUtils.toCq("app", true, "content=$content")
                }
                // service message
                is ServiceMessage -> {
                    KQCodeUtils.toCq("service", true, "content=$content", "serviceId=$serviceId")
                }
                else -> {
                    val string = toString()
                    return if (string.trim().startsWith("[mirai:")) {
                        MQCodeUtils.toMqCode(string).toKQCode().toString()
                    } else string
                }
            }

            // 其他东西，不做特殊处理
            else -> {
                val string: String = toString()
                return if (string.trim().startsWith("[mirai:")) {
                    MQCodeUtils.toMqCode(string).toKQCode().toString()
                } else string
            }
        }
    } else ""
}

/**
 * 将一个[Image]实例转化为[KQCode]. 如果[imageCache]不为null, 则会缓存.
 * [flash]代表其是否为闪照.
 */
private fun Image.toCq(imageCache: ImageCache?, flash: Boolean): String {
    // 缓存image
    val imageId = this.imageId
    imageCache?.set(imageId, this)

    // builder

    val builder: CodeBuilder<String> = KQCodeUtils.getStringBuilder("image")
        .key("file").value(imageId)
        .key("url").value(runBlocking { queryUrl() })

    // val imageMq = MQCodeUtils.toMqCode(this.toString())
    // val imageKq = imageMq.toKQCode().mutable()
    // imageKq["file"] = imageId
    // imageKq["url"] = runBlocking { queryUrl() }
    if (flash) {
        builder.key("destruct").value("true")
        // imageKq["destruct"] = "true"
    }
    return builder.build()
}

