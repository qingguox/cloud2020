package com.atguigu.springcloud.alibaba.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@MapperScan("com.atguigu.springcloud.alibaba.dao")
@Configuration
public class MybatisConfig {
}
