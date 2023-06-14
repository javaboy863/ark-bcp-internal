drop table if exists bcp_check_rule_config
drop table if exists bcp_check_rule_stat
drop table if exists bcp_condition_config
drop table if exists bcp_dynamic_code_config
drop table if exists bcp_event_source_config
drop table if exists bcp_event_task_item


CREATE TABLE `bcp_check_fail_record` (
  `id` int(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '自增ID',
  `rule_id` int(11) NOT NULL DEFAULT '0' COMMENT '检验规则ID',
  `msg_id` varchar(64) NOT NULL DEFAULT '' COMMENT '消息ID',
  `event_msg` varchar(2048) NOT NULL DEFAULT '' COMMENT '事件消息体',
  `reason` varchar(1024) NOT NULL DEFAULT '' COMMENT '一致性校验失败原因',
  `handle_msg` varchar(256) NOT null DEFAULT '' COMMENT '处理意见',
  `is_handle` tinyint not null default 0 comment '是否已经处理',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_createtime` (`create_time`),
  KEY `idx_ruleid_createtime` (`rule_id`,`create_time`),
  KEY `idx_ruleid_status_createtime` (`rule_id`,is_handle,`create_time`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COMMENT='数据校验失败记录表';


CREATE TABLE `bcp_check_rule_config` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT COMMENT '编号，主键',
  `event_id` int(11) NOT NULL DEFAULT '0' COMMENT '所属事件id',
  `rule_name` varchar(50) NOT NULL DEFAULT '' COMMENT '规则名称',
  `description` varchar(255) NOT NULL DEFAULT '' COMMENT '描述',
  `status` tinyint(1) NOT NULL DEFAULT '0' COMMENT '状态，1-开, 0-关',
  `is_delete` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否可用,0x1 是已经删除',
  `version` int(11) NOT NULL DEFAULT '1' COMMENT 'data version',
  `updated_by` varchar(50) NOT NULL DEFAULT '' COMMENT '最终修改人',
  `created_by` varchar(50) NOT NULL DEFAULT '' COMMENT '最终修改人',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='规则表';

CREATE TABLE `bcp_check_rule_stat` (
  `id` int(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '自增ID',
  `rule_id` int(11) NOT NULL DEFAULT '0' COMMENT '校验规则ID',
  `stat_time` int(11) NOT NULL DEFAULT '0' COMMENT '统计时间，根据不同的粒度可以有不同的格式，例如 小时粒度：yyyyMMddHH，分钟粒度：yyyyMMddHHmm',
  `succ_count` int(11) unsigned NOT NULL DEFAULT '0' COMMENT '成功数（脚本正常执行且数据验证一致）',
  `fail_count` int(11) unsigned NOT NULL DEFAULT '0' COMMENT '校验不一致数',
  `exception_count` int(11) unsigned NOT NULL DEFAULT '0' COMMENT '规则执行异常数',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最近一次更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uniq_rule_stat_fineness` (`rule_id`,`stat_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据校验规则执行情况统计表';


CREATE TABLE `bcp_condition_config` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT COMMENT '编号，主键',
  `rule_id` int(11) NOT NULL DEFAULT '0' COMMENT '所属规则编号',
  `type` tinyint(1) NOT NULL DEFAULT '0' COMMENT '条件类型，条件、条件组、规则模板',
  `parent_id` int(11) NOT NULL DEFAULT '0' COMMENT '父条件id',
  `params` varchar(2000) NOT NULL DEFAULT '' COMMENT '如果是规则模板，值为模板的描述信息',
  `is_delete` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否可用,0x1 是已经删除',
  `version` int(11) NOT NULL DEFAULT '1' COMMENT 'data version',
  `updated_by` varchar(50) NOT NULL DEFAULT '' COMMENT '最终修改人',
  `created_by` varchar(50) NOT NULL DEFAULT '' COMMENT '最终修改人',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='规则条件表';


CREATE TABLE `bcp_dynamic_code_config` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT COMMENT '编号，主键',
  `condition_id` varchar(32) NOT NULL DEFAULT '0' COMMENT '代码id',
  `type` tinyint(4) NOT NULL DEFAULT '0' COMMENT '代码分类, java/ groovy/ql',
  `script_content` varchar(5000) NOT NULL DEFAULT '' COMMENT '代码内容',
  `version` int(10) unsigned NOT NULL DEFAULT '1' COMMENT '数据版本号',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `updated_by` varchar(50) NOT NULL DEFAULT '' COMMENT '修改人',
  `created_by` varchar(50) NOT NULL DEFAULT '' COMMENT '创建人',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='动态代码配置表';


CREATE TABLE `bcp_event_source_config` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT COMMENT '自增ID',
  `source_name` varchar(20) NOT NULL DEFAULT '' COMMENT '数据源名称，英文名称',
  `source_desc` varchar(256) NOT NULL DEFAULT '' COMMENT '数据源的详细描述',
  `source_type` tinyint(6) NOT NULL DEFAULT '0' COMMENT '数据源类型，1:Otter 2:DataBus 3:MQ 4:日志 ...',
  `source_detail` varchar(1024) NOT NULL DEFAULT '' COMMENT '数据源详细配置，根据不同的类型有不同的格式',
  `app_code` varchar(64) NOT NULL DEFAULT '' COMMENT '应用code，鉴权和展示用',
  `sample_ratio` tinyint(4) unsigned NOT NULL DEFAULT '0' COMMENT '采样频率，1-100，表示1/100 - 100/100',
  `e_status` tinyint(4) unsigned NOT NULL DEFAULT '0' COMMENT '状态，-1:已删除， 0:关闭，1:开启',
  `create_user` varchar(64) NOT NULL DEFAULT '' COMMENT '创建用户',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_user` varchar(64) NOT NULL DEFAULT '' COMMENT '最近一次更新用户',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最近一次更新时间',
  `version` int(5) unsigned NOT NULL DEFAULT '0' COMMENT '版本号',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uniq_source_name` (`source_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='BCP事件源配置表';

CREATE TABLE `bcp_event_task_item` (
  `id` int(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `rule_id` int(11) NOT NULL DEFAULT '0' COMMENT '所属的校验规则ID',
  `msg_id` varchar(64) NOT NULL DEFAULT '' COMMENT '消息ID',
  `event_msg` varchar(2048) NOT NULL DEFAULT '' COMMENT '事件消息体',
  `expire_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '延迟执行时间',
  `e_status` tinyint(4) NOT NULL DEFAULT '0' COMMENT '任务执行状态，0:未处理，1:已处理',
  `receive_time` datetime NOT NULL DEFAULT '0000-00-00 00:00:00' COMMENT '事件接收时间',
  `version` int(5) unsigned NOT NULL DEFAULT '0' COMMENT '版本号',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='规则执行任务表';


alter table bcp_event_source_config add column `delay_type` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '延迟策略';
alter table bcp_event_source_config add column `delay_config_json` varchar(1024) NOT NULL DEFAULT '' COMMENT '延迟策略参数，根据不同的延迟策略而定';
alter table bcp_event_task_item add column `sharding` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '分片数';
alter table bcp_event_task_item add column `retrytime` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '重试次数';
alter table bcp_event_task_item add column `event_id` integer unsigned NOT NULL DEFAULT '0' COMMENT '事件源id';
alter table bcp_condition_config add column `zipped` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否压缩存储，1压缩，0未压缩';

-- v1.1
CREATE TABLE `bcp_check_rule_alert_config` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT COMMENT '编号，主键',
  `rule_id` int(11) NOT NULL DEFAULT '0' COMMENT '所规则id',
  `alert_type` int(11) NOT NULL DEFAULT '0' COMMENT '报警类型',
  `alert_config_json` varchar(1024) NOT NULL DEFAULT '' COMMENT '报警参数',
  `alert_text_format` varchar(1024) NOT NULL DEFAULT '' COMMENT '报警内容模版',
  `zipped` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否压缩存储，1压缩，0未压缩',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uniq_rule` (rule_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='规则报警配置表';


CREATE TABLE `bcp_check_rule_repair_config` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT COMMENT '编号，主键',
  `rule_id` int(11) NOT NULL DEFAULT '0' COMMENT '所规则id',
  `repair_type` int(11) NOT NULL DEFAULT '0' COMMENT '修复类型',
  `repair_config_json` varchar(1024) NOT NULL DEFAULT '' COMMENT '修复配置参数',
  `zipped` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否压缩存储，1压缩，0未压缩',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uniq_rule` (rule_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='规则报警配置表';

-- 12月7日
alter table bcp_event_source_config modify column detail_conf varchar(4096) not null default '' comment '详细配置';
alter table bcp_event_source_config add column `trigger_type` tinyint(3) unsigned NOT NULL DEFAULT '0' COMMENT '0 被动触发-业务检查，1 主动触发-巡检';
alter table bcp_event_source_config add index `idx_trigger_type` (`trigger_type`);
alter table bcp_dynamic_code_config add column `event_id` int unsigned NOT NULL DEFAULT '0' COMMENT '所属事件id';
alter table bcp_dynamic_code_config add index `idx_event_id` (`event_id`);