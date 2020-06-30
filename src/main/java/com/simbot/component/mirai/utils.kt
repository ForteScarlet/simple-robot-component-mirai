package com.simbot.component.mirai

import cn.hutool.core.io.FileUtil
import cn.hutool.core.io.resource.FileResource
import com.simplerobot.modules.utils.KQCode
import com.simplerobot.modules.utils.KQCodeUtils
import com.simplerobot.modules.utils.MQCodeUtils
import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.Friend
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.message.MessageReceipt
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.message.uploadImage
import java.io.File
import java.net.URL
import java.util.function.BiFunction


/**
 * 发送消息之前，会将Message通过此处进行处理。
 * 此处可解析部分CQ码并转化为Message
 * 然后发送此消息
 */
suspend fun <C: Contact> C.sendMsg(msg: String): MessageReceipt<Contact> {
    return this.sendMessage(msg.toWholeMessage(this))
}


/**
 * 字符串解析为[Message]
 * 一般解析其中的CQ码
 * 等核心支持CAT码中转后转化为CAT码
 */
fun String.toWholeMessage(contact: Contact): Message {
    // 切割，解析CQ码并拼接最终结果
   return KQCodeUtils.split(this).asSequence().map {
       if(it.trim().startsWith("[CQ:")){
           // 如果是CQ码，转化为KQCode并进行处理
           KQCode.of(it).toMessage(contact)
       }else{
           it.toMessage()
       }
   }.reduce { acc, msg -> acc + msg }
}

/**
 * KQCode转化为Message对象
 */
fun KQCode.toMessage(contact: Contact): Message {
    // 判断类型，有些东西有可能并不存在与CQ码规范中，例如XML
    return when(this.type) {
        //region CQ码解析为Message
        //region at
        "at" -> {
            val id = this["qq"] ?: this["at"] ?: throw CQCodeParamNullPointerException("at", "qq", "at")
            if(id == "all") {
                AtAll
            } else {
                when(contact) {
                    is Member -> {
                        At(contact)
                    }
                    is Friend -> {
                        "@${contact.nick}".toMessage()
                    }
                    is Group -> {
                        At(contact[id.toLong()])
                    }
                    else -> "@$id".toMessage()
                }
            }
        }
        //endregion

        //region face
        "face" ->  Face((this["id"] ?: this["face"])!!.toInt())
        //endregion


        //region image
        "image" -> {
            // image 类型的CQ码，参数一般是file, destruct
            val file = this["file"] ?: this["image"] ?: throw CQCodeParamNullPointerException("image", "file", "image")

            // file文件，可能是本地的或者网络的
            val image: Image = if(file.startsWith("http")){
                // 网络图片 阻塞上传
                runBlocking {
                    contact.uploadImage(URL(file)).also {  ImageCache[file] = it }
                }
            }else{
                // 先查询缓存中有没有这个东西
                // 本地文件
               ImageCache[file] ?: runBlocking {
//                   contact.uploadImage(File(file)).also { ImageCache[file] = it }
                   contact.uploadImage(FileUtil.file(file)).also { ImageCache[file] = it }
               }
            }
            // 如果是闪照则转化
            return if(this["destruct"] == "true"){
                image.flash()
            }else{
                image
            }
        }
        //endregion

        //region record
        "voice", "record" -> {
            // 似乎暂不支持，不过可转发
            "[语音]".toMessage()
        }
        //endregion


        //region rps 猜拳
        "rps" -> {
            // 似乎也不支持猜拳
            "[猜拳]".toMessage()
        }
        //endregion

        //region dice 骰子
        "dice" -> {
            // 似乎也..
            "[骰子]".toMessage()
        }
        //endregion

        //region shake 戳一戳
        "shake" -> {
            // 戳一戳进行扩展，可多解析'type'参数与'id'参数。
            // 如果没有type，直接返回戳一戳
            val type = this["type"]?.toInt() ?: return PokeMessage.Poke
            val id = this["id"]?.toInt() ?: -1

            // 尝试寻找对应的Poke，找不到则返回戳一戳
            return PokeMessage.values.find { it.type == type && it.id == id } ?: PokeMessage.Poke
        }
        //endregion

        //region anonymous
        "anonymous" -> {
            // 匿名消息，不进行解析
            EmptyMessageChain
        }
        //endregion

        //region music
        "music" -> {
            // 音乐，就是分享，应该归类于xml
            // 参数："type", "id", "style*"
            // 或者："type", "url", "audio", "title", "content*", "image*"
            val type = this["type"]
            // TODO 解析music
            "[${type}音乐]".toMessage()
        }
        //endregion

        //region share
        "share" -> {
            // 分享
            // 参数："url", "title", "content*", "image*"
            val type = this["url"]
            val title = this["title"]
            // TODO 解析share
            "$title: $type".toMessage()
        }
        //endregion

        //region emoji
        "emoji" -> {
            // emoji, 基本用不到
            val id = this["id"] ?: ""
            "emoji($id)".toMessage()
        }
        //endregion


        //region location
        "location" -> {
            // 地点 "lat", "lon", "title", "content"
            val lat = this["lat"] ?: ""
            val lon = this["lon"] ?: ""
            val title = this["title"] ?: ""
            val content = this["content"] ?: ""
            "位置($lat,$lon)[$title]:$content".toMessage()
        }
        //endregion

        //region sign
        "sign" ->  "[签到]".toMessage()

        //endregion

        //region show
        "show" -> EmptyMessageChain
        //endregion


        //region contact
        "contact" -> {
            // TODO 联系人分享 可改成xml
            // ype一般可能是qq或者group
            // [CQ:contact,id=1234546,type=qq]
            val id = this["id"] ?: return EmptyMessageChain

            val typeName = when(this["type"]){
                "qq" -> "好友分享"
                "group" -> "群聊分享"
                else -> "其他分享"
            }
            "$typeName: $id".toMessage()
        }
        //endregion

        //region xml message
        //对XML类型的CQ码做解析
        "xml" -> {
            // 解析的参数
            val action = this["action"] ?: "plugin"
            val actionData = this["actionData"] ?: ""
            val brief = this["brief"] ?: ""
            val flag: Int = this["flag"]?.toInt() ?: 3
            val url = this["url"] ?: ""
            val sourceName = this["sourceName"] ?: ""
            val sourceIconURL = this["sourceIconURL"] ?: ""

            // 构建xml
            return buildXmlMessage(60) {
                // 一般为点击这条消息后跳转的链接
                this.actionData = actionData
                // action
                this.action = action
                /**
                 * 摘要, 在官方客户端内消息列表中显示
                 */
                this.brief = brief
                this.flag = flag
                this.url = url
                // sourceName 好像是名称
                this.sourceName = sourceName
                // sourceIconURL 好像是图标
                this.sourceIconURL = sourceIconURL
            }
        }
        //endregion


        //region lightApp小程序 & json
        // 一般都是json消息
        "app","json" -> {
            val content: String = this["content"] ?: "{}"
            LightApp(content)
        }
        //endregion


        //region quote
        // 引用回复
        "quote" -> {
            val key = this["id"] ?: this["quote"] ?: throw CQCodeParamNullPointerException("quote", "id")
            val source = RecallCache.get(key, contact.bot.id) ?: return EmptyMessageChain
            QuoteReply(source)
        }
        //endregion



        //region else
        else -> {
            val handler = CQCodeParsingHandler[this.type]
            return if(handler != null){
                handler(this, contact)
            }else{
                this.toString().toMessage()
            }
        }
        //endregion
        //endregion


    }


}

/**
 * 转化函数
 */
@FunctionalInterface
interface CQCodeHandler: (KQCode, Contact) -> Message, BiFunction<KQCode, Contact, Message> {
    @JvmDefault
    override fun apply(code: KQCode, contact: Contact): Message = this.invoke(code, contact)
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
        }else{
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
    fun mi2cq(msg: MessageChain?): String? {
        if(msg == null){
            return null
        }

        // 携带mirai码的字符串
        val msgStr = msg.asSequence().map {
            it.toCqString()
//            when(it){
//                // Voice，toString为url
//                is Voice -> "[mirai:voice:${it.url}]"
//
//                // image, 缓存Image
//                is Image -> {
//                    ImageCache[it.imageId] = it
//                    it.toString()
//                }
//
//                // 其他情况，先直接toString()
//                else -> it.toString()
//            }
        }.joinToString("")

//        val replaceToCq = MQCodeUtils.replaceToCq(msgStr)
//        // 转化voice类型的CQ码为record
//                .replace("[CQ:voice,voice=", "[CQ:record,file=")
        // 移除"source"类型的cq码
//        return KQCodeUtils.removeByType("source", replaceToCq)
        return msgStr
    }


    fun SingleMessage.toCqString(): String {
        if(this is MessageSource){
            return ""
        }
        val kqCode: KQCode = when(this){
            // voice, 转化为record类型的cq码
            is Voice -> {
                val voiceMq = MQCodeUtils.toMqCode(this.toString())
                val value = voiceMq.param
                val voiceKq = voiceMq.toKQCode().mutable()
                voiceKq.type = "record"
                voiceKq["file"] = value
                voiceKq["url"] = this.url
                voiceKq["fileName"] = this.fileName
                voiceKq
            }

            // image, 追加file、url
            is Image -> {
                // 缓存image
                ImageCache[this.imageId] = this
                val imageMq = MQCodeUtils.toMqCode(this.toString())
                val value = imageMq.param
                val imageKq = imageMq.toKQCode().mutable()
                imageKq["file"] = value
                imageKq["url"] = runBlocking { queryUrl() }
                if(this is FlashImage){
                    imageKq["destruct"] = "true"
                }
                imageKq
            }

            is At -> {
                val atMq = MQCodeUtils.toMqCode(this.toString())
                val value = atMq.param
                val atKq = atMq.toKQCode().mutable()
                atKq["qq"] = value
                atKq["display"] = this.display
                atKq["target"] = this.target.toString()
                atKq
            }

            // at all
            is AtAll -> AtAllKQCode

            // face -> id
            is Face -> {
                val faceMq = MQCodeUtils.toMqCode(this.toString())
                val value = faceMq.param
                val faceKq = faceMq.toKQCode().mutable()
                faceKq["id"] = value
                faceKq
            }

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


            else -> {
                val string = this.toString()
                return if(string.trim().startsWith("[mirai:")){
                    MQCodeUtils.toMqCode(string).toKQCode().toString()
                }else string
            }
        }
        return kqCode.toString()
    }



}

/** at全体的KQCode */
object AtAllKQCode: KQCode("at", "qq" to "all")




