package com.atguigu.springcloud.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class ApplicationContextConfig {

    //往容器中添加一个RestTemplate
    //RestTemplate提供了多种便捷访问远程http访问的方法
    @Bean
    //@LoadBalanced      此时我们要自顶义自己的 负载均衡算法了，，所以注释掉 内置的
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

}
