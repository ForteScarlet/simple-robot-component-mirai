/*
 * Copyright (c) 2020. ForteScarlet All rights reserved.
 * Project  component-mirai
 * File     Test.kt
 *
 * You can contact the author through the following channels:
 * github https://github.com/ForteScarlet
 * gitee  https://gitee.com/ForteScarlet
 * email  ForteScarlet@163.com
 * QQ     1149159218
 *
 *
 */

package com.test

import com.forte.qqrobot.BaseApplication
import com.forte.qqrobot.SimpleRobotApplication

@SimpleRobotApplication
class App

fun main() {
    BaseApplication.runAuto(App::class.java)
}