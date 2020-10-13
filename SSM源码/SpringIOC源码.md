@[TOC](最详细的Spring IOC源码分析，首次读_嘻嘻)

原文转自 : [https://javadoop.com/post/spring-ioc](https://javadoop.com/post/spring-ioc)

Spring 最重要的概念就是 IOC 和 AOP，本文章适合于有一定的Spring使用基础的同学查看，阅读本文不会让你成为spring这方面的专家，但是可以增加你对Spring IOC 的工作原理和创建Bean 过程的理解。

本文章Spring 的版本是5.2.5.RELEASE， 原文是4.2.4.RELEASE。其实5 和 4 没有太大区别，不过在源码方面，还是有几处小的区别和优化。

本文只是对SpringIOC 源码解读，了解其工作原理，方面我们以后的工作和学习。

目录
- [引言](#引言)
- [BeanFactory简介](#BeanFactory简介)
- [启动过程分析](#启动过程分析)	
	- [1. 创建Bean容器前的准备工作](#1-Bean容器前的准备工作)	
	- [2. 创建Bean容器,加载并注册Bean](#2-Bean容器,加载并注册Bean)	
		- [2.1 BeanDefinition接口定义](#2-1-BeanDefinition接口定义)	
		- [2.2 customizeBeanFactory](#2-2-customizeBeanFactory)		
		- [2.3 加载Bean:loadBeanDefinitions](#2-3-loadBeanDefinitions)			
			- [2.3.1 doRegisterBeanDefinitions：](#2-3-1-doRegisterBeanDefinitions)				
			- [2.3.2 processBeanDefinition](#2-3-2-processBeanDefinition)				
			- [2.3.3 注册 Bean](#2-3-3-注册Bean)				
	- [3. Bean容器实例化完成后](#3-Bean容器实例化完成后)			
	- [4. 准备Bean容器:prepareBeanFactory](#4-准备Bean容器prepareBeanFactory)		
	- [5. 初始化所有的singleton-beans](#5-初始化所有的singleton-beans)		
		- [5.1 preInstantiateSingletons](#5-1-启动过程分析)			
		- [5.2 getBean](#5-2-getBean)			
		- [5.3 创建Bean](#5-3-创建Bean)	
			- [5.3.1 创建 Bean 实例](#5-3-1-创建Bean实例)				
			- [5.3.2 bean 属性注入](#5-3-2-bean属性注入)				
			- [5.3.3 initializeBean](#5-3-3-initializeBean)	

- [附录](#附录)
	- [id和name](#id和name)	
	- [配置是否允许Bean覆盖、是否允许循环依赖](#配置是否允许Bean覆盖、是否允许循环依赖)	
	- [profile](#profile)	
	- [工厂模式生成Bean](#工厂模式生成Bean)	
	- [FactoryBean](#FactoryBean)	
	- [初始化Bean的回调](#初始化Bean的回调)	
	- [销毁Bean的回调](#销毁Bean的回调)	
	- [ConversionService](#ConversionService)	
	- [Bean继承](#Bean继承)	
	- [方法注入](#方法注入)	
		- [lookup-method](#lookup-method)		
		- [replaced-method](#replaced-method)		
	- [BeanPostProcessor](#BeanPostProcessor)	
- [总结](#总结)



# 引言


# BeanFactory简介



# 启动过程分析

## 1-Bean 容器前的准备工作

## 2-Bean 容器，加载并注册 Bean

###
 
###

###

####

####

####

##

##

##

###

###

###

####

####

####



	
# 附录

# id 和 name

# 配置是否允许 Bean 覆盖、是否允许循环依赖

# profile

# 工厂模式生成Bean

# FactoryBean

# 初始化Bean的回调

# 销毁Bean的回调



# ConversionService

# Bean继承

# 方法注入

## lookup-method

## replaced-method

# BeanPostProcessor		
		
# 总结

