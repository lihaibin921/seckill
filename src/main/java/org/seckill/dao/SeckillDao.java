package org.seckill.dao;

import org.apache.ibatis.annotations.Param;
import org.seckill.entity.Seckill;
import java.util.Date;
import java.util.List;

public interface SeckillDao {

    /**
     * 减库存
     */
    int reduceNumber(@Param("seckillId") long seckillId, @Param("killTime") Date killTime);

    Seckill queryByid(long seckillId);

    /**
     * @param("offset") 这个注解是mybatis提供给我们的 当参数超过一个时 不能写parameterType 而java在运行期间又不会保留形参的名字 只有类似arg0
     * 这样的描述 所以mybatis无法绑定参数 只好提供了这个注解 解决这个问题
     */
    List<Seckill> queryAll(@Param("offset") int offset, @Param("limit") int limit);
}
