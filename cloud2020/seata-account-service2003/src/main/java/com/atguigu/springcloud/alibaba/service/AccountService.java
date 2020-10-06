package com.atguigu.springcloud.alibaba.service;

import java.math.BigDecimal;

public interface AccountService {
    // 减 用户余额
    void decrease(Long userId, BigDecimal money) throws InterruptedException;
}
