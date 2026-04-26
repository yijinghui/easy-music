package com.easy.utils;


import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.easy.constant.RedisConstant;
import com.easy.pojo.dto.RedisData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;


@Slf4j
@Component
public class CacheClient {

    private final StringRedisTemplate stringRedisTemplate;

    // 线程池
    private static final ExecutorService CACHE_REBUILD_EXECUTOR = Executors.newFixedThreadPool(10);


    public CacheClient(StringRedisTemplate stringRedisTemplate){
        this.stringRedisTemplate=stringRedisTemplate;
    }

    public void set(String key, Object value, Long time, TimeUnit unit){
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(value),time,unit);
    }

    public void setWithLogicalExpire(String key, Object value, Long time, TimeUnit unit){
        // 设置逻辑过期
        RedisData redisData=new RedisData();
        redisData.setData(value);
        redisData.setExpireTime(LocalDateTime.now().plusSeconds(unit.toSeconds(time)));

        // 写入redis
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(redisData),time,unit);
    }

    public <R,ID> R queryWithPassThrough(String keyPrefix, ID id, Class<R> type, Function<ID,R> dbFallback, Long time, TimeUnit unit){
        // Function<ID,R> dbFallback:函数式编程，由调用者提供函数逻辑，指定参数类型和返回值类型

        String key = keyPrefix + id;

        // 1.从redis中查询商铺缓存
        String json = stringRedisTemplate.opsForValue().get(key);

        if (StrUtil.isNotBlank(json)) {
            return JSONUtil.toBean(json, type);
        }

        // 2.判断命中是否为空值
        if (json != null) {
            return null;
        }

        // 3.若redis中未缓存则查询数据库
        R r= dbFallback.apply(id);

        if (r==null){
            // 将空值写入Redis中防止缓存穿透导致数据库服务器崩溃
            stringRedisTemplate.opsForValue().set(key, "", time,unit);
            return null;
        }

        this.set(key,r,time,unit);

        return r;
    }

    public <R,ID> R queryWithLogicalExpire(String keyPrefix, ID id, Class<R> type, Function<ID,R> dbFallback, Long time, TimeUnit unit){
        // 逻辑过期需要缓存预热，即系统启动时、活动开始时前，主动加载热点数据到redis中
        String key = keyPrefix + id;

        // 1.从redis中查询商铺缓存
        String json = stringRedisTemplate.opsForValue().get(key);

        // 2.未命中直接返回
        if (StrUtil.isBlank(json)) {
            return null;
        }

        // 3.命中，需要先吧json反序列化为对象
        RedisData redisData=JSONUtil.toBean(json,RedisData.class);
        JSONObject data = (JSONObject) redisData.getData();
        R r=JSONUtil.toBean(data,type);
        LocalDateTime expireTime=redisData.getExpireTime();

        // 4.判断是否过期
        if (expireTime.isAfter(LocalDateTime.now())){
            return r; // 未过期则直接返回店铺信息
        }

        // 5.已过期，则需要缓存重建
        String lockKey= RedisConstant.LOCK_SHOP_KEY+id;
        // 5.1 获取互斥锁
        boolean isLock=tryLock(lockKey);
        if (isLock){
            CACHE_REBUILD_EXECUTOR.submit(()->{
                try {
                    R r1= dbFallback.apply(id);
                    this.setWithLogicalExpire(key,r1,time,unit);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                } finally {
                    // 释放锁
                    unlock(lockKey);
                }
            });
        }

        return r;

    }

    public <R,ID> R queryWithMutex(String keyPrefix, ID id, Class<R> type, Function<ID,R> dbFallback, Long time, TimeUnit unit){

        String key = keyPrefix + id;
        String lockKey = RedisConstant.LOCK_SHOP_KEY + id;
        // 1.从redis中查询商铺缓存
        String json = stringRedisTemplate.opsForValue().get(key);

        if (StrUtil.isNotBlank(json)) {
            return JSONUtil.toBean(json, type);
        }

        // 3.若redis中未缓存则查询数据库（此处若接口访问量极高就容易造成缓存击穿）
        // 3.1 获取互斥锁
        R r1 = null;
        try {
            boolean isLock = tryLock(lockKey);
            if (!isLock) {
                Thread.sleep(50); // 线程等待
                queryWithMutex(keyPrefix, id, type, dbFallback, time, unit);
            }

            r1 = dbFallback.apply(id);
            if (r1 == null) {
                return null;
            }
            stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(r1), time, unit);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            // 释放互斥锁
            unlock(lockKey);
        }
        return r1;
    }

    // 获取互斥锁
    private boolean tryLock(String key) {
        Boolean flag = stringRedisTemplate.opsForValue().setIfAbsent(key, "1", 10, TimeUnit.SECONDS);
        return BooleanUtil.isTrue(flag); //直接拆箱可能会导致空指针异常
    }

    // 释放锁
    private void unlock(String key) {
        stringRedisTemplate.delete(key);
    }



}
