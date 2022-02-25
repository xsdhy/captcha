package com.xsdhy.captcha.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class RedisUtils {


    /**
     * 写入缓存设置时效时间
     *
     * @param key
     * @param value
     * @return
     */
    public boolean set(final String key, Object value, Long expireTime, TimeUnit timeUnit) {
        return true;
    }






    /**
     * 读取缓存
     *
     * @param key
     * @return
     */
    public Object get(final String key) {
        return null;
    }


}