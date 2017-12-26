# noinspection SqlNoDataSourceInspectionForFile

-- 数据库初始化脚本
-- 只关注业务逻辑 所以表的字段很少

-- 建数据库
create database seckill default character set utf8 collate utf8_general_ci;

-- 使用数据库
use seckill;

-- 创建表
-- 1 秒杀库存表 我用的mysql 5.7.44 字段不能加 '' (有毒)
create table seckill(
	seckill_id bigint NOT NULL AUTO_INCREMENT COMMENT '商品库存id',
	name varchar(120) NOT NULL COMMENT '商品名称',
	number int NOT NULL COMMENT '库存数量',
	start_time timestamp NOT NULL COMMENT '秒杀开始时间',
	end_time timestamp NOT NULL COMMENT '秒杀结束时间',
	create_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
	PRIMARY KEY (seckill_id),
	key idx_start_time(start_time),
	key idx_end_time(end_time),
	key idx_create_time(create_time)
)ENGINE=InnoDB AUTO_INCREMENT=10000 DEFAULT CHARSET=utf8 COMMENT '库存表';

-- 初始化数据
insert into
	seckill(name , number , start_time , end_time)
values
	('400元秒杀iPad Pro', 700, '2017-5-27 04:25:00', '2017-5-28 01:25:00'),
 	('300元秒杀iPad Air', 500, '2017-5-27 01:25:00', '2018-5-28 01:25:00'),
	('1000元秒杀Mac Air', 500, '2017-5-27 08:25:00', '2017-7-28 01:25:00'),
	('2000元秒杀Mac Pro', 200, '2017-5-27 09:25:00', '2017-7-28 01:25:00');

-- 秒杀成功明细表
-- 用户登陆认证相关的信息
create table success_killed(
	seckill_id bigint NOT NULL COMMENT '秒杀商品id',
	user_phone bigint NOT NULL COMMENT '用户手机号',
	state tinyint NOT NULL DEFAULT -1 COMMENT '状态标识 :-1 无效 , 0 成功 , 1 已付款',
	create_time timestamp NOT NULL COMMENT '创建时间',
	PRIMARY KEY(seckill_id , user_phone),/*联合主键*/
	key idx_create_time(create_time)
)ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT '库存表';