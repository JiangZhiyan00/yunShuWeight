package com.hhy.yunshu.utils;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.support.atomic.RedisAtomicLong;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Objects;

/**
 * redis自增流水工具类
 */
@Component
public class AutoIncrementNoUtils {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 获取redis自增流水号
     * @param key redis key
     * @param length 流水号长度(自行估计)
     * @param expireAt 过期于
     * @return 自增流水
     */
    public String getAutoIncrementNo(String key, String prefix, int length, Date expireAt) {
        expireAt = expireAt == null ? DateUtil.nextMonth() : expireAt;
        RedisAtomicLong noCounter = new RedisAtomicLong(key, Objects.requireNonNull(redisTemplate.getConnectionFactory()));
        // 下个月此刻过期
        noCounter.expireAt(expireAt);
        long increment = noCounter.getAndIncrement();
        //跳过初始值0,使初始值为1
        if (increment == 0L){
            increment = noCounter.getAndIncrement();
        }
        String no = StrUtil.padPre(String.valueOf(increment), length, '0');
        return StrUtil.isBlank(prefix) ? no : prefix + no;
    }
}
