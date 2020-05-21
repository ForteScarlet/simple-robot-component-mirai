package com.simbot.component.mirai

import com.forte.utils.collections.CacheMap
import com.simplerobot.modules.utils.KQCodeUtils
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.Friend
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.event.events.BotInvitedJoinGroupRequestEvent
import net.mamoe.mirai.event.events.MemberJoinRequestEvent
import net.mamoe.mirai.event.events.NewFriendRequestEvent
import net.mamoe.mirai.message.MessageReceipt
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.MessageSource
import net.mamoe.mirai.message.data.source
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger


/**
 * 发送消息之前，会将Message通过此处进行处理。
 * 此处可解析部分CQ码并转化为Message
 * 然后发送此消息
 */
suspend fun <C: Contact> C.sendMsg(msg: String): MessageReceipt<Contact> {
    return this.sendMessage(msg)
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
        val msgStr = msg.toString()
        val cqStr = msgStr.replace("[mirai:", "[CQ:")

        val result = KQCodeUtils.split(cqStr)?.map {
            if(it.startsWith("[CQ:")){
                it.replace(":", "=").replace("[CQ=", "[CQ:")
            }else{
                it
            }
        }?.joinToString { "" }

        return result
    }


    /**
     * 字符串替换，替换消息中的`[CQ:`字符串为`[mirai:`
     */
    @JvmStatic
    fun cq2mi(msg: String?): String? = msg?.replace("[CQ:", "[mirai:")


}




/** 缓存撤回消息用的id，每一个bot都有一个Map */
object RecallCache {
    /** botCacheMap */
    @JvmStatic
    private val botCacheMap: MutableMap<Long, CacheMap<String, MessageSource>> = ConcurrentHashMap()

    /** 计数器，当计数器达到100的时候，触发缓存清除 */
    @JvmStatic
    private val counter: AtomicInteger = AtomicInteger(0)

    private const val CACHE_TIME: Long = 30

//    /** 缓存，消息记录10分钟 */
//    @JvmStatic
//    fun cache(receipt: MessageReceipt<*>): String {
//        val source = receipt.source
//        val id = source.bot.id
//        val keyType: String = when(receipt.target){
//            is Group -> "G"
//            is Friend -> "F"
//            is Member -> "M"
//            else -> "N"
//        }
//        val key = keyType + source.id.toString()
//        return cache(key, source)
//    }

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
        val cacheMap = botCacheMap.computeIfAbsent(botId) { CacheMap() }

        // 缓存10分钟
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
    private val friendRequestCacheMap: MutableMap<Long, CacheMap<String, NewFriendRequestEvent>> = ConcurrentHashMap()

    /** 可能是[MemberJoinRequestEvent] 其他人入群 或者 [BotInvitedJoinGroupRequestEvent] 被邀请入群 */
    @JvmStatic
    private val joinRequestCacheMap: MutableMap<Long, CacheMap<String, Any>> = ConcurrentHashMap()

    private const val CACHE_TIME: Long = 30

    /** 计数器，当计数器达到100的时候，触发缓存清除 */
    @JvmStatic
    private val counter: AtomicInteger = AtomicInteger(0)

    /** 缓存friend request，消息记录1小时 */
    @JvmStatic
    fun cache(request: NewFriendRequestEvent): String {
        // bot id
        val id = request.bot.id
        val key = request.eventId.toString()
        // 获取缓存map
        val cacheMap = friendRequestCacheMap.computeIfAbsent(id) { CacheMap() }
        // 缓存30分钟
        cacheMap.putPlusMinutes(key, request, CACHE_TIME)

        // 计数+1, 如果大于100，清除缓存
        if(counter.addAndGet(1) >= 100){
            counter.set(0)
            synchronized(cacheMap){
                cacheMap.detect()
            }
        }
        return key
    }


    /** 缓存 join request，消息记录1小时 */
    @JvmStatic
    fun cache(request: MemberJoinRequestEvent): String {
        // bot id
        val id = request.bot.id
        val key = request.eventId.toString()
        // 获取缓存map
        val cacheMap = joinRequestCacheMap.computeIfAbsent(id) { CacheMap() }
        // 缓存30分钟
        cacheMap.putPlusMinutes(key, request, CACHE_TIME)

        // 计数+1, 如果大于100，清除缓存
        if(counter.addAndGet(1) >= 100){
            counter.set(0)
            synchronized(cacheMap){
                cacheMap.detect()
            }
        }
        return key
    }

    /** 缓存invited join request，消息记录1小时 */
    @JvmStatic
    fun cache(request: BotInvitedJoinGroupRequestEvent): String {
        // bot id
        val id = request.bot.id
        val key = request.eventId.toString()
        // 获取缓存map
        val cacheMap = joinRequestCacheMap.computeIfAbsent(id) { CacheMap() }
        // 缓存30分钟
        cacheMap.putPlusMinutes(key, request, CACHE_TIME)

        // 计数+1, 如果大于100，清除缓存
        if(counter.addAndGet(1) >= 100){
            counter.set(0)
            synchronized(cacheMap){
                cacheMap.detect()
            }
        }
        return key
    }

    /** 获取friend request缓存 */
    @JvmStatic
    fun getFriendRequest(key: String, botId: Long): NewFriendRequestEvent? {
        // 获取缓存值，可能为null
        return friendRequestCacheMap[botId]?.get(key)
    }

    /**
     * 获取join request缓存
     * 可能是[MemberJoinRequestEvent] 其他人入群 或者 [BotInvitedJoinGroupRequestEvent] 被邀请入群
     *
     */
    @JvmStatic
    fun getJoinRequest(key: String, botId: Long): Any? {
        // 获取缓存值，可能为null
        return joinRequestCacheMap[botId]?.get(key)
    }

    /** 移除一个friend request */
    @JvmStatic
    fun removeFriendRequest(key: String, botId: Long): Any? {
        return friendRequestCacheMap[botId]?.remove(key)
    }

    /** 移除一个join request */
    @JvmStatic
    fun removeJoinRequest(key: String, botId: Long): Any? {
        return joinRequestCacheMap[botId]?.remove(key)
    }

}


