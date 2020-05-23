# simple-robot mirai组件

基于 **`simple-robot-core`** 核心框架，对接 **`Mirai`** 的qqandroid库


## 特性
- 内置`v1.4-1.13`版本的`CQCodeUtils`模组来代替核心内部自带的CQCodeUtil工具.

- 接收到的绝大部分`CQ码`都是从`mirai码`转化而来。
而从`mirai码`转化而来的`CQ码`与原版CQ码有一定的区别，例如`at`类型的CQ码, 正常情况下为`[CQ:at,qq=12345678]`，而转化而来的则是`[CQ:at,at=12345678]`
此问题可能会逐步改善，但是还请注意情况，增加一层判断。

- 图片暂时没法复读？

- 为了兼容Mirai的特性与simbot，此组件内置了部分缓存类以缓存一些信息，例如请求的request和image等

## 




# 施工中...