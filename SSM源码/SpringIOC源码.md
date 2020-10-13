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
先看下最基本的启动 Spring 容器的例子：

```javascript
public static void main(String[] args) {
	ApplicationContext context = new ClassPathXmlApplicationContext("classpath:applicationfile.xml");
}
```
以上代码就可以利用配置文件来启动一个 Spring 容器了，请使用 maven 的小伙伴直接在 dependencies 5.2.5中加上以下依赖即可，
我比较反对那些不知道要添加什么依赖，然后把 Spring 的所有相关的东西都加进来的方式。
```javascript
 <properties>
	<project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
	<maven.compiler.source>12</maven.compiler.source>
	<maven.compiler.target>12</maven.compiler.target>
	<junit.version>4.12</junit.version>
	<lombok.version>1.18.10</lombok.version>
	<log4j.version>1.2.17</log4j.version>
	<mysql.version>8.0.18</mysql.version>
	<druid.version>1.1.16</druid.version>
	<mybatis.spring.boot.version>2.1.1</mybatis.spring.boot.version>
</properties>
<dependencies>
	<dependency>
		<groupId>org.springframework</groupId>
		<artifactId>spring-beans</artifactId>
		<version>5.2.5.RELEASE</version>
	</dependency>
	<dependency>
		<groupId>org.springframework</groupId>
		<artifactId>spring-core</artifactId>
		<version>5.2.5.RELEASE</version>
	</dependency>
	<dependency>
		<groupId>org.springframework</groupId>
		<artifactId>spring-context</artifactId>
		<version>5.2.5.RELEASE</version>
	</dependency>
	<dependency>
		<groupId>cglib</groupId>
		<artifactId>cglib</artifactId>
		<version>2.2.2</version>
	</dependency>
</dependencies>
```

>
	spring-context 会自动将 spring-core、spring-beans、spring-aop、spring-expression 这几个基础 jar 包带进来。


多说一句，很多开发者入门就直接接触的 SpringMVC，对 Spring 其实不是很了解，Spring 是渐进式的工具，并不具有很强的侵入性，它的模块也划分得很合理，即使你的应用不是 web 应用，或者之前完全没有使用到 Spring，而你就想用 Spring 的依赖注入这个功能，其实完全是可以的，它的引入不会对其他的组件产生冲突。
废话说完，我们继续。ApplicationContext context = new ClassPathXmlApplicationContext(...) 其实很好理解，从名字上就可以猜出一二，就是在 ClassPath 中寻找 xml 配置文件，根据 xml 文件内容来构建 ApplicationContext。当然，除了 ClassPathXmlApplicationContext 以外，我们也还有其他构建 ApplicationContext 的方案可供选择，我们先来看看大体的继承结构是怎么样的：


![图片](https://img-blog.csdnimg.cn/20201013202125550.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzQxNzczMDI2,size_16,color_FFFFFF,t_70#pic_center)

>
	读者可以大致看一下类名，源码分析的时候不至于找不着看哪个类，因为 Spring 为了适应各种使用场景，提供的各个接口都可能有很多的实现类。对于我们来说，
	就是揪着一个完整的分支看完。当然，读本文的时候读者也不必太担心，每个代码块分析的时候，我都会告诉读者我们在说哪个类第几行。

我们可以看到，ClassPathXmlApplicationContext 兜兜转转了好久才到 ApplicationContext 接口，同样的，我们也可以使用绿颜色的 FileSystemXmlApplicationContext 和 AnnotationConfigApplicationContext 这两个类。

**FileSystemXmlApplicationContext** 的构造函数需要一个 xml 配置文件在系统中的路径，其他和 ClassPathXmlApplicationContext 基本上一样。

**AnnotationConfigApplicationContext** 是基于注解来使用的，它不需要配置文件，采用 java 配置类和各种注解来配置，是比较简单的方式，也是大势所趋吧。

不过本文旨在帮助大家理解整个构建流程，所以决定使用 ClassPathXmlApplicationContext 进行分析。

我们先来一个简单的例子来看看怎么实例化 ApplicationContext。

首先，定义一个类：

```javascript
public class Video {
	private int id;
	private String title;
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
}
```

# BeanFactory简介



# 启动过程分析

## 1-Bean容器前的准备工作

## 2-Bean容器,加载并注册Bean

### 2-1-BeanDefinition接口定义
 
### 2-2-customizeBeanFactory

### 2-3-loadBeanDefinitions

#### 2-3-1-doRegisterBeanDefinitions

#### 2-3-2-processBeanDefinition

#### 2-3-3-注册Bean

## 3-Bean容器实例化完成后

## 4-准备Bean容器prepareBeanFactory

## 5-初始化所有的singleton-beans

### 5-1-启动过程分析

### 5-2-getBean

### 5-3-创建Bean

#### 5-3-1-创建Bean实例

#### 5-3-2-bean属性注入

#### 5-3-3-initializeBean



	
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

