package org.seckill.dao;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.seckill.entity.SuccessKilled;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:spring/spring-dao.xml"})
public class SuccessKilledDaoTest {
    //依赖注入的注解
    @Resource
    private SuccessKilledDao successKilledDao;
    @Test
    public void insertSuccessKilledTest() throws Exception{
        long id = 1001;
        long phone = 13542227778L;
        int insertCount = successKilledDao.insertSuccessKilled(id, phone);
        //第一次执行:insertCount:1
        //第二次执行：insertCount:0 主键冲突
        System.out.println("insertCount:" + insertCount);

    }
    @Test
    public void queryByIdWithSeckillTest() throws Exception{
        long id = 1001;
        long phone = 13542227778L;
        SuccessKilled successKilled = successKilledDao.queryByIdWithSeckill(id, phone);
        System.out.println(successKilled);
        System.out.println(successKilled.getSeckill());
        //SuccessKilled{seckillId=1000, userPhone=13542227778, state=-1, createTime=Sat Aug 17 21:54:06 CST 2019}
        //Seckill{seckillId=1000, name='1000元秒杀iphone8', number=100, startTime=Thu Aug 15 00:00:00 CST 2019, endTime=Fri Aug 16 00:00:00 CST 2019, createTime=Sat Aug 17 14:24:40 CST 2019}

    }
}