package com.simbot.component.mirai

import com.forte.utils.collections.CacheMap
import com.simplerobot.modules.utils.KQCode
import com.simplerobot.modules.utils.KQCodeUtils
import com.simplerobot.modules.utils.MQCodeUtils
import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.Friend
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.event.events.BotInvitedJoinGroupRequestEvent
import net.mamoe.mirai.event.events.MemberJoinRequestEvent
import net.mamoe.mirai.event.events.NewFriendRequestEvent
import net.mamoe.mirai.message.MessageReceipt
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.message.uploadImage
import java.io.File
import java.net.URL
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger


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
           PlainText(it)
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
            val id = this["qq"] ?: this["at"] ?: throw IllegalArgumentException("")
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
            val file = this["file"] ?: this["image"] ?: throw NullPointerException("can not found param file or image")
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
                   contact.uploadImage(File(file)).also { ImageCache[file] = it }
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
        "record" -> {
            // 似乎暂不支持
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

        //region xml, for mirai
        // TODO 增加对XML类型的CQ码做解析
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






        //region else
        else -> this.toString().toMessage()
        //endregion
        //endregion


    }


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
            when(it){
                // Voice，toString为url
                is Voice -> "[mirai:voice:${it.url}]"

                // image, 缓存Image并替换image参数为file
                is Image -> {
                    ImageCache[it.imageId] = it
                    it.toString()
                }
                // 其他情况，直接toString()
                else -> it.toString()
            }
        }.joinToString("")

        val replaceToCq = MQCodeUtils.replaceToCq(msgStr)
        // 转化voice类型的CQ码为record
                .replace("[CQ:voice,voice=", "[CQ:record,file=")
        // 移除"source"类型的cq码
        return KQCodeUtils.removeByType("source", replaceToCq)
    }




}




/** 缓存撤回消息用的id，每一个bot都有一个Map */
object RecallCache {
    /** botCacheMap */
    @JvmStatic
    private val botCacheMap: MutableMap<Long, LRUCacheMap<String, MessageSource>> = ConcurrentHashMap()

    /** 计数器，当计数器达到100的时候，触发缓存清除 */
    @JvmStatic
    private val counter: AtomicInteger = AtomicInteger(0)

    private const val CACHE_TIME: Long = 30

    /** 缓存消息记录 */
    @JvmStatic
    fun cache(receipt: MessageReceipt<*>): String = cache(receipt.source)

    /** 缓存消息记录 */
    @JvmStatic
    fun cache(source: MessageSource): String {
        val id = source.bot.id
        val key = "${source.id}.${source.internalId}.${source.time}"
        return cache(id, key, source)
    }

    /**
     * 记录一个缓存
     */
    private fun cache(botId: Long, id: String, source: MessageSource): String{
        // 获取缓存map
        val cacheMap = botCacheMap.computeIfAbsent(botId) { LRUCacheMap() }

        // 缓存
        cacheMap.putPlusMinutes(id, source, CACHE_TIME)

        // 计数+1, 如果大于100，清除缓存
        if(counter.addAndGet(1) >= 100){
            counter.set(0)
            synchronized(cacheMap){
                cacheMap.detect()
            }
        }
        return id
    }


    /** 获取缓存 */
    @JvmStatic
    fun get(key: String, botId: Long): MessageSource? {
        // 获取缓存值，可能为null
        return botCacheMap[botId]?.get(key)
    }

    /** 移除缓存 */
    @JvmStatic
    fun remove(key: String, botId: Long): MessageSource? {
        // 获取缓存值，可能为null
        return botCacheMap[botId]?.remove(key)
    }

}

/** 缓存请求相关消息用的id，每一个bot都有一个Map */
object RequestCache {
    /** botCacheMap */
    @JvmStatic
    private val friendRequestCacheMap: MutableMap<Long, LRUCacheMap<String, NewFriendRequestEvent>> = ConcurrentHashMap()

    /** 可能是[MemberJoinRequestEvent] 其他人入群 或者 [BotInvitedJoinGroupRequestEvent] 被邀请入群 */
    @JvmStatic
    private val joinRequestCacheMap: MutableMap<Long, LRUCacheMap<String, Any>> = ConcurrentHashMap()

    private const val CACHE_TIME: Long = 30

    /** 计数器，当计数器达到100的时候，触发缓存清除 */
    @JvmStatic
    private val counter: AtomicInteger = AtomicInteger(0)

    /** 缓存friend request，消息记录1小时 */
    @JvmStatic
    fun cache(request: NewFriendRequestEvent): String {
        // bot id
        val id = request.botId()
        val key = request.toKey()
        joinRequestCacheMap.cache(id, key, request)
//        // 获取缓存map
//        val cacheMap = friendRequestCacheMap.computeIfAbsent(id) { CacheMap() }
//        // 缓存30分钟
//        cacheMap.putPlusMinutes(key, request, CACHE_TIME)
//
//        // 计数+1, 如果大于100，清除缓存
//        if(counter.addAndGet(1) >= 100){
//            counter.set(0)
//            synchronized(cacheMap){
//                cacheMap.detect()
//            }
//        }
        return key
    }


    /** 缓存 join request，消息记录1小时 */
    @JvmStatic
    fun cache(request: MemberJoinRequestEvent): String {
        // bot id
        val id = request.botId()
        val key = request.toKey()
        joinRequestCacheMap.cache(id, key, request)
        return key
    }

    /** 缓存invited join request，消息记录1小时 */
    @JvmStatic
    fun cache(request: BotInvitedJoinGroupRequestEvent): String {
        // bot id
        val id = request.botId()
        val key = request.toKey()
        // 缓存30分钟
        joinRequestCacheMap.cache(id, key, request)
        return key
    }

    /** 进行缓存 */
    private fun <V> MutableMap<Long, LRUCacheMap<String, V>>.cache(botId: Long, key: String, value: V){
        val cacheMap = this.computeIfAbsent(botId) { LRUCacheMap() }
        // 缓存30分钟
        cacheMap.putPlusMinutes(key, value, CACHE_TIME)

        // 计数+1, 如果大于100，清除缓存
        if(counter.addAndGet(1) >= 100){
            counter.set(0)
            synchronized(cacheMap){
                cacheMap.detect()
            }
        }
    }

    /** 获取friend request缓存 */
    @JvmStatic
    fun getFriendRequest(botId: Long, key: String): NewFriendRequestEvent? {
        // 获取缓存值，可能为null
        return friendRequestCacheMap[botId]?.get(key)
    }

    /**
     * 获取join request缓存
     * 可能是[MemberJoinRequestEvent] 其他人入群 或者 [BotInvitedJoinGroupRequestEvent] 被邀请入群
     *
     */
    @JvmStatic
    fun getJoinRequest(botId: Long, key: String): Any? {
        // 获取缓存值，可能为null
        return joinRequestCacheMap[botId]?.get(key)
    }

    /** 移除一个friend request */
    @JvmStatic
    fun removeFriendRequest(botId: Long, key: String): Any? {
        return friendRequestCacheMap[botId]?.remove(key)
    }

    /** 移除一个join request */
    @JvmStatic
    fun removeJoinRequest(botId: Long, key: String): Any? {
        return joinRequestCacheMap[botId]?.remove(key)
    }
}


/**
 * 图片缓存器
 */
object ImageCache {
    // image缓存map
    @JvmStatic
    private val imageCacheMap = CacheMap<String, Image>()

    private const val CACHE_TIME: Long = 30

    /** 计数器，当计数器达到100的时候，触发缓存清除 */
    @JvmStatic
    private val counter: AtomicInteger = AtomicInteger(0)
    /** 获取 */
    @JvmStatic
    operator fun get(key: String): Image? {
        // 获取缓存, 并刷新时间
        val image = imageCacheMap[key] ?: return null
        imageCacheMap.putPlusMinutes(key, image, CACHE_TIME)
        return image
    }

    /** 记录一个map */
    @JvmStatic
    operator fun set(key: String, image: Image): Image? {
        val putImage = imageCacheMap.putPlusMinutes(key, image, CACHE_TIME)
        if(counter.addAndGet(1) > 100){
            counter.set(0)
            imageCacheMap.detect()
        }
        return putImage

    }

    /** 进行缓存 */
    @JvmStatic
    fun cache(key: String, image: Image): Image? = this.set(key, image)


}



/** 转化为key */
fun BotInvitedJoinGroupRequestEvent.toKey(): String = this.eventId.toString()

/** 转化为key */
fun MemberJoinRequestEvent.toKey(): String = this.eventId.toString()

/** 转化为key */
fun NewFriendRequestEvent.toKey(): String = this.eventId.toString()

/** 获取botid */
fun BotInvitedJoinGroupRequestEvent.botId(): Long = this.bot.id

/** 获取botid */
fun MemberJoinRequestEvent.botId(): Long = this.bot.id

/** 获取botid */
fun NewFriendRequestEvent.botId(): Long = this.bot.id


