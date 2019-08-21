package org.seckill.service.impl;

import com.sun.net.httpserver.Authenticator;
import org.apache.commons.collections.MapUtils;
import org.seckill.dao.SeckillDao;
import org.seckill.dao.SuccessKilledDao;
import org.seckill.dao.cache.RedisDao;
import org.seckill.dto.Exposer;
import org.seckill.dto.SeckillExecution;
import org.seckill.entity.Seckill;
import org.seckill.entity.SuccessKilled;
import org.seckill.enums.SeckillStatEnum;
import org.seckill.exception.RepeatKillException;
import org.seckill.exception.SeckillCloseException;
import org.seckill.exception.SeckillException;
import org.seckill.service.SeckillService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 业务操作过程： 判断 + DTO数据封装
 * @author: sjswh
 * @date: 2019-08-18 16:20:08
 * @description:
 **/
@Service
public class SeckillServiceImpl implements SeckillService {

    //统一的日志API slf4j
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    //需要在spring容器中获取实例并注入到service中
    //注入Service依赖 自动注入  该实例已经存在，mybaits帮我们实现了
    @Autowired  //@Resource @Inject
    private SeckillDao seckillDao;

    @Autowired
    private SuccessKilledDao successKilledDao;
    @Autowired
    private RedisDao redisDao;
    //MD5盐值
    private final String slat = "sdhaldkhsadklhasdkaskl13w47813%^&%^&%^&$%";

    public List<Seckill> getSeckillList() {

        return seckillDao.queryAll(0, 4);
    }

    public Seckill getById(long seckillId) {
        return seckillDao.queryById(seckillId);
    }

    public Exposer exportSeckillUrl(long seckillId) {

        //优化点  缓存优化 主要是针对下面的数据库查询操作
        //一致性维护建立在超时基础上  因为秒杀单对象一般情况不会改变 如果要改，一般会废弃掉+ 重建
        //虽然是通过主键查询，一般比较快，但是所有的秒杀单都要发起请求 通过redis优化 降低数据库访问压力
        /*
        * get from cache
        * if null
        * get db
        * else
        *       put cache
        * locgoin
        *
        * */
        //1.访问缓存
        Seckill seckill = redisDao.getSeckill(seckillId);
        if(seckill == null){
            //2.访问数据库
            seckill = seckillDao.queryById(seckillId);
            if(seckill == null){
                //秒杀单不存在
                return new Exposer(false, seckillId);
            }else{
                //3.放入redis
                redisDao.putSeckill(seckill);
            }
        }

        Date startTime = seckill.getStartTime();
        Date endTime = seckill.getEndTime();
        Date date = new Date();
        if(date.getTime() < startTime.getTime() ||
            date.getTime() > endTime.getTime()){
            return new Exposer(false, seckillId, date.getTime(), startTime.getTime(), endTime.getTime());

        }

        //转化特定字符串的过程，不可逆
        String md5 = getMD5(seckillId);
        return new Exposer(true, md5, seckillId);

    }

    //生成MD5的方法  单独抽取出来
    private String getMD5(long seckillId){

        String base = seckillId + "/" + slat;
        String md5 = DigestUtils.md5DigestAsHex(base.getBytes());
        return md5;

    }

    //秒杀核心业务
    @Transactional
    public SeckillExecution executeSeckill(long seckillId, long userPhone, String md5) throws SeckillException, RepeatKillException, SeckillCloseException {

        //数据篡改
        if(null == md5 || !md5.equals(getMD5(seckillId))){
            throw new SeckillException("seckill data rewrite");
        }
        //执行秒杀逻辑：减库存 + 记录购买行为
        Date nowTime = new Date();

        //整体try catch因为可能出现其他异常比如插入超时，或者数据库连接断了等
        try{
            //减库存成功,记录购买行为
            int insertCount = successKilledDao.insertSuccessKilled(seckillId, userPhone);
            //唯一验证:seckillId userPhone
            if(insertCount <= 0){
                //重复秒杀
                throw new RepeatKillException("seckill repeated");
            }else{
                //减库存，热点商品竞争 操作顺序调换了一下
                int updateCount = seckillDao.reduceNumber(seckillId, nowTime);

                if(updateCount <= 0){
                    //没有更新到记录， 秒杀结束,rollback
                    throw new SeckillCloseException("seckill is closed");
                }else{
                    //秒杀成功
                    SuccessKilled successKilled = successKilledDao.queryByIdWithSeckill(seckillId, userPhone);
                    //直接使用枚举 把数据字典放到枚举中
                    return new SeckillExecution(seckillId, SeckillStatEnum.SUCCESS, successKilled);
                }

            }

        }catch(SeckillCloseException e1){
            throw e1;
        }
        catch(RepeatKillException e2){
            throw e2;
        }
        catch(Exception e){
            logger.error(e.getMessage(), e);
            //所有编译期异常转化为运行期异常 spring会rollback 避免减库存操作和购买记录没有同时执行
            throw new SeckillException("seckill inner error:" + e.getMessage());
        }


    }

    public SeckillExecution executeSeckillProcedure(long seckillId, long userPhone, String md5) {

        if(md5 == null || !md5.equals(getMD5(seckillId))){
            return new SeckillExecution(seckillId, SeckillStatEnum.DATA_REWRITE);

        }

        Date killTime = new Date();
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("seckillId", seckillId);
        map.put("phone", userPhone);
        map.put("killTime", killTime);
        map.put("result", null);

        try {
            //执行存储过程，result被赋值
            seckillDao.killByProcedure(map);
            //获取result
            int result = MapUtils.getInteger(map, "result", -2);
            if(result == 1){
                SuccessKilled sk = successKilledDao.
                        queryByIdWithSeckill(seckillId, userPhone);
                return new SeckillExecution(seckillId, SeckillStatEnum.SUCCESS, sk);

            }else{
                return new SeckillExecution(seckillId, SeckillStatEnum.stateOf(result));
            }

        } catch (Exception e) {
            return new SeckillExecution(seckillId, SeckillStatEnum.INNER_ERROR);
        }


    }
}
