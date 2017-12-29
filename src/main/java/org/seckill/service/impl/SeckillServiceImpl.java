package org.seckill.service.impl;

import org.apache.commons.collections.MapUtils;
import org.seckill.dao.SeckillDao;
import org.seckill.dao.SuccessKilledDao;
import org.seckill.dao.cache.RedisDao;
import org.seckill.dto.Exposer;
import org.seckill.dto.SeckillExecution;
import org.seckill.entity.Seckill;
import org.seckill.entity.SuccessKilled;
import org.seckill.enums.SeckillStatEnum;
import org.seckill.exception.RepeatKillExcption;
import org.seckill.exception.SeckillCloseException;
import org.seckill.exception.SeckillException;
import org.seckill.service.SeckillService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SeckillServiceImpl implements SeckillService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    // md5加密时 混淆字符串 越乱越好
    private final String slat = "sagfgavnpoaijgpe;af5a4g5dgoj943gjs#T@#$^dgsd";

    @Autowired
    private SeckillDao seckillDao;

    @Autowired
    private SuccessKilledDao successKilledDao;

    @Autowired
    private RedisDao redisDao;


    public List<Seckill> getSeckillList() {
        return seckillDao.queryAll(0, 4);
    }

    public Seckill getSeckillById(long seckillId) {
        return seckillDao.queryByid(seckillId);
    }

    public Exposer exportSeckillUrl(long seckillId) {

        // 缓存优化 先查缓存
        Seckill seckill = redisDao.getSeckill(seckillId);

        // 缓存没有
        if (seckill == null) {
            // 查数据库
            seckill = seckillDao.queryByid(seckillId);
            if (seckill == null) {
                return new Exposer(false, seckillId);
            } else {
                // 存缓存
                redisDao.setSeckill(seckill);
            }
        }

        Date startTime = seckill.getStartTime();
        Date endTime = seckill.getEndTime();
        Date nowTime = new Date();

        if (nowTime.getTime() < startTime.getTime() || nowTime.getTime() > endTime.getTime()) {
            return new Exposer(false, seckillId, nowTime.getTime(), startTime.getTime(),
                    endTime.getTime());
        }

        // 转化特定字符的过程
        String md5 = getMd5(seckillId);
        return new Exposer(true, md5, seckillId);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public SeckillExecution executeSeckill(long seckillId, long userPhone, String md5)
            throws SeckillException, RepeatKillExcption, SeckillCloseException {

        if (md5 == null || !md5.equals(getMd5(seckillId))) {
            throw new SeckillException("秒杀数据被重写过了");
        }

        // 执行秒杀逻辑 : 减少库存 ＋ 记录
        // 优化 先插入 后减少库存 减少航级锁的持有时间
        Date nowTime = new Date();

        try {
            // 记录购买成功
            int insertCount = successKilledDao.insertSuccessKilled(seckillId, userPhone);

            // 重复秒杀解决
            if (insertCount <= 0) {
                throw new RepeatKillExcption("重复秒杀");
            } else {
                // 减库存
                int updateCount = seckillDao.reduceNumber(seckillId, nowTime);
                if (updateCount <= 0) {
                    // 更新失败 rollback
                    throw new SeckillCloseException("秒杀结束");
                } else {
                    // 秒杀成功 返回成功信息 commit
                    SuccessKilled successKilled =
                            successKilledDao.queryByIdWithSeckill(seckillId, userPhone);

                    return new SeckillExecution(seckillId, SeckillStatEnum.SUCCESS, successKilled);
                }
            }
        } catch (SeckillCloseException e1) {
            throw e1;
        } catch (RepeatKillExcption e2) {
            throw e2;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);

            throw new SeckillException("秒杀异常" + e.getMessage());
        }
    }

    @Override
    public SeckillExecution executeSeckillProcedure(long seckillId, long userPhone, String md5) {
        if (md5 == null || !md5.equals(getMd5(seckillId))) {
            return new SeckillExecution(seckillId, SeckillStatEnum.DATA_REWRITE);
        }

        Date killTime = new Date();

        Map<String, Object> map = new HashMap<>();
        map.put("seckillId", seckillId);
        map.put("phone", userPhone);
        map.put("killTime", killTime);
        map.put("result", null);

        try {
            seckillDao.killByProcedure(map);
            // 获取result
            Integer result = MapUtils.getInteger(map, "result", -2);
            if (result == 1) {
                SuccessKilled sk = successKilledDao.queryByIdWithSeckill(seckillId, userPhone);
                return new SeckillExecution(seckillId ,SeckillStatEnum.SUCCESS , sk);
            }else{
                return new SeckillExecution(seckillId , SeckillStatEnum.stateOf(result));
            }
        } catch (Exception e) {
            logger.error(e.getMessage() , e);
            return new SeckillExecution(seckillId , SeckillStatEnum.INNER_ERROR);
        }
    }

    private String getMd5(long seckillId) {
        String base = seckillId + "/" + slat;
        String md5 = DigestUtils.md5DigestAsHex(base.getBytes());
        return md5;
    }
}
