package com.atguigu.springcloud.service;

import org.springframework.stereotype.Component;

/**
 * @program: cloud2020
 * @description: 降级处理类
 * @author: Mr.Wang
 * @create: 2020-09-27 16:37
 **/
@Component
public class PaymentFallBackService implements PaymentHystrixService {
    @Override
    public String paymentInfo_OK(Integer id) {
        return "---------PaymentFallBackService fall back-paymentInfo_OK.,,";
    }
    @Override
    public String paymentInfo_TimeOut(Integer id) {
        return "---------PaymentFallBackService fall back-paymentInfo_TimeOut.,,";
    }
}
