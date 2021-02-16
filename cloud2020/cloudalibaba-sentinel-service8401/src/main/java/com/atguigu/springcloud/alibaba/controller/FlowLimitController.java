package com.atguigu.springcloud.alibaba.controller;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;

import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class FlowLimitController {
    @GetMapping("/testA")
    public String testA() throws InterruptedException {

        return "------testA";
    }

    @GetMapping("/testB")
    public String testB() {
        log.info(Thread.currentThread().getName() + "\t " + ".............testB");
        return "------testB";
    }

    @GetMapping("/testD")
    public String testD() {
        //        try { TimeUnit.SECONDS.sleep(1); } catch (InterruptedException e) { e.printStackTrace(); }
        //        log.info("testD 测试RT 秒级");
        //        log.info("testD  异常比例 秒级");
        //        int age = 10 / 0;

        log.info("testD 测试异常数 分钟级");
        int age = 10 / 0;
        return "------testD";
    }

    @GetMapping("/testHotKey")
    @SentinelResource(value = "testHotKey", blockHandler = "deal_testHotKey")         // value是 在sentinenl控制台中需要的东西
    public String testKey(@RequestParam(value = "p1", required = false) String p1,
            @RequestParam(value = "p2", required = false) String p2) {
        return "------------testHotKey";
    }

    public String deal_testHotKey(String p1, String p2, BlockException exception) {
        return "-------------------deal_testHotKey";    //  Blocked by Sentinel (flow limiting)， 和这是默认的
    }
}
 
 