package com.atguigu.springcloud.service.impl;

import com.atguigu.springcloud.service.IMessageProvider;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.MessageChannel;

import javax.annotation.Resource;
import java.util.UUID;

/**
 * @program: cloud2020
 * @description:
 * @author: Mr.Wang
 * @create: 2020-09-29 19:43
 **/
@EnableBinding(Source.class)    // 定义消息的推送管道，，其实就是声明 发送方，和 mq绑定，通过管道，因为数据会进入管道
public class MessageProviderImpl implements IMessageProvider {

    @Resource
    private MessageChannel output;   // 消息发送管道

    @Override
    public String send() {
        String serial = UUID.randomUUID().toString();
        output.send(MessageBuilder.withPayload(serial).build());
        System.out.println("******************** serial : " + serial);
        return null;
    }
}
