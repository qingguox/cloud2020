package com.atguigu.springcloud.service;

import cn.hutool.core.util.IdUtil;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.concurrent.TimeUnit;

/**
 * @program: cloud2020
 * @description: 服务
 * @author: Mr.Wang
 * @create: 2020-09-27 10:43
 **/
@Service
public class PaymentService {

    /**
      * 模拟正确访问
     */
    public String paymentInfo_OK(Integer id) {
        return "线程池 ： "+ Thread.currentThread().getName() + " paymentInfo_OK : id " + id +"\t"+"O(n_n)O哈哈";
    }

    @HystrixCommand(fallbackMethod = "paymentInfo_TimeOutHandler", commandProperties = {
        @HystrixProperty(name="execution.isolation.thread.timeoutInMilliseconds", value = "5000")
    })
    public String paymentInfo_TimeOut(Integer id) {
        int timeout = 3;
        // 设置五秒 肯定超时了
        // int age = 10 / 0;
       try { TimeUnit.SECONDS.sleep(timeout); }catch (InterruptedException e) {e.printStackTrace();}
        return "线程池 ： "+ Thread.currentThread().getName() + " paymentInfo_TimeOut : id " + id +"\t"+"O(n_n)O哈哈 耗时:" + timeout +"ms秒";
    }

    public String paymentInfo_TimeOutHandler(Integer id) {
        return "线程池 ： "+ Thread.currentThread().getName() + " 系统超时或者运行异常:  id " +
                 + id +"\t"+" O哭了哭了 ";
    }

    // 服务熔断
    //服务熔断
    @HystrixCommand(fallbackMethod = "paymentCircuitBreaker_fallback",commandProperties = {
            @HystrixProperty(name = "circuitBreaker.enabled",value = "true"),  //是否开启断路器
            @HystrixProperty(name = "circuitBreaker.requestVolumeThreshold",value = "10"),   //请求次数
            @HystrixProperty(name = "circuitBreaker.sleepWindowInMilliseconds",value = "10000"),  //时间范围
            @HystrixProperty(name = "circuitBreaker.errorThresholdPercentage",value = "60"), //失败率达到多少后跳闸
    })
    public String paymentCircuitBreaker(@PathVariable("id") Integer id){
        if (id < 0){
            throw new RuntimeException("*****id 不能负数");
        }
        String serialNumber = IdUtil.simpleUUID();

        return Thread.currentThread().getName()+"\t"+"调用成功,流水号："+serialNumber;
    }
    public String paymentCircuitBreaker_fallback(@PathVariable("id") Integer id) {
        return "id 不能负数，请稍候再试,(┬＿┬)/~~     id: " + id;
    }


}
