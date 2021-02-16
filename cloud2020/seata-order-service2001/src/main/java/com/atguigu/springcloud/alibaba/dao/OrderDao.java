package com.atguigu.springcloud.alibaba.dao;

import com.atguigu.springcloud.alibaba.domain.Order;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @program: cloud2020
 * @description:
 * @author: Mr.Wang
 * @create: 2020-10-05 14:04
 **/
@Mapper
public interface OrderDao {

    // 1. 创建订单
    void create(Order order);

    // 2. 修改订单状态
    void update(@Param("userId") Long userId, @Param("status") Integer status);
}
