@[TOC](最详细的Spring IOC源码分析，首次读_嘻嘻)

原文转自 : [https://javadoop.com/post/spring-ioc](https://javadoop.com/post/spring-ioc)

Spring 最重要的概念就是 IOC 和 AOP，本文章适合于有一定的Spring使用基础的同学查看，阅读本文不会让你成为spring这方面的专家，但是可以增加你对Spring IOC 的工作原理和创建Bean 过程的理解。

本文章Spring 的版本是5.2.5.RELEASE， 原文是4.2.4.RELEASE。其实5 和 4 没有太大区别，不过在源码方面，还是有几处小的区别和优化。

本文只是对SpringIOC 源码解读，了解其工作原理，方面我们以后的工作和学习。

目录
- [引言](#引言)
- [BeanFactory简介](#BeanFactory简介)
- [启动过程分析](#启动过程分析)	
	- [1. 创建 Bean 容器前的准备工作](#1-Bean 容器前的准备工作)			
	- [2. 创建 Bean 容器，加载并注册 Bean](#2-Bean 容器，加载并注册 Bean)	
		- [2.1 BeanDefinition接口定义](#2-1-BeanDefinition接口定义)	
		- [2.2 customizeBeanFactory](#2-2-customizeBeanFactory)	
		- [2.3 加载 Bean: loadBeanDefinitions](#2-3-loadBeanDefinitions)	
			- [2.3.1 doRegisterBeanDefinitions：](#2-3-1-doRegisterBeanDefinitions)	
			- [2.3.2 processBeanDefinition](#2-3-2-processBeanDefinition)	
			- [2.3.3 注册 Bean](#2-3-3-注册Bean)	
	- [3. Bean容器实例化完成后](#3-Bean容器实例化完成后)		
	- [4. 准备Bean容器:prepareBeanFactory](#4-准备Bean容器:prepareBeanFactory)	
	- [5. 初始化所有的singleton beans](#5-初始化所有的singleton beans)	
		- [5.1 preInstantiateSingletons](#5-1-启动过程分析)	
		- [5.2 getBean](#5-2-getBean)	
		- [5.3 创建Bean](#5-3-创建Bean)	
			- [5.3.1 创建 Bean 实例](#5-3-1-创建Bean实例)	
			- [5.3.2 bean 属性注入](#5-3-2-bean属性注入)	
			- [5.3.3 initializeBean](#5-3-3-initializeBean)	
		
		
		
		
    - [BeanFactory简介](#BeanFactory简介)
        - [1.1. 说一说自己对于 synchronized 关键字的了解](#11-说一说自己对于-synchronized-关键字的了解)
		
	- [启动过程分析](#启动过程分析)	
		





# 引言


# BeanFactory简介



# 启动过程分析








