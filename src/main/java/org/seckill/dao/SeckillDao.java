package org.seckill.dao;

import org.seckill.entity.Seckill;

import java.util.Date;
import java.util.List;

public interface SeckillDao {

    /**
     * 减库存
     *
     * @param seckillId
     * @param KillTime
     * @return
     */
    int reduceNumber(long seckillId, Date KillTime);

    Seckill queryByid(long seckillId);

    List<Seckill> queryAll(int offet, int limit);
}
