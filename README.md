# seckill
秒杀项目基本逻辑

## 使用的技术
+ 项目管理 maven  
+ dao层mybatis 
+ 数据库mysql 
+ 连接池druid 
+ servicespring 
+ web层springmvc 
+ 日志logback
+ 前端bootstarp + jquery

## 数据库
    库中只有seckill 和 successKilled表 对于用户信息只是用phone代替
    本项目专注于秒杀业务 不会过多设计其他部分

## dao层
---
    对数据库的操作 整合mybatis和spring 没什么好说的
---
    使用接口+mapper.xml方式
    对于多对一的部分使用 别名映射
    (不使用mybatis中的resultMap多对一映射 太复杂)

## service层
    新建了dto(data transfer object数据传输对象)包 用来存放
    虽然类似实体类(或者pojo)但功能不同 所以分了包
---
    新建exception包管理异常 
    继承RuntimeException 因为spring事务的回滚是依赖于运行时异常的      
---   
    事务使用注解开发
        1 别人看你代码很容易知道哪个方法使用了事务
        2 你自己知道这个方法用了事务 就会尽可能优化
            (使此方法尽可能执行快 将缓存等操作放在外部执行)
        3 不是所有方法都需要事务           
             使用声明式开发很容易造成所有方法都开启事务 或者有需要事务的方法反而没有事务
             
##  web层
    jsp使用bootstrap框架  js使用jquery(前端写的不多)  
    完成Controller包的构建 采用restful风格构建url
---
    整合项目流程  测试通过  基本秒杀功能已经实现  接下来需要进行优化   
    
## 优化点
+  改变了原先的事务操作  
  原先是先减少库存数量，再增加订单记录  
  这样有个问题，减少库存数量这个操作会上行级锁  
  加上网络传输延迟和GC时间，使得秒杀事务过长  
  而且存在用户重复秒杀，这样的操作是不用去占用行级锁的  
  改变顺序可以使得并发提高
+  把执行逻辑放在Mysql上  
  即使用Mysql的存储过程(Oracle的PL/SQL)  
  这样可以有效减少延迟时间  
+  增加NoSql(适用于分布式和集群)  
  使用原子计数器来记录库存数量  
  通过消息队列的生产者记录行为  
  消费者消费消息并落地到Mysql
   

