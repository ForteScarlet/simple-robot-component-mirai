package com.simbot.component.mirai

import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.event.events.BotInvitedJoinGroupRequestEvent
import net.mamoe.mirai.event.events.MemberJoinRequestEvent
import net.mamoe.mirai.event.events.NewFriendRequestEvent
import net.mamoe.mirai.message.MessageReceipt
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.MessageSource
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger


/** 缓存撤回消息用的id，每一个bot都有一个Map */
object RecallCache {
    /** botCacheMap */
    @JvmStatic
    private val botCacheMap: MutableMap<Long, LRUCacheMap<String, MessageSource>> = ConcurrentHashMap()

    /** 计数器，当计数器达到100的时候，触发缓存清除 */
    @JvmStatic
    private val counter: AtomicInteger = AtomicInteger(0)

    /**
     * 清理缓存临界值
     */
    private const val CHECK: Int = 1000

    private const val CACHE_TIME: Long = 30

    /** 缓存消息记录 */
    @JvmStatic
    fun cache(receipt: MessageReceipt<*>): String = cache(receipt.source)

    /** 缓存消息记录 */
    @JvmStatic
    fun cache(source: MessageSource): String {
        val id = source.bot.id
        val key = source.toCacheKey()
        return cache(id, key, source)
    }

    /**
     * 记录一个缓存
     */
    private fun cache(botId: Long, key: String, source: MessageSource): String{
        // 获取缓存map
        val cacheMap = botCacheMap.computeIfAbsent(botId) { LRUCacheMap() }

        // 缓存
        cacheMap.putPlusMinutes(key, source, CACHE_TIME)

        // 计数+1, 如果大于100，清除缓存
        if(counter.addAndGet(1) >= CHECK){
            counter.set(0)
            synchronized(botCacheMap){
                botCacheMap.forEach{it.value.detect()}
            }
        }
        return key
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


fun MessageSource.toCacheKey() = "${this.id}.${this.internalId}.${this.time}"


/** 缓存请求相关消息用的id，每一个bot都有一个Map */
object RequestCache {
    /** botCacheMap */
    @JvmStatic
    private val friendRequestCacheMap: MutableMap<Long, LRUCacheMap<String, NewFriendRequestEvent>> = ConcurrentHashMap()

    /** 可能是[MemberJoinRequestEvent] 其他人入群 或者 [BotInvitedJoinGroupRequestEvent] 被邀请入群 */
    @JvmStatic
    private val joinRequestCacheMap: MutableMap<Long, LRUCacheMap<String, Any>> = ConcurrentHashMap()

    /**
     * 清理缓存临界值
     */
    private const val CHECK: Int = 1000

    /**
     * 这玩意，只缓存5分钟就行了
     */
    private const val CACHE_TIME: Long = 5

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
    private inline fun <V> MutableMap<Long, LRUCacheMap<String, V>>.cache(botId: Long, key: String, value: V){
        val cacheMap = this.computeIfAbsent(botId) { LRUCacheMap() }
        // 缓存30分钟
        cacheMap.putPlusMinutes(key, value, CACHE_TIME)

        // 计数+1, 如果大于100，清除缓存
        if(counter.addAndGet(1) >= CHECK){
            counter.set(0)
            synchronized(this){
                this.forEach { it.value.detect() }
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
    private val imageCacheMap by lazy { LRUCacheMap<String, Image>() }
    /**
     * 清理缓存临界值
     */
    private const val CHECK: Int = 1000

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
        if(counter.addAndGet(1) > CHECK){
            counter.set(0)
            imageCacheMap.detect()
        }
        return putImage

    }
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


/**
 * 发送消息的缓存器，只会在发送给非当前消息群的人的消息的时候触发缓存
 */
object ContactCache {

    // contact缓存map
    @JvmStatic
    private val contactCacheMap: MutableMap<Long, LRUCacheMap<Long, Contact>> by lazy { ConcurrentHashMap<Long, LRUCacheMap<Long, Contact>>() }

    private const val CACHE_TIME: Long = 15

    /**
     * 清理缓存临界值
     */
    private const val CHECK: Int = 1000

    /** 计数器，当计数器达到100的时候，触发缓存清除 */
    @JvmStatic
    private val counter: AtomicInteger = AtomicInteger(0)

    /**
     * 获取某个bot的缓存时间
     */
    private fun getBotCacheMap(botId: Long): LRUCacheMap<Long, Contact> {
        return contactCacheMap[botId] ?: run {
            val newMap = LRUCacheMap<Long, Contact>()
            contactCacheMap.compute(botId) {_, old -> old ?: newMap }
            newMap
        }
    }

    /**
     * 缓存所有信息
     */
    fun cache(bot: Bot) {
        val cacheMap = getBotCacheMap(bot.id)
        bot.groups.asSequence().flatMap { g -> g.members.asSequence() }.forEach {
            // 计入缓存
            val memberId = it.id
            cacheMap.putIfAbsent(memberId, it)
        }
    }

    /**
     * 获取
     * @param key 要获取的contact的id
     * @param bot 哪个bot要获取contact，之所以是bot是因为如果获取不到的话要用bot来缓存信息
     */
    @JvmStatic
    operator fun get(key: Long, bot: Bot): Contact? {
        val botId = bot.id
        // 此bot的缓存map
        val cacheMap = getBotCacheMap(botId)
        // 获取缓存
        return cacheMap[key]?.also {
            // 如果存在，刷新其缓存时间
            cacheMap.putPlusMinutes(key, it, CACHE_TIME)
        } ?: run {
            // 查询不到，尝试遍历并缓存所有的群聊群员。
            var find: Contact? = null
            bot.groups.asSequence().flatMap { g -> g.members.asSequence() }.forEach {
                // 计入缓存
                val memberId = it.id
                if(memberId == key){
                    find = it
                }
                cacheMap.putIfAbsent(memberId, it)
            }
            // 此时进行缓存清理计数
            if(counter.addAndGet(1) > CHECK){
                counter.set(0)
                synchronized(contactCacheMap){
                    contactCacheMap.forEach { it.value.detect() }
                }
            }
            find
        }
    }
}
