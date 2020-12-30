package com.countdown.seckill.db.dao;

import com.countdown.seckill.db.po.Order;

public interface OrderDao {

    void insertOrder(Order order);

    Order queryOrder(String orderNo);

    void updateOrder(Order order);
}
