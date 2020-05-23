# simple-robot mirai组件

基于 **`simple-robot-core`** 核心框架，对接 **`Mirai`** 的qqandroid库


## 特性
- 内置`v1.4-1.13`版本的`CQCodeUtils`模组来代替核心内部自带的CQCodeUtil工具.

- 接收到的绝大部分`CQ码`都是从`mirai码`转化而来。
而从`mirai码`转化而来的`CQ码`与原版CQ码有一定的区别，例如`at`类型的CQ码, 正常情况下为`[CQ:at,qq=12345678]`，而转化而来的则是`[CQ:at,at=12345678]`
此问题可能会逐步改善，但是还请注意情况，增加一层判断。

- 图片暂时没法复读？

- 为了兼容Mirai的特性与simbot，此组件内置了部分缓存类以缓存一些信息，例如请求的request和image等

## 支持内容
目前只测试了：
- 私信的复读（文本、表情、图片）
- 群聊被at情况下的复读（文本、表情、图片）
- 启动的时候主动发送消息（`after()`）
- 主动发送图片（以本地路径的cq码类型发送，例如`KQCodeUtils.INSTANCE.toCq("image", "file=C:\\Users\\Administrator\\Desktop\\123.jpg")`）
- 根据返回值的快捷回复：
    - Message消息相关中，返回值为`Map`类型的时候，会获取key = "reply"的作为快捷回复值。

- 撤回监听需要等下一次核心更新

## 注意事项
尽可能使用`KQCodeUtils`来代替`CQCodeUtil`。使Mirai兼容simbot已经损失了很多性能了，CQ码方面就换相对高效一点的工具吧。
Image对象可以使用`ImageCache`得到，默认情况下，内部会默认对你发送的和接收到的Image类进行缓存，时效30分钟，每次获取会刷新其时间。
与请求相关(request)的event实例（例如好友申请实例`NewFriendRequestEvent`）同样会被缓存，时效30分钟，获取不会刷新时间。


# 施工中...