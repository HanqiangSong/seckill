package com.countdown.seckill.services;

import com.countdown.seckill.util.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SeckillActivityService {

    @Autowired
    RedisService redisService;

    /**
     * 判断商品是否还有库存
     * @param activityId
     */
    public boolean seckillStockValidator(long activityId) {
        String key = "stock:" + activityId;
        return redisService.stockDeductValidator(key);
    }
}
