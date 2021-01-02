package com.countdown.seckill.services;

import com.alibaba.fastjson.JSON;
import com.countdown.seckill.db.dao.OrderDao;
import com.countdown.seckill.db.dao.SeckillActivityDao;
import com.countdown.seckill.db.po.Order;
import com.countdown.seckill.db.po.SeckillActivity;
import com.countdown.seckill.mq.RocketMQService;
import com.countdown.seckill.util.RedisService;
import com.countdown.seckill.util.SnowFlake;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Slf4j
@Service
public class SeckillActivityService {
    @Autowired
    RedisService redisService;
    @Autowired
    private SeckillActivityDao seckillActivityDao;
    @Autowired
    private RocketMQService rocketMQService;
    /**
     * datacenterId; 数据中心
     * machineId; 机器标识
     * 在分布式环境中可以从机器配置上读取 * 单机开发环境中先写死
     */
    private SnowFlake snowFlake = new SnowFlake(1, 1);

    /**
     * 判断商品是否还有库存
     * @param activityId
     */
    public boolean seckillStockValidator(long activityId) {
        String key = "stock:" + activityId;
        return redisService.stockDeductValidator(key);
    }

    /**
     * 创建订单
     * @param seckillActivityId
     * @param userId
     * @return
     * @throws Exception
     */
    public Order createOrder(long seckillActivityId, long userId) throws Exception {
        /*
         * 1.创建订单
         */
        SeckillActivity seckillActivity =
                seckillActivityDao.querySeckillActivityById(seckillActivityId);
        Order order = new Order();
        //采用雪花算法生成订单ID
        order.setOrderNo(String.valueOf(snowFlake.nextId()));
        order.setSeckillActivityId(seckillActivity.getId());
        order.setUserId(userId);
        order.setOrderAmount(seckillActivity.getSeckillPrice().longValue());
        /*
         *2.发送创建订单消息
         */
        rocketMQService.sendMessage("seckill_order", JSON.toJSONString(order));

        /*
        * 3.发送订单付款状态校验消息
        * 开源RocketMQ支持延迟消息，但是不支持秒级精度。默认支持18个level的延迟消息，这是通
        过broker端的messageDelayLevel配置项确定的，如下:
        * messageDelayLevel=1s 5s 10s 30s 1m 2m 3m 4m 5m 6m 7m 8m 9m 10m 20m
        30m 1h 2h
        * */
        rocketMQService.sendDelayMessage("pay_check", JSON.toJSONString(order), 3);
        return order;
    }

    @Autowired
    private OrderDao orderDao;
    /**
     * 订单支付完成处理 * @param orderNo */
    public void payOrderProcess(String orderNo) throws Exception {
        log.info("完成支付订单 订单号:" + orderNo);
        Order order = orderDao.queryOrder(orderNo);

        /*
         * 1.判断订单是否存在
         * 2.判断订单状态是否为未支付状态
         *  */
        if (order == null) {
            log.error("订单号对应订单不存在:" + orderNo);
            return;
        } else if(order.getOrderStatus() != 1) {
            log.error("订单状态无效:" + orderNo);
            return;
        }

        order.setOrderStatus(2);
        order.setPayTime(new Date());
        orderDao.updateOrder(order);

        /*
         *3.发送订单付款成功消息
         */
        rocketMQService.sendMessage("pay_done", JSON.toJSONString(order));
        
    }
}
