package com.countdown.seckill.mq;

import com.alibaba.fastjson.JSON;
import com.countdown.seckill.db.dao.OrderDao;
import com.countdown.seckill.db.dao.SeckillActivityDao;
import com.countdown.seckill.db.po.Order;
import com.countdown.seckill.util.RedisService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@RocketMQMessageListener(topic = "pay_check", consumerGroup = "pay_check_group")
public class PayStatusCheckListener  implements RocketMQListener<MessageExt> {
    @Autowired
    private OrderDao orderDao;
    @Autowired
    private SeckillActivityDao seckillActivityDao;
    @Resource
    private RedisService redisService;

    @Override
    @Transactional
    public void onMessage(MessageExt messageExt) {
        String message = new String(messageExt.getBody(),
                StandardCharsets.UTF_8);
        log.info("接收到订单支付状态校验消息:" + message);
        //1.查询订单
        Order order = JSON.parseObject(message, Order.class);
        //2.判读订单是否完成支付
        Order orderInfo = orderDao.queryOrder(order.getOrderNo());
        if (orderInfo.getOrderStatus() != 2) {
            orderInfo.setOrderStatus(99);
            orderDao.updateOrder(orderInfo);
            //4.恢复数据库库存
            seckillActivityDao.revertStock(order.getSeckillActivityId());
            // 恢复 redis 库存
            redisService.revertStock("stock:" + order.getSeckillActivityId());
        }
    }
}
