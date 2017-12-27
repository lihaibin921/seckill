package org.seckill.exception;

/**
 * 重复秒杀异常(本质上是运行期异常)
 */
public class RepeatKillExcption extends SeckillException{

    public RepeatKillExcption(String message) {
        super(message);
    }

    public RepeatKillExcption(String message, Throwable cause) {
        super(message, cause);
    }
}
