# 1.3.0-1.16
- 追加实现mirai的好友头像更换事件。
```java
    @Listen.ByName(MiraiEvents.friendAvatarChangedEvent)
    public void event1() { }

    // 或

    @OnFriendAvatarChanged
    public void event2() { }
```

- 追加一些可配置的信息：
```properties
# suppress inspection "UnusedProperty" for whole file

# mirai心跳周期. 过长会导致被服务器断开连接. 单位毫秒
simbot.mirai.heartbeatPeriodMillis=60000

# 每次心跳时等待结果的时间.
# 一旦心跳超时, 整个网络服务将会重启 (将消耗约 1s). 除正在进行的任务 (如图片上传) 会被中断外, 事件和插件均不受影响.
# . 单位毫秒
simbot.mirai.heartbeatTimeoutMillis=5000

# 心跳失败后的第一次重连前的等待时间. 默认5000ms
simbot.mirai.firstReconnectDelayMillis=5000

# 重连失败后, 继续尝试的每次等待时间 默认5000ms
simbot.mirai.reconnectPeriodMillis=5000

# 最多尝试多少次重连. 此处默认值为Integer.MAX_VALUE
simbot.mirai.reconnectionRetryTimes=

# 使用的协议类型，值为枚举 [net.mamoe.mirai.utils.BotConfiguration.MiraiProtocol] 中的值。
# 默认为ANDROID_PAD
# 可选：ANDROID_PHONE ANDROID_PAD
simbot.mirai.protocol=ANDROID_PAD

```

# 1.2.1-1.16
- `Reply`追加支持回复消息的时候使用引用回复，且默认为true。
- `Reply`追加支持回复消息的时候使用at，且默认为true。
- `Reply`上述两个选项只有在回复群消息的时候才会生效。


# 1.2.0-1.16
- 更新核心到`1.16.3`，使用原生方法支持好友删除事件: `@Listen(MsgGetTypes.friendDelete)`或`@OnFriendDelete`
- 调整各大缓存Map的检查机制，当一个缓存Map的`check`的值小于0的时候, 将不会进行清理检测。
- 简单修改/优化部分内部代码结构。
- 追加`Reply`类来实现快捷回复/请求响应。

以加群申请为例，在kotlin环境下，你可以这样来同意所有的请求：
```kotlin
    /**
     * 直接同意入群申请
     */
    @OnGroupAddRequest
    fun group() = Reply.agreeReply
```
在java环境下，你可以这样：
```java
    /**
     * 同意所有入群请求
     */
    @OnGroupAddRequest
    public Reply agree(){
        return Reply.getAgreeReply();
    }
```
关于`Reply`的所有内容，可以查看`com.simbot.component.mirai.messages.Reply.kt`中的内容。
注：Reply虽然实现了Map接口，但其本质为**不可变**Map。
`Reply`在未来计划会整合至核心中。


# 1.1.0-1.16
- 暂时以mirai层面来支持好友删除事件。 [#2](https://github.com/ForteScarlet/simple-robot-component-mirai/issues/2)
使用方法：
```java
@Listen.ByName(MiraiEvents.friendDeleteEvent)
public void listenFriendDelete(FriendDelete friendDelete){
    // do somthing...
}
```
核心的后续更新中可能会追加支持通用接口。

- setFriendAddRequest相关的事件中，friendName参数可以为null了（实际上此参数也属无用参数。）
- 优化内部部分代码，提高获取CodeNumber的效率(不再有字符串转化了)，移除无意义的lazy(event相关)

- 调整缓存类的使用方式，变更为注入形式，现在你可以通过修改配置文件来配置缓存类的缓存清理临界值和缓存时间了。
缓存的相关配置如下：
```properties
# suppress inspection "UnusedProperty" for whole file
# RecallCacheConfiguration
# 消息缓存中，清理缓存临界值, 当计数器达到指定值则触发一次清理。check值小于等于0的时候将不会触发清理。
simbot.mirai.cache.recall.check=1000
# 缓存时间
simbot.mirai.cache.recall.cacheTime=3600000
# 内部缓存的初始容量
simbot.mirai.cache.recall.initialCapacity=32
# 缓存的最大容量
simbot.mirai.cache.recall.max=102400

# RequestCacheConfiguration
# 以下是请求信息缓存相关的配置，好友请求和群请求共用以下两个配置。
simbot.mirai.cache.request.check=1000
simbot.mirai.cache.request.cacheTime=3600000
# 以下是请求信息缓存相关的配置，分为好友请求和群请求。
simbot.mirai.cache.request.friend.initialCapacity=32
simbot.mirai.cache.request.friend.max=102400
simbot.mirai.cache.request.join.initialCapacity=32
simbot.mirai.cache.request.join.max=102400

# ImageCacheConfiguration
# 以下是图片信息缓存相关的配置
simbot.mirai.cache.image.check=1000
simbot.mirai.cache.image.cacheTime=3600000
simbot.mirai.cache.image.initialCapacity=32
simbot.mirai.cache.image.max=102400

# ContactCacheConfiguration
# 以下是非好友联系人信息缓存相关的配置
simbot.mirai.cache.contact.check=1000
simbot.mirai.cache.contact.cacheTime=3600000
simbot.mirai.cache.contact.initialCapacity=32
simbot.mirai.cache.contact.max=102400
```

- 追加配置以选择送信器的送信方式：
```properties
# 送信器的类型，目前有两个类型，一个是BLOCK(阻塞), 一个是COROUTINE(协程)。
# 默认为BLOCK
# 注意，使用协程模式的时候你无法获取发送的消息的ID等一些相关的返回值信息。
simbot.mirai.senderType=COROUTINE
```


# 1.0.4-1.16
- 修复新好友添加事件无效的bug


# 1.0.3-1.16
- 单独更新`cqCodeUtils`模组到`1.6.1`版本以修复二次转义问题。
- 增加了一个Xml类和XmlCodeBuilder类来辅助构建一个参数较长的Xml类型的CQ码。
- xml类型的CQ码增加一些可解析的参数。文档内已经追加，详情看文档的：mirai组件 -> 注意事项 章节。


# 1.0.0-1.16
- 核心更新至1.16.0

# 0.6.3-1.15
- 修复`BotManager`使用`logOutBot(...)`的时候会导致无法正常登出的问题
- 更新核心至`1.15.2`
- mirai版本变更为其稳定版本`1.0.2`
- 优化image类型的CQ码的解析，现在可以正常解析`file:`开头的路径、相对路径的图片资源了。


# 0.6.2-1.15
- 对一些不支持的api增加warning日志提示。
注：此处的api指的是获取到的实例，例如通过GETTER得到的`GroupInfo`，而sender中直接调用的api如果不支持则其依旧会抛出异常。
- 优化登录设备配置，现在默认情况下，会根据登录的账号提供一个不变的随机设备号。并提供一个默认的设备信息类：MiraiSystemDeviceInfo，详细内容可查看此类。
- mirai更新到`1.1-EA`

# 0.6.1-1.15
更新核心到`v1.15.1`

# 0.6-1.15
更新核心到`v1.15.0`

# 0.5-1.14
更新GETTER的getStrangerInfo为获取好友信息。

更新核心至1.14.1


# 0.4-1.14
- 更新核心至1.14.0

# 0.4-1.13
    
- 增加quote（引用回复）的解析。使用方法即创建"quote"类型的CQ码放在消息开头即可。参数id为需要回复的消息的id。由于消息存在缓存，因此接收到消息30分钟内有效。例如：`\[CQ:quote,id={msgId}]`
- 优化对接收到的消息的CQ码转化，提供更多参数   
- 修复内部的缓存map存在的一些隐患
- 修复无法私聊非好友群成员的问题。
- 增加对全部群成员的缓存，以支持上述的私聊非好友群成员问题。
- 增加一个依赖以支持缓存map的LRU

# 0.3-1.13