package org.seckill.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.seckill.dto.Exposer;
import org.seckill.dto.SeckillExecution;
import org.seckill.entity.Seckill;
import org.seckill.exception.SeckillException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import java.util.List;
import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:spring/applicationContext-dao.xml",
        "classpath:spring/applicationContext-service.xml"})
public class SeckillServiceTest {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private SeckillService seckillService;

    @Test
    public void getSeckillList() {
        List<Seckill> seckillList = seckillService.getSeckillList();
        logger.info("list={}", seckillList);
    }

    @Test
    public void getSeckillById() {
        long seckillId = 10000;
        Seckill seckill = seckillService.getSeckillById(seckillId);
        logger.info("seckill={}", seckill);
    }

    @Test
    public void exportSeckillUrl() {
        long id = 10000;
        Exposer exposer = seckillService.exportSeckillUrl(id);
        //Exposer{exposed=true, md5='780621db83f4f40e980fad9f921a624f', seckillId=10000, now=0, start=0, end=0}
        logger.info("exposer={}" , exposer);
    }

    @Test
    public void executeSeckill() {
        long id = 10000;
        long phone = 13514612399L;
        String md5 = "780621db83f4f40e980fad9f921a624f";
        SeckillExecution execution = seckillService.executeSeckill(id, phone, md5);
        logger.info("execution={}" , execution);
    }

    /**
     * 整合秒杀测试
     */
    @Test
    public void doSeckill(){
        long id = 10000;
        long phone = 15845755468L;
        Exposer exposer = seckillService.exportSeckillUrl(id);
        if(exposer.isExposed()){
            try {
                SeckillExecution seckill = seckillService.executeSeckill(id, phone, exposer.getMd5());
                logger.info("seckill={}" , seckill);
            } catch (SeckillException e) {
                logger.error(e.getMessage());
            }
        }else {
            //秒杀未开启或关闭了
            logger.warn("exposer={}" , exposer);
        }
    }

    @Test
    public void executeSeckillPro(){
        long seckillId = 10001L;
        long phone = 13514623589L;
        Exposer exposer = seckillService.exportSeckillUrl(seckillId);
        if(exposer.isExposed()){
            String md5 = exposer.getMd5();
            SeckillExecution sk = seckillService.executeSeckillProcedure(seckillId, phone, md5);
            logger.info(sk.getStateInfo());
        }else {
            //秒杀未开启或关闭了
            logger.warn("exposer={}" , exposer);
        }
    }
}
