package com.atguigu.springcloud.alibaba.service;

public interface StorageService {

    // 减库存
    void decrease(Long productId, Integer count);
}
