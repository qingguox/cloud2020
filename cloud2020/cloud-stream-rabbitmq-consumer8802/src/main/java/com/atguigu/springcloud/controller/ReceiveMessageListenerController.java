package com.atguigu.springcloud.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

/**
 * @program: cloud2020
 * @description:
 * @author: Mr.Wang
 * @create: 2020-09-29 20:25
 **/
@Component
@EnableBinding(Sink.class)                // 绑定 通道 和 rabbitmq
public class ReceiveMessageListenerController {

    @Value("${server.port}")
    private String serverPort;

    @StreamListener(Sink.INPUT)                  // 消息监听器
    public void input(Message<String> message) {
        System.out.println("消费者1号，------> 接受到的消息是:  "+ message.getPayload() + "\t  端口为: " +serverPort);
    }
}
