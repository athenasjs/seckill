-- 数据库初始化脚本

-- 创建数据库
CREATE DATABASE seckill;
-- 使用数据库
use seckill;
-- 创建秒杀库存表
CREATE TABLE seckill(
  `seckill_id` bigint NOT NULL AUTO_INCREMENT COMMENT'商品库存id',
  `name` varchar(120)   NOT NULL COMMENT '商品名称',
  `number` int NOT NULL COMMENT '库存数量',
  `start_time` timestamp NOT NULL DEFAULT '2019-08-15 00:00:00' COMMENT'秒杀开启时间',
  `end_time` timestamp NOT NULL DEFAULT '2019-08-16 00:00:00' COMMENT'秒杀结束时间',
  -- 创建时间设定数据库默认值
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT'创建时间',
  PRIMARY KEY (seckill_id),
  -- 下面的索引是为了加速查询
  key idx_start_time(start_time),
  key idx_end_time(end_time),
  key idx_create_time(create_time)
)ENGINE=InnoDB AUTO_INCREMENT=1000 DEFAULT CHARSET=utf8 COMMENT='秒杀库存表';

-- 初始化数据
INSERT INTO
  seckill (name, number, start_time,end_time)
VALUES
    ('1000元秒杀iphone8', 100, '2019-08-15 00:00:00', '2019-08-16 00:00:00'),
    ('500元秒杀ipad2', 200, '2019-08-15 00:00:00', '2019-08-16 00:00:00'),
    ('300元秒杀小米4', 300, '2019-08-15 00:00:00', '2019-08-16 00:00:00'),
    ('200元秒杀红米note', 400, '2019-08-15 00:00:00', '2019-08-16 00:00:00');

-- 秒杀成功明细表
-- 用户登录认证相关的信息 这里用user_phone 正常是需要权限认证
CREATE TABLE success_killed(
  `seckill_id` bigint NOT NULL COMMENT'秒杀商品id',
  `user_phone` bigint NOT NULL COMMENT'用户手机号',
  `state` tinyint NOT NULL DEFAULT -1 COMMENT '状态标识:-1:无效  0:成功 1:已付款 2:已发货',
  -- 所有表都有这个字段，代表这行字段创建的时间 这行记录插入的时间 秒杀成功的时间
  `create_time` timestamp NOT NULL comment '创建时间',
  PRIMARY KEY(seckill_id, user_phone),  /*联合主键 两个字段保证唯一性 后面防止用户重复秒杀，基于这个唯一性可以做过滤*/
  key idx_create_time(create_time)
)ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='秒杀成功明细表';

-- 连接数据库控制台
mysql -root -p

-- 手写DDL
-- 记录每次上线的DDL修改
-- 上线V1.1
ALTER TABLE seckill
DROP IDNEX idx_create_time,
ADD index idx_c_s(start_time, create_time);