# simple-robot mirai组件

[![](https://img.shields.io/badge/simple--robot-core-green)](https://github.com/ForteScarlet/simple-robot-core) [![](https://img.shields.io/maven-central/v/love.forte.simple-robot-component/component-mirai)](https://search.maven.org/artifact/love.forte.simple-robot-component/component-mirai)

基于 **`simple-robot-core`** 核心框架，对接 **`Mirai`** 的qqandroid库


## 特性

- 内置`v1.4.1-1.13`版本的`CQCodeUtils`模组来代替核心内部自带的CQCodeUtil工具.

- 接收到的绝大部分`CQ码`都是从`mirai码`转化而来。
而从`mirai码`转化而来的`CQ码`与原版CQ码有一定的区别，例如`at`类型的CQ码, 正常情况下为`[CQ:at,qq=12345678]`，而转化而来的则是`[CQ:at,at=12345678]`
此问题可能会逐步改善，但是还请注意情况，增加一层判断。

- 为了兼容Mirai的特性与simbot的特性，此组件内置了部分缓存类以缓存一些信息，例如请求的request和image等

## Mirai版本
目前使用的mirai版本为：**`1.1-EA`**



## 支持内容
目前测试了：
- 私信的复读（文本、表情、图片）
- 群聊被at情况下的复读（文本、表情、图片）
- 启动的时候主动发送消息（`after()`）
- 主动发送图片（以本地路径的cq码类型发送，例如`KQCodeUtils.INSTANCE.toCq("image", "file=C:\\Users\\Administrator\\Desktop\\123.jpg")` 即 `[CQ:image,file=C:\\Users\\Administrator\\Desktop\\123.jpg]`）
- 根据返回值的快捷回复：
    - Message消息相关(私信、群聊事件)中，返回值为`Map`类型的时候，会获取key = "reply"的作为快捷回复值。
    
    
- 多账号登录
- 群消息撤回监听（mirai没有提供用户撤回的消息的具体msg内容的获取方法，因此撤回消息的`getMsg()`方法所得到的值会通过simbot组件进行缓存，如果监听到的撤回消息的缓存已过期或者其发送是在程序未启动的时候发送的，则只能获取到null。）
- 私聊撤回监听暂不支持

## 注意事项
- 尽可能使用`KQCodeUtils`来代替`CQCodeUtil`。使Mirai兼容simbot已经损失了很多性能了，CQ码方面就换相对高效一点的工具吧。
- Image对象可以使用`ImageCache`得到，默认情况下，内部会默认对你发送的和接收到的Image类进行缓存，时效30分钟，每次获取会刷新其时间。
- 与请求相关(request)的event实例（例如好友申请实例`NewFriendRequestEvent`）同样会被缓存，时效30分钟，获取不会刷新时间。
- 配置文件中的`core.bots`格式不变，但是code不再可以省略了。格式为：`${账号1}:${密码1},${账号2}:${密码2},.....`，如果密码中存在：`\`或者`,`，则使用`\\`和`\,`进行转义。

- 下载Mirai相关库的时候可以添加额外的仓库地址：
```xml
<repositories>
        <repository>
            <id>jcenter</id>
            <url>https://jcenter.bintray.com/</url>
        </repository>
        <repository>
            <id>bintray</id>
            <url>https://dl.bintray.com/kotlin/kotlin-eap</url>
        </repository>
</repositories>
```

<br>

**0.x的版本都是未完善的，使用的时候多做些处理，并及时反馈bug**

<br>

## 使用
以maven为例：
```xml
<dependency>
    <groupId>love.forte.simple-robot-component</groupId>
    <artifactId>component-mirai</artifactId>
    <!-- 参考:0.4-1.13 -->
    <version>${version}</version>
</dependency>
```

启动器：

```java
@SimpleRobotApplication(resources = "/conf.properties")
public class TestMain2 /* implements MiraiApp // 可以选择实现此接口 */ {
    public static void main(String[] args) throws Exception {
        // mirai application
        new MiraiApplication().run(TestMain2.class, args);
    }
}
```

或者试试核心1.13.x的通用启动器：
```java
@SimpleRobotApplication(resources = "/conf.properties")
public class TestMain2 /* implements Application // 可以选择实现此接口 */ {
    public static void main(String[] args) throws Exception {
        // base application
        BaseApplication.runAuto(TestMain2.class, args);
    }
}
```


## 可解析的CQ码
此处简述已经支持解析为mirai`Message`的CQ码类型列表.
参数展示格式为：`\[参数1(1)|参数1(2)](参数1含义)\[参数2](参数2含义)`
每个中括号(`\[]`)中代表一种参数，中括号后面的括号中为对此参数的描述。
参数名可能存在一些重名参数，例如at类型中，参数qq与参数at含义相同（例如`\[CQ:at,qq=123456789]`和`\[CQ:at,at=123456789]`）
则其中，中括号中第一个出现的参数即为推荐参数，例如at中，推荐使用`qq`作为参数名，其次才是`at`


- **at**

    参数：\[`qq`|`at`](at的人的QQ号)
    
    
- **face**
    
    参数：\[`face`|`id`](face表情的数字ID)
    
- **image**    

    参数：\[`file`|`image`](图片的ID或本地文件或网络文件。)\[`destruct`](是否为闪照，参数为true即为闪照，可忽略)
    
    注①：file参数中的值，如果为`http`开头则会认定为网络图片。
    注②：file参数中的值，图片ID一般为接收到的图片的ID，接收到的图片ID存在缓存期，默认缓存30分钟，每次获取此图片则会刷新此时间。也就是说当你30分钟内不再发送此图片，则之后也无法发送了。
    注③：file参数中的值，在排除了网络图片后，会优先判断缓存，如果缓存中不存在才会去寻找本地文件，并根据本地文件路径将此图片计入缓存。
    
    
    
- **record**

    参数：无
    
    注①：暂不支持发送语音，因此语音会变为`\[语音]`字样的文本消息    
    

- **rps**    

    参数：无
        
    注①：暂不支持发送猜拳，因此语音会变为`\[猜拳]`字样的文本消息    
        
    
- **dice**    

     参数：无
            
     注①：暂不支持发送骰子，因此语音会变为`\[骰子]`字样的文本消息    


- **shake**

    参数：\[`type`](互动表情的数字类型，如果此参数忽略则会直接使用戳一戳)\[`id`](类型下的数字ID。默认-1)
    
    参考(摘自Mirai源码：`HummerMessage.kt`)：     
    ```kotlin
               /** 戳一戳 */
               @JvmField
               val Poke = PokeMessage("戳一戳", 1, -1)
       
               /** 比心 */
               @JvmField
               val ShowLove = PokeMessage("比心", 2, -1)
       
               /** 点赞  */
               @JvmField
               val Like = PokeMessage("点赞", 3, -1)
       
               /** 心碎 */
               @JvmField
               val Heartbroken = PokeMessage("心碎", 4, -1)
       
               /** 666 */
               @JvmField
               val SixSixSix = PokeMessage("666", 5, -1)
       
               /** 放大招 */
               @JvmField
               val FangDaZhao = PokeMessage("放大招", 6, -1)
       
               /** 宝贝球 (SVIP) */
               @JvmField
               val BaoBeiQiu = PokeMessage("宝贝球", 126, 2011)
       
               /** 玫瑰花 (SVIP) */
               @JvmField
               val Rose = PokeMessage("玫瑰花", 126, 2007)
       
               /** 召唤术 (SVIP) */
               @JvmField
               val ZhaoHuanShu = PokeMessage("召唤术", 126, 2006)
       
               /** 让你皮 (SVIP) */
               @JvmField
               val RangNiPi = PokeMessage("让你皮", 126, 2009)
       
               /** 结印 (SVIP) */
               @JvmField
               val JieYin = PokeMessage("结印", 126, 2005)
       
               /** 手雷 (SVIP) */
               @JvmField
               val ShouLei = PokeMessage("手雷", 126, 2004)
       
               /** 勾引 */
               @JvmField
               val GouYin = PokeMessage("勾引", 126, 2003)
       
               /** 抓一下 (SVIP) */
               @JvmField
               val ZhuaYiXia = PokeMessage("抓一下", 126, 2001)
       
               /** 碎屏 (SVIP) */
               @JvmField
               val SuiPing = PokeMessage("碎屏", 126, 2002)
       
               /** 敲门 (SVIP) */
               @JvmField
               val QiaoMen = PokeMessage("敲门", 126, 2002)
     ```
               

- **anonymous**

    参数：无
    
    注①：匿名消息暂时无效


- **music**

    参数：\[`type`](音乐类型)
    
    注①：mirai中没有直接进行的music分享解析，可参考下文的XML类型。
    注②：后续可能会追加预设解析。
    注③：目前阶段music类型CQ码会被解析为文本消息：`[$type音乐]`
    
    
- **emoji**    

    参数：\[`id`](emoji的id)
    
    注①：emoji直接发送就行，用不着cq码，所以这个类型的CQ码会被解析为文本消息：`emoji($id)`
    

- **location**

    参数：\[`lat`](纬度)\[`lon`](经度)\[`title`](分享地点的名称)\[`content`](分享地点的具体地址)

    注①：会被直接解析为文本消息：`位置($lat,$lon)[$title]:$content`
    

- **sign**

    参数：无
    
    注①：不支持解析签到，会被直接解析为文本消息：`\[签到]`
    

- **show**

    参数：无
    
    注①：会被直接解析为空消息，即忽略


- **contact**

    参数：\[`id`](联系人分享类型)
    
    注①：暂不支持此类型解析，可考虑参考XML消息。会被解析为文本消息：`$typeName: $id`

    
- **xml**    
    
    参数：\[`action`](一般为点击这条消息后跳转的链接)\[`actionData`]()\[`brief`](摘要, 在官方客户端内消息列表中显示)\[`flag`]()\[`url`](//TODO: 2019/12/3 unknown)\[`sourceName`](sourceName 好像是名称)\[`sourceIconURL`](sourceIconURL 好像是图标)
    
    
    注①：以上参数都是mirai定义的，有注释的我都写上了
    
    
- **app** 与 **json**

    参数：\[`content`](json内容，默认为`{}`)    
    
    注①：因为说json大部分都是小程序类型，因此app与json合并解析，使用的都是`LightApp`类。
    
   
- **quote** 

    参数：\[`id`|`quote`](引用回复的消息ID)
    
    注①：使用方法即创建"quote"类型的CQ码放在消息开头即可。参数id为需要回复的消息的id。由于消息存在缓存，因此接收到消息30分钟内有效。例如：`\[CQ:quote,id={msgId}]`
   
   
- **其他**
    
    其他未提及的直接做toString处理。但是你可以追加一些额外的解析规则。
    你可以通过`CQCodeParsingHandler.registerHandler(type, function)`来注册一个除上述解析以外的任意额外解析。
    ※ 此类仅存在于Mirai组件中。    
    
    
    
    
    