/*
 *
 *  * Copyright (c) 2020. ForteScarlet All rights reserved.
 *  * Project  component-mirai
 *  * File     FixBotInfoImpl.java
 *  *
 *  * You can contact the author through the following channels:
 *  * github https://github.com/ForteScarlet
 *  * gitee  https://gitee.com/ForteScarlet
 *  * email  ForteScarlet@163.com
 *  * QQ     1149159218
 *  *
 *  * The Mirai code is copyrighted by mamoe-mirai
 *  * you can see mirai at https://github.com/mamoe/mirai
 *  *
 *  *
 *
 */

package com.simbot.component.mirai;

import com.forte.qqrobot.bot.BotInfo;
import com.forte.qqrobot.bot.BotSender;
import com.forte.qqrobot.bot.LoginInfo;

import java.util.Objects;

/**
 * 一个已注册的机器人的信息
 * 一个简单的信息封装类，一般为登录后验证完全部的登录信息后。
 * 不会对path做任何处理
 *
 * @author <a href="https://github.com/ForteScarlet"> ForteScarlet </a>
 */
public class FixBotInfoImpl implements BotInfo {

    private String botCode;
    private String path;
    private LoginInfo info;
    private BotSender botSender;
    private boolean closed = false;

    /**
     * 构建一个bot信息对象
     *
     * @param botCode bot的Code信息
     * @param path    上报路径信息, 结尾最终会转化为不带斜杠
     * @param info    bot的详细信息
     */
    public FixBotInfoImpl(String botCode, String path, LoginInfo info, BotSender botSender) {
        this.botCode = botCode;
        this.path = path;
        this.info = info;
        this.botSender = botSender;
    }

    @Override
    public String getBotCode() {
        return botCode;
    }

    public void setBotCode(String botCode) {
        this.botCode = botCode;
    }

    @Override
    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public LoginInfo getInfo() {
        return info;
    }

    /**
     * 获取当前bot所对应的送信器
     *
     * @return 当前账号送信器
     */
    @Override
    public BotSender getSender() {
        return botSender;
    }

    public void setSender(BotSender sender){
        this.botSender = sender;
    }

    public void setInfo(LoginInfo info) {
        this.info = info;
    }

    /**
     * 默认情况下，当code与请求路径相同的时候，即认为这两个bot相同。
     * @param o other
     * @return equals
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        FixBotInfoImpl botInfo = (FixBotInfoImpl) o;
        return Objects.equals(botCode, botInfo.botCode) &&
                Objects.equals(path, botInfo.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(botCode, path);
    }

    @Override
    public String toString() {
        return "botInfo(code=" + botCode + ", path=" + path + ", info=" + info + ")";
    }

    /**
     * nothing
     */
    @Override
    public void close() {
        closed = true;
    }
}
