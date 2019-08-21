package org.seckill.exception;

/**
 * 重复秒杀异常（运行期异常） spring的声明式事务只接受运行期异常回滚的策略
 * @author: sjswh
 * @date: 2019-08-18 15:33:05
 * @description:
 **/
public class RepeatKillException extends SeckillException {

    public RepeatKillException(String message) {
        super(message);
    }

    public RepeatKillException(String message, Throwable cause) {
        super(message, cause);
    }
}
