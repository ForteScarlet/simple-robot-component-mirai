/*
 *
 * Copyright (c) 2020. ForteScarlet All rights reserved.
 * Project  component-mirai
 * File     XmlTemplate.kt
 *
 * You can contact the author through the following channels:
 *  github https://github.com/ForteScarlet
 *  gitee  https://gitee.com/ForteScarlet
 *  email  ForteScarlet@163.com
 *  QQ     1149159218
 *  The Mirai code is copyrighted by mamoe-mirai
 *  you can see mirai at https://github.com/mamoe/mirai
 *
 *
 */

package com.simbot.component.mirai

import com.simplerobot.modules.utils.KQCode
//
// /**
//  * Xml [KQCode]实例
//  * 通过Builder构建
//  */
// class Xml internal constructor(override val params: Map<String, String>): KQCode {
//     companion object Builder {
//         /**
//          * 通过kotlin 的 dsl 构建一个[Xml]
//          */
//         @JvmStatic
//         fun buildDSL(builder: XmlCodeBuilder.() -> Unit): Xml = XmlCodeBuilder().also(builder).build()
//
//         /**
//          * 获取一个[XmlCodeBuilder]实例
//          */
//         @JvmStatic
//         val builder: XmlCodeBuilder get() = XmlCodeBuilder()
//     }
// }
//
// class XmlCodeBuilder {
//
//     private val params: MutableMap<String, String> = mutableMapOf()
//
//     var action: String?
//     get() = params["action"]
//     set(value) {
//         value?.also { params["action"] = it }
//     }
//
//     var actionData: String?
//     get() = params["actionData"]
//     set(value) {
//         value?.also { params["actionData"] = it }
//     }
//
//     var brief: String?
//     get() = params["brief"]
//     set(value) {
//         value?.also { params["brief"] = it }
//     }
//
//     var flag: String?
//     get() = params["flag"]
//     set(value) {
//         value?.also { params["flag"] = it }
//     }
//
//     var url: String?
//     get() = params["url"]
//     set(value) {
//         value?.also { params["url"] = it }
//     }
//
//     var sourceName: String?
//     get() = params["sourceName"]
//     set(value) {
//         value?.also { params["sourceName"] = it }
//     }
//
//     var sourceIconURL: String?
//     get() = params["sourceIconURL"]
//     set(value) {
//         value?.also { params["sourceIconURL"] = it }
//     }
//
//     var bg: String?
//     get() = params["bg"]
//     set(value) {
//         value?.also { params["bg"] = it }
//     }
//
//     var layout: String?
//     get() = params["layout"]
//     set(value) {
//         value?.also { params["layout"] = it }
//     }
//
//     var pictureCoverUrl: String?
//     get() = params["picture_coverUrl"]
//     set(value) {
//         value?.also { params["picture_coverUrl"] = it }
//     }
//
//     var summaryText: String?
//     get() = params["summary_text"]
//     set(value) {
//         value?.also { params["summary_text"] = it }
//     }
//
//     var summaryColor: String?
//     get() = params["summary_color"]
//     set(value) {
//         value?.also { params["summary_color"] = it }
//     }
//
//     var titleText: String?
//     get() = params["title_text"]
//     set(value) {
//         value?.also { params["title_text"] = it }
//     }
//
//     var titleSize: String?
//     get() = params["title_size"]
//     set(value) {
//         value?.also { params["title_size"] = it }
//     }
//
//     var titleColor: String?
//     get() = params["title_color"]
//     set(value) {
//         value?.also { params["title_color"] = it }
//     }
//
//
//     /**
//      * build
//      */
//     fun build(): Xml = Xml(params)
// }