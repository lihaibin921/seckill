package org.seckill.service;

import org.seckill.dto.Exposer;
import org.seckill.dto.SeckillExecution;
import org.seckill.entity.Seckill;
import org.seckill.exception.RepeatKillExcption;
import org.seckill.exception.SeckillCloseException;
import org.seckill.exception.SeckillException;
import java.util.List;

/**
 * 业务接口 应该站在使用者角度
 */
public interface SeckillService {

    /**
     * 查询所有秒杀记录
     * 
     * @return
     */
    List<Seckill> getSeckillList();

    /**
     * 查询单个秒杀记录
     * 
     * @param seckillId
     * @return
     */
    Seckill getSeckillById(long seckillId);

    /**
     * 秒杀开启时输出秒杀接口地址 否则输出系统时间和秒杀时间
     * 
     * @param seckillId
     */
    Exposer exportSeckillUrl(long seckillId);

    /**
     * 执行秒杀操作 抛出子类异常是为了告诉使用者 对子类异常明确指明异常信息
     *
     * @param seckillId
     * @param userPhone
     * @param md5
     */
    SeckillExecution executeSeckill(long seckillId, long userPhone, String md5)
            throws SeckillException, RepeatKillExcption, SeckillCloseException;


    /**
     * 执行秒杀操作 使用存储过程完成
     */
    SeckillExecution executeSeckillProcedure(long seckillId, long userPhone, String md5);

}
