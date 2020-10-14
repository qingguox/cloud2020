# 尚硅谷2020最新版SpringCloud(H版&alibaba)框架开发教程全套完整版从入门到精通(大牛讲授spring cloud)

## 0. 视频地址

[视频教程](https://www.bilibili.com/video/av93813318)

[以下是一位同学 angenin十一 做的视频对应的博客，有详细的教程哦， 抱歉，这里暂时引用，如果angenin十一您觉得不合适在此处引用，我会及时删除!! ]()

[SpringCloud入门学习笔记（1-4基础入门，创建项目） ](https://blog.csdn.net/qq_36903261/article/details/106507150)

[SpringCloud入门学习笔记（8-9初级部分，服务调用【Ribbon与OpenFeign】）](https://blog.csdn.net/qq_36903261/article/details/106590923)

[SpringCloud入门学习笔记（10初级部分，断路器【Hystrix】）](https://blog.csdn.net/qq_36903261/article/details/106614077)

[SpringCloud入门学习笔记（11-12初级部分，网关【Gateway】）](https://blog.csdn.net/qq_36903261/article/details/106635918)

[SpringCloud入门学习笔记（13-14初级部分，服务配置【Config】与消息总线【Bus】）](https://blog.csdn.net/qq_36903261/article/details/106814648)

[SpringCloud入门学习笔记（15-16初级部分，消息驱动【Stream】与分布式请求链路追踪【Sleuth】）](https://blog.csdn.net/qq_36903261/article/details/106834598)

[SpringCloud入门学习笔记（17-18高级部分，服务注册和配置中心【Nacos】）](https://blog.csdn.net/qq_36903261/article/details/106835279)

[SpringCloud入门学习笔记（19高级部分，熔断与限流【Sentinel】）](https://blog.csdn.net/qq_36903261/article/details/106899215)

[SpringCloud入门学习笔记（20高级部分，处理分布式事务【Seata】）](https://blog.csdn.net/qq_36903261/article/details/107009285)

[SpringCloud入门学习笔记（21高级部分，雪花算法【snowflake】）](https://blog.csdn.net/qq_36903261/article/details/107045717)



## 1. 笔记
1) doc目录

2) 工具

下载[MindManager 2020](http://dwnld.mindjet.com/stubs/Builds/MindManager2020/20_0_334/64Bit/MindManager%202020.msi)

激活码
```text
2019: MP19-777-APE8-1162-BD8E
2020: MP20-345-DP56-7778-919A
```

3) github下载失败

[gitee导入github仓库](https://gitee.com/projects/import/github/status)

## 2. 启动前准备
### 2.1 数据库
* 执行sql脚本 doc/db2019.sql
* 修改数据库的配置

```text
cloud-provider-payment8001\src\main\resources\application.yml中
mysql的用户名和密码
```

### 2.2 修改hosts
找到C:\Windows\System32\drivers\etc路径下的hosts文件,添加

```text
127.0.0.1 eureka7001.com
127.0.0.1 eureka7002.com
```
### 2.3 修改zookeeper的地址

cloud-provider-payment8004\src\main\resources\application.yml

spring.cloud.zookeeper.connect-string=localhost:2181

## 3 软件
* Zookeeper
* consul
* JMeter
* RabbitMq
* [Seata-server](https://github.com/seata/seata/releases/download/v0.9.0/seata-server-0.9.0.zip)
* zipkin-server



@[TOC](Java后端学习资料-------大厂必备----(正在连载))

# 大数据开发的 hadoop体系及其 storm体系 还有spark 欢迎大家来学习

[https://github.com/1367379258/BigDataED](https://github.com/1367379258/BigDataED)

# 学习微服务的 Springcloud 笔记 和过程 大家可以看看 求star

[https://github.com/1367379258/cloud2020](https://github.com/1367379258/cloud2020)

# 还有java 面试复习  求star
[https://github.com/1367379258/MyNote/tree/master/java](https://github.com/1367379258/MyNote/tree/master/java)

# SSM源码分析 ，，大厂必备  求star
github： 

[https://github.com/1367379258/MyNote/tree/master/SSM%E6%BA%90%E7%A0%81](https://github.com/1367379258/MyNote/tree/master/SSM%E6%BA%90%E7%A0%81)

CSDN：              求专注和 点赞  哈哈哈
[https://blog.csdn.net/qq_41773026/article/details/109054852](https://blog.csdn.net/qq_41773026/article/details/109054852)

