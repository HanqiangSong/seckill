package com.countdown.seckill.db.dao;

import com.countdown.seckill.db.mappers.OrderMapper;
import com.countdown.seckill.db.po.Order;

import javax.annotation.Resource;

public class OrderDaoImpl implements OrderDao {

    @Resource
    private OrderMapper orderMapper;

    @Override
    public void insertOrder(Order order) {
        orderMapper.insert(order);
    }

    @Override
    public Order queryOrder(String orderNo) {
        return orderMapper.selectByOrderNo(orderNo);
    }

    @Override
    public void updateOrder(Order order) {
        orderMapper.updateByPrimaryKey(order);
    }
}
