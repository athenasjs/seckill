package org.seckill.service;

import org.seckill.dto.Exposer;
import org.seckill.dto.SeckillExecution;
import org.seckill.entity.Seckill;
import org.seckill.exception.RepeatKillException;
import org.seckill.exception.SeckillCloseException;
import org.seckill.exception.SeckillException;

import java.util.List;

/**
 * 业务接口：站在使用者角度设计业务接口
 * 三个方面： 方法定义粒度(不要太繁琐和太抽象)， 参数， 返回类型（return类型/异常）
 *
 * @author: sjswh
 * @date: 2019-08-18 14:58:25
 * @description:
 **/
public interface SeckillService {

    /**
     * 查询所有秒杀记录
     * @return
     */
    List<Seckill> getSeckillList();

    /**
     * 查询单个秒杀记录
     * @param seckillId
     * @return
     */
    Seckill getById(long seckillId);

    /**
     * 输出秒杀接口的地址，
     * 秒杀开启时输出秒杀接口地址
     * 否则输出系统时间和秒杀时间
     * 当秒杀没有开启时用户无法根据url规则拼出秒杀地址（提前交由浏览器插件去执行秒杀）
     * web层调用接口方法时可以拿到DTO
     * @param seckillId
     */
    Exposer exportSeckillUrl(long seckillId);

    //md5传进来和内部的md5生成规则比较，如果md5值变了说明用户的url被篡改，拒绝秒杀

    /**
     * 执行秒杀操作
     * @param seckillId
     * @param userPhone
     * @param md5
     */
     SeckillExecution executeSeckill(long seckillId, long userPhone, String md5)
     throws SeckillException, RepeatKillException, SeckillCloseException;

    /**
     * 执行秒杀操作by 存储过程
     * @param seckillId
     * @param userPhone
     * @param md5
     * @return
     */
     SeckillExecution executeSeckillProcedure(long seckillId, long userPhone, String md5);


}
