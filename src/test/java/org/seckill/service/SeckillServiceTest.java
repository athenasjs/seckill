package org.seckill.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.seckill.dto.Exposer;
import org.seckill.dto.SeckillExecution;
import org.seckill.entity.Seckill;
import org.seckill.exception.RepeatKillException;
import org.seckill.exception.SeckillCloseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({
        "classpath:spring/spring-dao.xml",
        "classpath:spring/spring-service.xml"
})
public class SeckillServiceTest {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private SeckillService seckillService;

    @Test
    public void testGetSeckillList() {
        List<Seckill> list = seckillService.getSeckillList();
        //={}是占位符，list输出在这里 这是用logback的配置
        logger.info("list={}", list);


    }

    @Test
    public void testGetById() {
        Seckill seckill = seckillService.getById(1000);
        logger.info("seckill={}", seckill);
    }

    @Test
    public void testExportSeckillUrl() {
        Exposer exposer = seckillService.exportSeckillUrl(1004);
        logger.info("exposer={}" + exposer);
        //20:14:47.589 [main] INFO  o.seckill.service.SeckillServiceTest - exposer={}Exposer{exposed=false, md5='null', seckillId=1000, now=1566130487588, start=1565798400000, end=1565884800000}
        //exposer={}Exposer{exposed=true, md5='9fc8e536d04048f3ac01040075c91229', seckillId=1004, now=0, start=0, end=0}
    }

    @Test
    public void testExecuteSeckill() {
        //拿到md5传到方法中与生成的值比较  一致则进行秒杀
        String md5 = "9fc8e536d04048f3ac01040075c91229";
        long seckillId = 1004L;
        long userPhone = 15678964579L;
        try{
            //这里把一些业务异常trycatch 不向上抛给junit 就不会以异常形式出来
            //这样当出现这两个异常时也认为通过测试
            SeckillExecution seckillExecution = seckillService.executeSeckill(seckillId, userPhone, md5);
            logger.info("seckillExecution={}" ,seckillExecution);

        }catch(RepeatKillException e){
            logger.error(e.getMessage());

        }catch(SeckillCloseException e1){
            logger.error(e1.getMessage());
        }
        //第二次秒杀会重复秒杀异常
        //org.seckill.exception.RepeatKillException: seckill repeated
        //从日志可以看出完全是被spring的事务所管理的



    }

    //整合上面两个测试方法形成完整逻辑
    //继承测试业务覆盖完整性
    @Test
    public void testSeckillLogic() throws Exception{
        long seckillId = 1004L;
        long userPhone = 18878964579L;
        Exposer exposer = seckillService.exportSeckillUrl(seckillId);
        if(exposer.isExposed()){
            try{
                //这里把一些业务异常trycatch 不向上抛给junit 就不会以异常形式出来
                //这样当出现这两个异常时也认为通过测试
                //允许重复执行
                //通过一个单元测试完成业务流程  展示秒杀接口地址 开启时候执行秒杀
                SeckillExecution seckillExecution = seckillService.executeSeckill(seckillId, userPhone, exposer.getMd5());
                logger.info("seckillExecution={}",seckillExecution);

            }catch(RepeatKillException e){
                logger.error(e.getMessage());

            }catch(SeckillCloseException e1){
                logger.error(e1.getMessage());
            }
        }else{
            //秒杀还未开启
            logger.warn("exposer={}", exposer);
        }


    }

    @Test
    public void executeSeckillProcedure(){
        long seckillId = 1006;
        long phone = 1368011101;
        Exposer exposer = seckillService.exportSeckillUrl(seckillId);
        if(exposer.isExposed()){
            String md5 = exposer.getMd5();
            SeckillExecution execution = seckillService.executeSeckillProcedure(seckillId, phone, md5);
            logger.info(execution.getStateInfo());
        }
    }
}