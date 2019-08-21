package org.seckill.dao;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.seckill.entity.Seckill;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;

import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;

/*
配置spring和junit整合，junit启动时加载springIOC容器
spring-test,junit
 */
@RunWith(SpringJUnit4ClassRunner.class)
//告诉junit spring配置文件
@ContextConfiguration({"classpath:spring/spring-dao.xml"})
public class SeckillDaoTest {
    //注入Dao实现类依赖
    @Resource //这个注释实现自动注入
    private SeckillDao seckillDao;


    @Test
    public void testQueryById() throws Exception{
        Seckill seckill = seckillDao.queryById(1000);
        System.out.println(seckill.getName());
        System.out.println(seckill);
//        1000元秒杀iphone8
//        Seckill{seckillId=1000, name='1000元秒杀iphone8', number=100, startTime=Thu Aug 15 00:00:00 CST 2019, endTime=Fri Aug 16 00:00:00 CST 2019, createTime=Sat Aug 17 14:24:40 CST 2019}

    }
    @Test
    public void testReduceNumber() throws Exception{
        int updateCount = seckillDao.reduceNumber(1000L, new Date());
        System.out.println("updateCount:" + updateCount);

    }
    @Test
    public void testQueryAll() throws Exception{

        List<Seckill> list = seckillDao.queryAll(0, 100);
        for(Seckill seckill: list){
            System.out.println(seckill);
        }
        //这里报错  Parameter 'offset' not found. Available parameters are [0, 1, param1, param2]
        //因为java没有保存形参的记录：queryAll(int offset, int limit) -> query(arg0, arg1) 需要用注解@Param
        // Seckill{seckillId=1000, name='1000元秒杀iphone8', number=100, startTime=Thu Aug 15 00:00:00 CST 2019, endTime=Fri Aug 16 00:00:00 CST 2019, createTime=Sat Aug 17 14:24:40 CST 2019}
        //Seckill{seckillId=1001, name='500元秒杀ipad2', number=200, startTime=Thu Aug 15 00:00:00 CST 2019, endTime=Fri Aug 16 00:00:00 CST 2019, createTime=Sat Aug 17 14:24:40 CST 2019}
        //Seckill{seckillId=1002, name='300元秒杀小米4', number=300, startTime=Thu Aug 15 00:00:00 CST 2019, endTime=Fri Aug 16 00:00:00 CST 2019, createTime=Sat Aug 17 14:24:40 CST 2019}
        //Seckill{seckillId=1003, name='200元秒杀红米note', number=400, startTime=Thu Aug 15 00:00:00 CST 2019, endTime=Fri Aug 16 00:00:00 CST 2019, createTime=Sat Aug 17 14:24:40 CST 2019}
    }

}