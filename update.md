# 1.8.2-1.16
### BETA.2
- 更新`cqcodeutils`模组到`1.8.0-BETA.2`并进行兼容
- 移除类`Xml`与`XmlCodeBuilder`

### BETA.3
- message缓存时的ID不再包含`time`
- `GroupInfo.getAdminList`中现在包含了群主的账号信息.

### BETA.4
- `getter`的返回值中，有部分通过懒加载获取的实例，不再有双重锁了。
- 部分封装类中，针对于`mirai`原生数据的获取不再进行字段储存了。

### BETA.5
- 尝试调整对消息的CQ码转化的方式
- 当发送的消息最终为`EmptyMessageChain`的时候，将不会实际发送消息，而是返回一个`null`
- 微调发送私聊、群聊内部逻辑
- 不再传递`mirai`与`kotlin`的`serialization`依赖


### BETA.6?
 



# 1.8.1-1.16
- 优化处理进程收到终止指令时的处理流程
- 修复闪照不会被处理为`image`CQ码的问题



# 1.8.0-1.16
- 简单调整内部事件与文件结构

- 新增`好友昵称变更`事件: `@Listen.ByName(MiraiEvents.friendNicknameChangedEvent)`
对应封装接口: `FriendNicknameChanged`

- 新增`好友输入状态变更`事件: `@Listen.ByName(MiraiEvents.friendInputStatusChangedEvent)`
对应封装接口: `FriendInputStatusChanged`

- 新增`群名称变更`事件: `@Listen.ByName(MiraiEvents.groupNameChangedEvent)`
对应封装接口: `GroupNameChanged`

- 新增`群员备注变更`事件: `@Listen.ByName(MiraiEvents.memberRemarkChangedEvent)`
对应封装接口: `MemberRemarkChanged`

- 新增`群成员头衔变更`事件: `@Listen.ByName(MiraiEvents.memberSpecialTitleChangedEvent)`
对应封装接口: `MemberSpecialTitleChanged`

- 修复`AtDetection`.`test`只有在消息最前列at bot的时候才生效的问题. 此bug同样适用于`@Filter(at=true)`

- 优化网络图片、语音的发送机制，现在从网络上传(多个)图片/语音的时候效率会更高。

- 优化网络图片、语音的发送机制，现在使用`ktor`的`HttpClient`库进行请求，不会出现因响应值为`302`重定向而导致无法发送图片的问题

- 调整`BotOffline`事件，追加事件类型并更细致的进行判断。

- 调整`simbot.mirai.autoRelogin`, 其只会在Bot因为**网络原因(`Dropped`)**掉线的时候重启, 并通过debug输出日志。

# 1.7.0-1.16
- 处理**群成员增加**事件由于mirai`v1.3.0`版本前的bug导致无法正常触发的问题。
- **群成员减少**事件现在会收到当bot被踢出、退群的事件了。可判断离群者账号是否为bot自身来区分。
- 为`voice/record`类型CQ码和`image`类型CQ码增加一个参数`cache`。当cache参数存在且`=false`的时候，此图片不会被缓存。（仅限于上传图片的时候）

- 变更特殊码转化机制，现在在进行图片、语音上传等可挂起函数的时候，会通过协程异步上传，
（大概）提高了发送（多个）本地/网络的图片/语音时候的效率。

- 配置`simbot.mirai.senderType`增加一个可选项`ASYNC`, 其代表异步进行消息发送，并等待消息发送成功后得到其返回值。
通过此类型可以实现消息异步发送，并同步得到送信返回值。
测试时，此选项在单核低配且运行了4个bot的机器上表现并不好。

**※ 此版本内部的送信机制变动较大，如果存在任何bug请及时上报。**



# 1.6.1-1.16
- **群员减少事件** 现在会收到**bot**离群时候的事件了。
- 为了兼容mirai`v1.3.0`版本之前的`bot入群`事件bug, 调整了`bot入群`事件的处理机制。
- cq码解析的时候追加一条`cache`参数, 当`cache`为false的时候则不会将当前发送的图片(本地、网络)进行缓存

# 1.6.0-1.16
- 调整内部缓存Map的文件结构
- 简单调整缓存Map的过期值清理规则。当check值为0的时候，每次检测都会清理，当check小于0的时候，永远不会因为计数检测而被清理。
- 群成员增加事件现在会被`bot入群`事件所触发。与普通成员进群事件的区别即为入群者的账号是否与bot一致。对应mirai的`BotJoinGroupEvent`事件。
- 追加支持mirai`v1.2.x`中的语音功能与`record`类型CQ码的解析。（目前仅支持群消息的发送与转发。具体限制请查看[https://github.com/mamoe/mirai/releases/tag/1.2.0](https://github.com/mamoe/mirai/releases/tag/1.2.0)）


追加以下配置：
```properties
# VoiceCacheConfiguration
# 以下是语音信息缓存相关的配置
simbot.mirai.cache.voice.check=1000
simbot.mirai.cache.voice.cacheTime=3600000
simbot.mirai.cache.voice.initialCapacity=32
simbot.mirai.cache.voice.max=102400
```

# 1.5.1-1.16
- 移除`getAuthInfo`得到的cookies字符串中最后的分号 `; `
- `mirai`更新至`1.2.2`
- 现在获取登录信息`LoginInfo`可以得到登录号的等级信息了。
- 追加配置项:
```properties
# mirai的缓存策略。可以是FILE或者MEMORY，分别代表文件缓存或内存缓存. 默认为FILE
simbot.mirai.cacheType=FILE
# 如果cacheType是FILE，则此处代表缓存文件夹路径。如果没有则默认为系统临时文件夹
simbot.mirai.cacheDirectory=
```
注：经测试，`1.2.2`版本的mirai切换缓存策略似乎不能在指定路径出现缓存文件。暂时不知道是mirai的bug还是我个人原因。
因此这两项配置项依旧可以使用，但是不保证有效果。


- 修改`MiraiConfiguration`的`botConfiguration: (String) -> BotConfiguration`为私有，并提供一个替代方案`setPostBotConfigurationProcessor: (String, BotConfiguration) -> BotConfiguration`.
java中可以：
```java
setPostBotConfigurationProcessor(code, conf -> {
     // do some for conf
     return conf; // or return your own conf instance.
})
```

# 1.5.0-1.16
- 支持API`getAuthInfo`
- 简单优化msg中接收到的`at`类型的CQ码
- 不支持解析的CQ码现在将不会输出信息，例如`[CQ:sign]`

# 1.4.5-1.16
- 修复因为自己手残少删了些代码导致无法发送私聊消息的问题

# 1.4.4-1.16
- 修复logger覆盖功能会导致登录的时候堆栈溢出的问题。

# 1.4.3-1.16
- 追加事件：`MiraiEvents.botOfflineEvent`(bot离线事件)、`MiraiEvents.botReloginEvent`(bot重新登录事件)
- 现在当接收到消息的时候，simbot-mirai组件会在触发**消息监听**前通过id缓存消息中的image。
- 优化sendPrivateMsg对于群临时消息的处理
- 追加配置项：
```properties
# 是否在bot掉线的时候自动重启，默认为false
simbot.mirai.autoRelogin=false
```


# 1.4.2-1.16
- 修改日志输出规则，在bot登录后、注册监听前的这段时间中暂停当前已登录bot的mirai的日志输出，以保证所有的bot的登录与验证行为不会受到mirai的日志干扰。



# 1.4.1-1.16
- 修复全体禁言无效的问题。
- `setGroupAnonymousBan`将会直接重载为`setGroupBan`, 并且提供一个警告.
- 增加`setGroupLeave`的警告日志
- 追加一个配置值:
```properties
# mirai配置自定义deviceInfo的时候使用的随机种子。默认为1.
simbot.mirai.deviceInfoSeed=1
```
- `GroupBan`事件中增加可能出现**全体禁言**事件。
    其中，全体禁言事件与普通禁言的区别为：全体禁言事件的 **被操作者(`beOperatedQQ`)** 必定为null, **禁言时长(`time`)** 必定为`-1`.



# 1.4.0-1.16
- mirai更新到`1.2.1`
- kotlin更新到`1.4.0`
- kotlinx-coroutines-core更新到`1.3.9`
- 处理多Bot登录时出现消息重复的问题

# 1.3.2-1.16
- 修复当账号的密码结尾为'/'符号的时候会导致密码被截掉的问题

# 1.3.1-1.16

- 追加几项可配置的配置信息：
```properties


# 关闭mirai bot logger
simbot.mirai.noBotLog=false

# 关闭mirai网络日志
simbot.mirai.noNetworkLog=false

# mirai bot log切换使用simbot的log
simbot.mirai.useSimbotBotLog=false

# mirai 网络log 切换使用simbot的log
simbot.mirai.useSimbotNetworkLog=false
```

# 1.3.0-1.16
- 追加实现mirai的好友头像更换事件。
```java
    @Listen.ByName(MiraiEvents.friendAvatarChangedEvent)
    public void event1(FriendAvatarChanged avatarChanged) {
        // ... do
    }

    // 或

    @OnFriendAvatarChanged
    public void event2(FriendAvatarChanged avatarChanged) { 
        // ... do
    }
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