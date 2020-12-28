package com.countdown.seckill.db.mappers;

import com.countdown.seckill.db.po.SeckillCommodity;

public interface SeckillCommodityMapper {
    int deleteByPrimaryKey(Long id);

    int insert(SeckillCommodity record);

    int insertSelective(SeckillCommodity record);

    SeckillCommodity selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(SeckillCommodity record);

    int updateByPrimaryKey(SeckillCommodity record);
}