/*
 *
 * Copyright (c) 2020. ForteScarlet All rights reserved.
 * Project  component-mirai
 * File     MiraiSimbotConfiguration.java
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

package com.simbot.component.mirai;

import com.forte.qqrobot.ConfigProperties;
import com.forte.qqrobot.anno.depend.Beans;
import com.forte.qqrobot.anno.depend.Depend;
import com.simbot.component.mirai.collections.*;

/**
 * just a empty class
 * 暂时没用什么可配置的
 * @author ForteScarlet
 */
@Beans
public class MiraiSimbotConfiguration {

    @Depend
    private ConfigProperties configProperties;

    @Depend
    private MiraiConfiguration configuration;

    @Beans
    public MiraiBots getMiraiBots(){
        return MiraiBots.INSTANCE;
    }


    @Beans
    public CacheMapConfigurationInjectable getCacheMapConfigurationInjectable(){
        return new CacheMapConfigurationInjectable();
    }

    /**
     * @see RecallCacheConfiguration
     * @see RecallCache
     */
    @Beans
    public RecallCacheConfiguration getRecallCacheConfiguration(CacheMapConfigurationInjectable injectable){
        RecallCacheConfiguration conf = new RecallCacheConfiguration();
        injectable.getRecallCacheConfigurationInjectableConfig().inject(conf, configProperties);
        return conf;
    }

    /**
     * @see RequestCacheConfiguration
     * @see RequestCache
     */
    @Beans
    public RequestCacheConfiguration getRequestCacheConfiguration(CacheMapConfigurationInjectable injectable){
        RequestCacheConfiguration conf = new RequestCacheConfiguration();
        injectable.getRequestCacheConfigurationInjectableConfig().inject(conf, configProperties);
        return conf;
    }

    /**
     * @see ImageCacheConfiguration
     * @see ImageCache
     */
    @Beans
    public ImageCacheConfiguration getImageCacheConfiguration(CacheMapConfigurationInjectable injectable){
        ImageCacheConfiguration conf = new ImageCacheConfiguration();
        injectable.getImageCacheConfigurationInjectableConfig().inject(conf, configProperties);
        return conf;
    }
    /**
     * @see VoiceCacheConfiguration
     * @see VoiceCache
     */
    @Beans
    public VoiceCacheConfiguration getVoiceCacheConfiguration(CacheMapConfigurationInjectable injectable){
        VoiceCacheConfiguration conf = new VoiceCacheConfiguration();
        injectable.getVoiceCacheConfigurationInjectableConfig().inject(conf, configProperties);
        return conf;
    }

    /**
     * @see ContactCacheConfiguration
     * @see ContactCache
     */
    @Beans
    public ContactCacheConfiguration getContactCacheConfiguration(CacheMapConfigurationInjectable injectable){
        ContactCacheConfiguration conf = new ContactCacheConfiguration();
        injectable.getContactCacheConfigurationInjectableConfig().inject(conf, configProperties);
        return conf;
    }


    @Beans
    public RecallCache getRecallCache(RecallCacheConfiguration conf){
        return new RecallCache(conf);
    }
    @Beans
    public RequestCache getRequestCache(RequestCacheConfiguration conf){
        return new RequestCache(conf);
    }
    @Beans
    public ImageCache getImageCache(ImageCacheConfiguration conf){
        return new ImageCache(conf);
    }
    @Beans
    public VoiceCache getVoiceCache(VoiceCacheConfiguration conf){
        return new VoiceCache(conf);
    }
    @Beans
    public ContactCache getContactCache(ContactCacheConfiguration conf){
        return new ContactCache(conf);
    }

    /**
     * 获取缓存map
     * @param recallCache   消息缓存map
     * @param requestCache  请求缓存map
     * @param imageCache    image缓存map
     * @param voiceCache    voice缓存map
     * @param contactCache  发送消息缓存map
     * @return
     */
    @Beans
    public CacheMaps getCacheMaps(RecallCache recallCache,
                                  RequestCache requestCache,
                                  ImageCache imageCache,
                                  VoiceCache voiceCache,
                                  ContactCache contactCache){
        return new CacheMaps(recallCache, requestCache, imageCache, voiceCache, contactCache);
    }


    @Beans
    public SenderRunner getSenderRunner(){
        return configuration.getSenderType().getRunnerGetter().invoke();
    }




}
