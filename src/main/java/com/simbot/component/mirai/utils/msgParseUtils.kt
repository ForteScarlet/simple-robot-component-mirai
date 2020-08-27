/*
 * Copyright (c) 2020. ForteScarlet All rights reserved.
 * Project  component-mirai (Codes other than Mirai)
 * File     utils.kt (Codes other than Mirai)
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

//@file:Suppress("unused")

package com.simbot.component.mirai.utils

import cn.hutool.core.io.FileUtil
import com.simbot.component.mirai.CQCodeParamNullPointerException
import com.simbot.component.mirai.CQCodeParseHandlerRegisterException
import com.simbot.component.mirai.CacheMaps
import com.simbot.component.mirai.collections.toCacheKey
import com.simplerobot.modules.utils.*
import kotlinx.coroutines.*
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.Friend
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.message.MessageReceipt
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.message.data.AtAll
import net.mamoe.mirai.message.uploadAsGroupVoice
import net.mamoe.mirai.utils.toExternalImage
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.net.URL
import java.util.function.BiFunction
import kotlin.collections.set


/**
 * 发送消息之前，会将Message通过此处进行处理。
 * 此处可解析部分CQ码并转化为Message
 * 然后发送此消息
 */
@Suppress("EXPERIMENTAL_API_USAGE")
public suspend fun <C : Contact> C.sendMsg(msg: String, cacheMaps: CacheMaps): MessageReceipt<Contact> {
    return this.sendMessage(msg.toWholeMessage(this, cacheMaps))
}


/**
 * 字符串解析为[Message]
 * 一般解析其中的CQ码
 * 等核心支持CAT码中转后转化为CAT码
 */
//@ExperimentalCoroutinesApi
fun String.toWholeMessage(contact: Contact, cacheMaps: CacheMaps): Message {
    // 切割，解析CQ码并拼接最终结果
    return KQCodeUtils.split(this).asSequence().map {
        if (it.trim().startsWith("[CQ:")) {
            // 如果是CQ码，转化为KQCode并进行处理
            KQCode.of(it).toMessage(contact, cacheMaps)
        } else {
            PlainText(CQDecoder.decodeText(it)!!).async(contact)
        }
    }.map {
        runBlocking { it.await() }.also { _ ->
            it.invokeOnCompletion {
                e -> e?.also { ex -> throw ex }
            }
        }
    }.reduce { acc, msg ->
        when{
            acc is EmptyMessageChain && msg is EmptyMessageChain -> EmptyMessageChain
            msg is EmptyMessageChain -> acc
            acc is EmptyMessageChain -> msg
            else ->  acc + msg
        }
    }
}

/**
 * KQCode转化为Message对象
 */
fun KQCode.toMessage(contact: Contact, cacheMaps: CacheMaps): Deferred<Message> {
    // 判断类型，有些东西有可能并不存在与CQ码规范中，例如XML
    return when (this.type) {
        //region CQ码解析为Message
        //region at
        "at" -> {
            val id = this["qq"] ?: this["at"] ?: throw CQCodeParamNullPointerException("at", "qq", "at")
            if (id == "all") {
                AtAll.async(contact)
            } else {
                when (contact) {
                    is Member -> {
                        At(contact).async(contact)
                    }
                    is Friend -> {
                        PlainText("@${contact.nick} ").async(contact)
                    }
                    is Group -> {
                        At(contact[id.toLong()]).async(contact)
                    }
                    else -> PlainText("@$id").async(contact)
                }
            }
        }
        //endregion

        //region face
        "face" -> Face((this["id"] ?: this["face"])!!.toInt()).async(contact)
        //endregion


        //region image
        "image" -> {
            // image 类型的CQ码，参数一般是file, destruct
            val file = this["file"] ?: this["image"] ?: throw CQCodeParamNullPointerException("image", "file", "image")

            val imageCache = cacheMaps.imageCache

            // 先查缓存
            var image: Image? = imageCache[file]

            if (image == null) {
                // 是否缓存此上传的图片
                val cache: Boolean = this["cache"] != "false"
                image = if (file.startsWith("http")) {
                    // 网络图片 阻塞上传
                    runBlocking {
                        val externalImage = URL(file).openStream().toExternalImage()
                        contact.uploadImage(externalImage).also {
                            if (cache) {
                                imageCache[file] = it
                            }
                        }
                    }
                } else {
                    var cacheKey = file
                    val localFile: File = FileUtil.file(file)
                    val externalImage = if (!localFile.exists()) {
                        // 尝试看看有没有url参数, 如果没有则抛出异常
                        val url = this@toMessage["url"] ?: throw FileNotFoundException(file)
                        cacheKey = url
                        // 如果有，通过url发送
                        URL(url).openStream().toExternalImage()
                    } else {
                        localFile.toExternalImage()
                    }
//                    contact.uploadImage(externalImage).also { imageCache[url] = it }
//                    uploadImage.also { imageCache[file] = it }

                    // async def
                    return contact.async {
                        contact.uploadImage(externalImage).also {
                            if (cache) {
                                imageCache[cacheKey] = it
                            }
                        }.also {
                            if (this@toMessage["destruct"] == "true") {
                                it.flash()
                            } else {
                                it
                            }
                        }
                    }

//                    // 不是http开头的, 则认为是本地图片
//                    runBlocking {
//                        val localFile: File = FileUtil.file(file)
//                        if(!localFile.exists()){
//                            // 尝试看看有没有url参数, 如果没有则抛出异常
//                            val url = this@toMessage["url"] ?: throw FileNotFoundException(file)
//                            // 如果有，通过url发送
//                            val externalImage = URL(url).openStream().toExternalImage()
//                            contact.uploadImage(externalImage).also { imageCache[url] = it }
//                        }else{
//                            val uploadImage = localFile.uploadAsImage(contact)
//                            uploadImage.also { imageCache[file] = it }
//                        }
//                    }
                }
            }

//            // 先查询缓存中有没有这个东西
//            // 本地文件
//            imageCache[file] ?:

            // file文件，可能是本地的或者网络的
//            val image: Image =
            // 如果是闪照则转化
            return if (this["destruct"] == "true") {
                image.flash()
            } else {
                image
            }.async(contact)
        }
        //endregion


        //region record 语音
        "voice", "record" -> {
            // voice 类型的CQ码，参数一般是file
            val file = this["file"] ?: this["voice"] ?: throw CQCodeParamNullPointerException("image", "file", "voice")
            // 先找缓存

            val voiceCache = cacheMaps.voiceCache

            // 截止到1.2.0, 只支持Group.uploadVoice
            // see https://github.com/mamoe/mirai/releases/tag/1.2.0
            return voiceCache[file]?.async(contact) ?: if (contact is Group) {
                val cache: Boolean = this["cache"] != "false"
                if (file.startsWith("http")) {
                    // 网络图片
                    val stream = URL(file).openStream()
                    contact.async {
                        stream.uploadAsGroupVoice(contact).also {
                            if (cache) {
                                voiceCache[file] = it
                                voiceCache[it.fileName] = it
                            }
                        }
                    }
                } else {
                    // 本地文件
                    val voiceFile = File(file)
                    val stream = BufferedInputStream(FileInputStream(voiceFile))
                    contact.async {
                        contact.uploadVoice(stream).also {
                            if (cache) {
                                voiceCache[file] = it
                                voiceCache[it.fileName] = it
                            }
                        }
                    }
                }
            } else {
                EmptyMessageChain.async(contact)
            }
        }
        //endregion


        //region rps 猜拳
        "rps" -> {
            EmptyMessageChain.async(contact)
            // 似乎也不支持猜拳
//            PlainText("[猜拳]")
        }
        //endregion

        //region dice 骰子
        "dice" -> {
            EmptyMessageChain.async(contact)
            // 似乎也..
//            PlainText("[骰子]")
        }
        //endregion

        //region shake 戳一戳
        "shake" -> {
            // 戳一戳进行扩展，可多解析'type'参数与'id'参数。
            // 如果没有type，直接返回戳一戳
            val type = this["type"]?.toInt() ?: return PokeMessage.Poke.async(contact)
            val id = this["id"]?.toInt() ?: -1

            // 尝试寻找对应的Poke，找不到则返回戳一戳
            return (PokeMessage.values.find { it.type == type && it.id == id } ?: PokeMessage.Poke).async(contact)
        }
        //endregion

        //region anonymous
        "anonymous" -> {
            // 匿名消息，不进行解析
            EmptyMessageChain.async(contact)
        }
        //endregion

        //region music
        "music" -> {
            // 音乐，就是分享，应该归类于xml
            // 参数："type", "id", "style*"
            // 或者："type", "url", "audio", "title", "content*", "image*"
            val type = this["type"]
            // TODO 解析music
            PlainText("[${type}音乐]").async(contact)
        }
        //endregion

        //region share
        "share" -> {
            EmptyMessageChain.async(contact)
            // 分享
            // 参数："url", "title", "content*", "image*"
//            val type = this["url"]
//            val title = this["title"]
//            PlainText("$title: $type")
        }
        //endregion

        //region emoji
        "emoji" -> {
            EmptyMessageChain.async(contact)
            // emoji, 基本用不到
            // val id = this["id"] ?: ""
            // PlainText("emoji($id)")
        }
        //endregion


        //region location
        "location" -> {
            EmptyMessageChain.async(contact)
            // 地点 "lat", "lon", "title", "content"
//            val lat = this["lat"] ?: ""
//            val lon = this["lon"] ?: ""
//            val title = this["title"] ?: ""
//            val content = this["content"] ?: ""
//            PlainText("位置($lat,$lon)[$title]:$content")
        }
        //endregion

        //region sign
        "sign" -> EmptyMessageChain.async(contact)

        //endregion

        //region show
        "show" -> EmptyMessageChain.async(contact)
        //endregion


        //region contact
        "contact" -> {
            EmptyMessageChain.async(contact)
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

            }.async(contact)
        }
        //endregion


        //region lightApp小程序 & json
        // 一般都是json消息
        "app", "json" -> {
            val content: String = this["content"] ?: "{}"
            LightApp(content).async(contact)
        }
        //endregion


        //region rich 或 service, 对应serviceMessage。
        "rich", "service" -> {
            val content: String = this["content"] ?: "{}"
            // 如果没有serviceId，认为其为lightApp
            val serviceId: Int = this["serviceId"]?.toInt() ?: return LightApp(content).async(contact)
            ServiceMessage(serviceId, content).async(contact)
        }
        //endregion

        //region quote
        // 引用回复
        "quote" -> {
            val key = this["id"] ?: this["quote"] ?: throw CQCodeParamNullPointerException("quote", "id")
            val source = cacheMaps.recallCache.get(key, contact.bot.id) ?: return EmptyMessageChain.async(contact)
            QuoteReply(source).async(contact)
        }
        //endregion


        //region else
        else -> {
            val handler = CQCodeParsingHandler[this.type]
            return if (handler != null) {
                handler(this, contact)
            } else {
                PlainText(this.toString()).async(contact)
            }
        }
        //endregion
        //endregion


    }


}

/**
 * 一个非挂起返回值得到[Deferred]实例
 */
private fun Message.async(coroutineScope: CoroutineScope): Deferred<Message> {
    return when(this){
        is EmptyMessageChain -> EmptyMessageChainDeferred
//        else -> coroutineScope.async(start = CoroutineStart.LAZY) { this@async }
        else -> SimpleDefaultDeferred(this)
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
    private val otherHandler: MutableMap<String, CQCodeHandler> by lazy { mutableMapOf() }

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
     * 字符串替换，替换消息中的`[mirai:`字符串为`[CQ:`
     */
    @JvmStatic
    fun mi2cq(msg: MessageChain?, cacheMaps: CacheMaps): String? {
        if (msg == null) {
            return null
        }

        // 携带mirai码的字符串
        return msg.asSequence().map { it.toCqString(cacheMaps) }.joinToString("")
    }

}

/**
 * [SingleMessage] to cqcode string
 */
fun SingleMessage.toCqString(cacheMaps: CacheMaps): String {
    if (this is MessageSource) {
        return ""
    }
    val kqCode: KQCode = when (this) {
        // voice, 转化为record类型的cq码
        is Voice -> {
            val voiceMq = MQCodeUtils.toMqCode(this.toString())
            val voiceKq = voiceMq.toKQCode().mutable()
            voiceKq.type = "record"
            this.url?.run { voiceKq["url"] = this }
            voiceKq["file"] = this.fileName
            voiceKq["size"] = this.fileSize.toString()
            voiceKq
        }

        // image, 追加file、url
        is Image -> {
            // 缓存image
            val imageId = this.imageId
            cacheMaps.imageCache[imageId] = this
            val imageMq = MQCodeUtils.toMqCode(this.toString())
            val imageKq = imageMq.toKQCode().mutable()
            imageKq["file"] = imageId
            imageKq["url"] = runBlocking { queryUrl() }
            if (this is FlashImage) {
                imageKq["destruct"] = "true"
            }
            imageKq
        }

        is At -> {
            KQCodeUtils.toKq("at", "qq" to this.target, "display" to this.display)
//                val atMq = MQCodeUtils.toMqCode(this.toString())
//                val atKq = atMq.toKQCode().mutable()
//                atKq["display"] = this.display
//                atKq["target"] = this.target.toString()
//                atKq
        }

        // at all
        is AtAll -> com.simplerobot.modules.utils.AtAll


        // face -> id
        is Face -> MQCodeUtils.toMqCode(this.toString()).toKQCode()

        // poke message, get id & type
        is PokeMessage -> {
            val pokeMq = MQCodeUtils.toMqCode(this.toString())
            val pokeKq = pokeMq.toKQCode().mutable()
            pokeKq["type"] = this.type.toString()
            pokeKq["id"] = this.id.toString()
            pokeKq
        }

        // 引用
        is QuoteReply -> {
            val quoteMq = MQCodeUtils.toMqCode(this.toString())
            val quoteKq = quoteMq.toKQCode().mutable()
            quoteKq["id"] = this.source.toCacheKey()
            quoteKq["qq"] = this.source.fromId.toString()
            quoteKq
        }

        // 富文本
        is RichMessage -> when (this) {
            // app
            is LightApp -> {
                val code = MutableKQCode("app")
                code["content"] = this.content
                code
            }
            // service message
            is ServiceMessage -> {
                val code = MutableKQCode("service")
                code["content"] = this.content
                code["serviceId"] = this.serviceId.toString()
                code
            }
            else -> {
                val string = this.toString()
                return if (string.trim().startsWith("[mirai:")) {
                    MQCodeUtils.toMqCode(string).toKQCode().toString()
                } else string
            }
        }

        // 其他东西，不做特殊处理
        else -> {
            val string = this.toString()
            return if (string.trim().startsWith("[mirai:")) {
                MQCodeUtils.toMqCode(string).toKQCode().toString()
            } else string
        }
    }
    return kqCode.toString()
}

