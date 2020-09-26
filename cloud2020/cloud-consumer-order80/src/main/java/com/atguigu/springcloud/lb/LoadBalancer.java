package com.atguigu.springcloud.lb;

import org.springframework.cloud.client.ServiceInstance;

import java.util.List;

/**
 * @program: cloud2020
 * @description: 自顶义接口
 * @author: Mr.Wang
 * @create: 2020-09-26 16:39
 **/
public interface LoadBalancer {

    /**
     * 容器启动的时候，找到 指定的服务地址
     */
    ServiceInstance instances(List<ServiceInstance> serviceInstances);

}
