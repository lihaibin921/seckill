# seckill
秒杀项目基本逻辑

## 使用的技术
+ 项目管理 maven  
+ dao层使用mybatis 
+ 数据库mysql 
+ 连接池druid 
+ service使用spring 
+ web层使用springmvc 
+ 日志使用logback

## dao层
    对数据库的操作 整合mybatis和spring 没什么好说的
---
    使用接口+mapper.xml方式
    对于多对一的部分使用 别名映射
    (不适用mybatis中的resultMap多对一映射 太复杂)

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

