package org.seckill.exception;

/**
 * 秒杀关闭异常 (秒杀时间到、库存消耗完，不应该再秒杀)
 * @author: sjswh
 * @date: 2019-08-18 15:38:35
 * @description:
 **/
public class SeckillCloseException extends SeckillException{

    public SeckillCloseException(String message) {
        super(message);
    }

    public SeckillCloseException(String message, Throwable cause) {
        super(message, cause);
    }
}
