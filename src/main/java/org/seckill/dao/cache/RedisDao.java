package org.seckill.dao.cache;

import com.dyuproject.protostuff.LinkedBuffer;
import com.dyuproject.protostuff.ProtostuffIOUtil;
import com.dyuproject.protostuff.runtime.RuntimeSchema;
import org.seckill.entity.Seckill;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 * redis的数据访问对象
 * @author: sjswh
 * @date: 2019-08-20 17:00:27
 * @description:
 **/
public class RedisDao {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    //运行期根据字节码对象动态创建schema
    private RuntimeSchema<Seckill> schema = RuntimeSchema.createFrom(Seckill.class);
    //JedisPool相当于数据库连接池
    private JedisPool jedisPool;

    public RedisDao(String ip,  int port){
        jedisPool = new JedisPool(ip, port);
    }

    //缓存获取Seckill对象
    public Seckill getSeckill(long seckillId){

        //redis操作的逻辑 不应该放在service代码中


        try {
            //Jedis相当于数据库的connection 最后要释放
            Jedis jedis = jedisPool.getResource();
            try {
                //redis的key
                String key = "seckillId:" + seckillId;
                //redis并没有实现内部序列化操作 存储都是二进制数组
                //序列化也是高并发的一个点 这里的业务场景需要对序列化的高并发做优化
                //所以不选择java的序列化，选择开源社区提供的protostuff 创建对象序列化过程非常高效
                //一个是序列化的速度，另一个是大小 在网络上传输的字节数
                //get ->byte[]  -> 反序列化 ->object(Seckill)
                //采用自定义序列化
                byte[] bytes = jedis.get(key.getBytes());
                //从缓存中获取到
                if(bytes != null){
                    //这是一个根据schema生出的空对象
                    Seckill seckill = schema.newMessage();
                    //反序列化  比原生序列化空间压缩五分之一 速度大幅提高
                    ProtostuffIOUtil.mergeFrom(bytes, seckill, schema);
                    return seckill;
                }


            } finally {
                jedis.close();
            }


        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        return null;
    }

    //把Seckill对象传递到redis中
    public String putSeckill(Seckill seckill){
        // set Object (Seckill) -> 序列化 ->byte[]


        try {
            Jedis jedis = jedisPool.getResource();

            try{
                String key = "seckillId:" + seckill.getSeckillId();
                byte[] bytes = ProtostuffIOUtil.toByteArray(seckill, schema,
                        LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE));
                //超时缓存 1小时
                int timeout = 60*60;
                String result = jedis.setex(key.getBytes(), timeout, bytes);
                //如果返回正确信息会返回ok 否则会提示错误
                return result;
            }finally{
                jedis.close();
            }


        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        return null ;
    }

}
