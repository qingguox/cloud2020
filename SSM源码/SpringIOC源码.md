@[TOC](最详细的Spring IOC源码分析，首次读_嘻嘻)

原文转自 : [https://javadoop.com/post/spring-ioc](https://javadoop.com/post/spring-ioc)

Spring 最重要的概念就是 IOC 和 AOP，本文章适合于有一定的Spring使用基础的同学查看，阅读本文不会让你成为spring这方面的专家，但是可以增加你对Spring IOC 的工作原理和创建Bean 过程的理解。

本文章Spring 的版本是5.2.5.RELEASE， 原文是4.2.4.RELEASE。其实5 和 4 没有太大区别，不过在源码方面，还是有几处小的区别和优化。

本文只是对SpringIOC 源码解读，了解其工作原理，方面我们以后的工作和学习。

目录
- [引言](#引言)
    - [BeanFactory简介](#BeanFactory简介)
        - [1.1. 说一说自己对于 synchronized 关键字的了解](#11-说一说自己对于-synchronized-关键字的了解)
		
	- [1. synchronized 关键字](#1-synchronized-关键字)	
		
		
