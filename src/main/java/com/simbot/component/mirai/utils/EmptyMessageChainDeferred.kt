///*
// *
// *  * Copyright (c) 2020. ForteScarlet All rights reserved.
// *  * Project  component-mirai
// *  * File     EmptyMessageChainDeferred2.kt
// *  *
// *  * You can contact the author through the following channels:
// *  * github https://github.com/ForteScarlet
// *  * gitee  https://gitee.com/ForteScarlet
// *  * email  ForteScarlet@163.com
// *  * QQ     1149159218
// *  *
// *  * The Mirai code is copyrighted by mamoe-mirai
// *  * you can see mirai at https://github.com/mamoe/mirai
// *  *
// *  *
// *
// */
//
//package com.simbot.component.mirai.utils
//
//import kotlinx.coroutines.*
//import net.mamoe.mirai.message.data.EmptyMessageChain
//import kotlin.coroutines.CoroutineContext
//
//object EmptyMessageChainDeferred: Deferred<EmptyMessageChain> by CompletableDeferred(null){
//    /**
//     * [CoroutineContext.Key] for [EmptyMessageChainDeferred]
//     */
//    object EmptyMessageChainDeferredKey: CoroutineContext.Key<EmptyMessageChainDeferred>
//
//    /**
//     * [emptySequence]
//     */
//    override val children: Sequence<Job> = emptySequence()
//
//    /**
//     * 总是false。因为他其中没有任何任务。
//     */
//    override val isActive: Boolean = true
//
//    /**
//     * 他并不会因错误或被关闭
//     */
//    override val isCancelled: Boolean = false
//
//    /**
//     * 他总是完成的
//     */
//    override val isCompleted: Boolean = true
//
//    /**
//     * A key of this coroutine context element.
//     */
//    override val key: CoroutineContext.Key<*> get() = EmptyMessageChainDeferredKey
//
//    /**
//     * 直接返回[EmptyMessageChain]
//     */
//    override suspend fun await(): EmptyMessageChain = EmptyMessageChain
//
//    /**
//     * nothing.
//     */
//    override fun cancel(cause: CancellationException?) {}
//
//    /**
//     * nothing to join
//     */
//    override suspend fun join() {}
//
//    /**
//     * Starts coroutine related to this job (if any) if it was not started yet.
//     * The result `true` if this invocation actually started coroutine or `false`
//     * if it was already started or completed.
//     */
//    override fun start(): Boolean = false
//
//}