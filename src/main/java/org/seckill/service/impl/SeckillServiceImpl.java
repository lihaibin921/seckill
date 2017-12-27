package org.seckill.service.impl;

import org.seckill.dao.SeckillDao;
import org.seckill.dao.SuccessKilledDao;
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
import java.util.List;

@Service
public class SeckillServiceImpl implements SeckillService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    // md5加密时 混淆字符串 越乱越好
    private final String slat = "sagfgavnpoaijgpe;af5a4g5dgoj943gjs#T@#$^dgsd";

    @Autowired
    private SeckillDao seckillDao;

    @Autowired
    private SuccessKilledDao successKilledDao;


    public List<Seckill> getSeckillList() {
        return seckillDao.queryAll(0, 4);
    }

    public Seckill getSeckillById(long seckillId) {
        return seckillDao.queryByid(seckillId);
    }

    public Exposer exportSeckillUrl(long seckillId) {

        Seckill seckill = seckillDao.queryByid(seckillId);

        if (seckill == null) {
            return new Exposer(false, seckillId);
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

        if(md5 == null || !md5.equals(getMd5(seckillId))){
            throw new SeckillException("秒杀数据被重写过了");
        }

        //执行秒杀逻辑 :　减少库存　＋　记录
        Date nowTime = new Date();


        try {
            int updateCount = seckillDao.reduceNumber(seckillId, nowTime);
            if(updateCount <= 0 ){
                //更新失败
                throw new SeckillCloseException("秒杀结束");
            }else {
                //记录购买成功
                int insertCount = successKilledDao.insertSuccessKilled(seckillId, userPhone);

                //重复秒杀解决
                if(insertCount <= 0){
                    throw new RepeatKillExcption("重复秒杀");
                }else{
                    //秒杀成功 返回成功信息
                    SuccessKilled successKilled = successKilledDao.queryByIdWithSeckill(seckillId, userPhone);

                    return new SeckillExecution(seckillId , SeckillStatEnum.SUCCESS , successKilled);
                }
            }

        } catch (SeckillCloseException e1){
            throw e1;
        } catch (RepeatKillExcption e2){
            throw e2;
        }catch (Exception e) {
            logger.error(e.getMessage() , e);

            throw new SeckillException("秒杀异常" + e.getMessage());
        }
    }

    private String getMd5(long seckillId) {
        String base = seckillId + "/" + slat;
        String md5 = DigestUtils.md5DigestAsHex(base.getBytes());
        return md5;
    }
}
