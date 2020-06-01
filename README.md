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
目前使用的mirai版本为：**`1.0-RC`**



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
    <!-- 参考:0.2-1.13 -->
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


# 施工中...