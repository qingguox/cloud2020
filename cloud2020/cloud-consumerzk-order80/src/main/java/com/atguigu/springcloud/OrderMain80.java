package com.atguigu.springcloud;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * @program: cloud2020
 * @description: zk提供者的消费放
 * @author: Mr.Wang
 * @create: 2020-09-26 10:54
 **/
@EnableDiscoveryClient      //该注解用于向使用consul或者Zookeeper作为注册中心时注
@SpringBootApplication
public class OrderMain80 {

    public static void main(String[] args) {
        SpringApplication.run(OrderMain80.class, args);
    }
}
