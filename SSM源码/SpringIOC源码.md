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

接下来，我们在 resources 目录新建一个配置文件，文件名随意，通常叫 application.xml 或 application-xxx.xml就可以了：

```javascript
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
        http://www.springframework.org/schema/beans/spring-beans.xsd">
    <bean id="video" class="com.xlg.springSource.beans.Video">
        <property name="id" value="9"></property>
        <property name="title" value="Spring5.X课程"></property>
    </bean>
</beans>
```

这样，我们就可以跑起来了：

```javascript
public class BeansSource {
    public static void main(String[] args) {
        ApplicationContext con = new ClassPathXmlApplicationContext("applicationContext.xml");
        Video video = (Video) con.getBean("video");
        System.out.println(video.getTitle() + video.getId());
    }
}
```

以上例子很简单，不过也够引出本文的主题了，就是怎么样通过配置文件来启动 Spring 的 ApplicationContext？也就是我们今天要分析的 IOC 的核心了.
ApplicationContext 启动过程中，会负责创建实例 Bean，往各个 Bean 中注入依赖等。

# BeanFactory简介


初学者可别以为我之前说那么多和 BeanFactory 无关，前面说的 ApplicationContext 其实就是一个 BeanFactory。
我们来看下和 BeanFactory 接口相关的主要的继承结构：

![beanFactory](https://img-blog.csdnimg.cn/20201013203056997.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzQxNzczMDI2,size_16,color_FFFFFF,t_70#pic_center)

我想，大家看完这个图以后，可能就不是很开心了。ApplicationContext 往下的继承结构前面一张图说过了，这里就不重复了。这张图呢，背下来肯定是不需要的，有几个重点和大家说明下就好。

* ApplicationContext 继承了 ListableBeanFactory，这个 Listable 的意思就是，通过这个接口，我们可以获取多个 Bean，最顶层 BeanFactory 接口的方法都是获取单个 Bean 的。
* ApplicationContext 继承了 HierarchicalBeanFactory，Hierarchical 单词本身已经能说明问题了，也就是说我们可以在应用中起多个 BeanFactory，然后可以将各个 BeanFactory 设置为父子关系。
* AutowireCapableBeanFactory 这个名字中的 Autowire 大家都非常熟悉，它就是用来自动装配 Bean 用的，但是仔细看上图，ApplicationContext 并没有继承它，不过不用担心，不使用继承，不代表不可以使用组合，如果你看到 ApplicationContext 接口定义中的最后一个方法 getAutowireCapableBeanFactory() 就知道了。
* ConfigurableListableBeanFactory 也是一个特殊的接口，看图，特殊之处在于它继承了第二层所有的三个接口，而 ApplicationContext 没有。这点之后会用到。
* 请先不用花时间在其他的接口和类上，先理解我说的这几点就可以了。

然后，请读者打开编辑器，翻一下 BeanFactory、ListableBeanFactory、HierarchicalBeanFactory、AutowireCapableBeanFactory、ApplicationContext 这几个接口的代码，大概看一下各个接口中的方法，大家心里要有底，限于篇幅，我就不贴代码介绍了。


# 启动过程分析
下面将会是冗长的代码分析，请读者先喝个水。记住，一定要在电脑中打开源码，不然纯看是很累的。

第一步，我们肯定要从 ClassPathXmlApplicationContext 的构造方法说起。

```javascript
public class ClassPathXmlApplicationContext extends AbstractXmlApplicationContext {
  private Resource[] configResources;

  // 如果已经有 ApplicationContext 并需要配置成父子关系，那么调用这个构造方法
  public ClassPathXmlApplicationContext(ApplicationContext parent) {
	super(parent);
  }
  ...
  public ClassPathXmlApplicationContext(String[] configLocations, boolean refresh, ApplicationContext parent)
	  throws BeansException {

	super(parent);
	// 根据提供的路径，处理成配置文件数组，(以分号. 逗号. 空格. tab. 换行符分割)
	setConfigLocations(configLocations);
	if (refresh) {
	  refresh(); // 核心方法
	}
  }
	...
}

```

接下来，就是refresh, 这里简单说一下为什么是refresh()， 而不是init()这种名字的方法。从大多数博客来看都是这样说明的:
因为ApplicationContext 建立起来以后，其实我们是可以通过调用refresh() 这个方法重建的, refresh() 会将原来的ApplciationContext销毁
然后再重新执行一次初始化操作。

往下看，refresh()方法里面调用了那么多方法，就知道肯定不简单了，请读者先看个大概，细节之后会详细说明。
```javascript
	@Override
public void refresh() throws BeansException, IllegalStateException {

	// 来个同步锁，不然refresh()还么结束，你又来个启动或者销毁容器的操作，那不就乱套了
	synchronized (this.startupShutdownMonitor) {
	
		// 准备工作，记录下容器的启动时间,并标记"已启动" 状态，处理配置文件中的占位符
		prepareRefresh();

		
		//  这步比较关键，这步完成后，配置文件就会解析成一个个Bean定义，注册到BeanFactory中
		// 当然，这里说的Bean还没有初始化，只是配置信息都提取出来了，
		// 注册也只是将这些信息都保存到了注册中心(说到底核心是一个 beadName->beanDefinition的 map)
		ConfigurableListableBeanFactory beanFactory = obtainFreshBeanFactory();

		// 设置BeanFactory 的类加载器，添加几个BeanPostProcessor, 手动注册几个特殊的bean
		// 这块待会说
		prepareBeanFactory(beanFactory);

		try {
			// 【这里需要知道BeanFactoryPostProcessor 这个知识点， bean如果实现了这个借口
			//  那么在容器初始化以后，Spring会负责调用里面的postProcessBeanFactory方法。】
		
			//  这里是提供给子类的扩展点，到这里的时候，所有的bean都加载，注册完成了，但是没有进行初始化
			//  具体的子类可以在这步的时候添加一些特殊的BeanFactoryPostProcessor的实现类或者其他什么事情
			postProcessBeanFactory(beanFactory);

			// 直接执行了，，，调用beanFactoryPostProcessor 各个实现类的postProcessBeanFactory(factory)方法
			invokeBeanFactoryPostProcessors(beanFactory);


	
			// 注册BeanPostProcessor的实现类，注意看和BeanFactoryPostProcessor的区别
			// 此接口两个方法： postProcessBeforeInitialization 和postProcessorAfterInitialization
			// 两个方法分别在 Bean初始化之前和初始化之后得到执行。 注意： 到这里，Bean还没有初始化
			registerBeanPostProcessors(beanFactory);

			// 初始化当前ApplicationContext的 MessageSource, 国际化这里就不展开了，，不然是没完没了
			initMessageSource();

			// 初始化ApplicationContext的 事件广播器，这里也不展开了
			initApplicationEventMulticaster();


			// 从方法名就可以知道，典型的模板方法(钩子方法)， 
			// 具体的子类可以在这里初始化一些特殊的 Bean (在初始化 singleton beans之前)
			onRefresh();

			// 注册事件监听器，监听器需要实现 ApplicationListener接口。 这并不是我们的重点
			registerListeners();


			// 重点  重点 重点 
			// 初始化所有的 singleton beans
			// ( lazy-init 的除外)
			finishBeanFactoryInitialization(beanFactory);

			// 最后，广播事件， ApplicationContext 初始化完成 
			finishRefresh();
		}

		catch (BeansException ex) {
			if (logger.isWarnEnabled()) {
				logger.warn("Exception encountered during context initialization - " +
						"cancelling refresh attempt: " + ex);
			}

			// Destroy already created singletons to avoid dangling resources.
			// 销毁已经初始化的singleton 的Beans， 以免有些bean 会一直占用资源 
			destroyBeans();

			// Reset 'active' flag.
			cancelRefresh(ex);

			// 把异常向外抛出
			throw ex;
		}

		finally {
			// Reset common introspection caches in Spring's core, since we
			// might not ever need metadata for singleton beans anymore...
			resetCommonCaches();
		}
	}
	
```

下面，我们开始一步步来肢解这个 refresh() 方法。


## 1-Bean容器前的准备工作
	
	这个比较简单，直接看代码中的几个注释即可。
```javascript
protected void prepareRefresh() {
	//记录 启动时间 
	// 将 active 属性设置为 true， closed 属性设置为false，它们都是 AtomicBoolean 类型
	this.startupDate = System.currentTimeMillis();
	this.closed.set(false);
	this.active.set(true);

	if (logger.isDebugEnabled()) {
		if (logger.isTraceEnabled()) {
			logger.trace("Refreshing " + this);
		}
		else {
			logger.debug("Refreshing " + getDisplayName());
		}
	}

	// Initialize any placeholder property sources in the context environment.
	initPropertySources();

	// 校验 xml 配置文件
	getEnvironment().validateRequiredProperties();

	if (this.earlyApplicationListeners == null) {
		this.earlyApplicationListeners = new LinkedHashSet<>(this.applicationListeners);
	}
	else {
		this.applicationListeners.clear();
		this.applicationListeners.addAll(this.earlyApplicationListeners);
	}
	this.earlyApplicationEvents = new LinkedHashSet<>();
}	
```

## 2-Bean容器,加载并注册Bean

	我们回到refresh() 方法中的下一行，obtainFreshBeanFactory() 方法
	注意：这个方法是全文最重要的部分之一，这里将会初始化 BeanFactory、加载 Bean、注册 Bean 等等。
	    
	当然，这步结束后，Bean 并没有完成初始化。这里指的是 Bean 实例并未在这一步生成。
	
// AbstractApplicationContext.java
```javascript
protected ConfigurableListableBeanFactory obtainFreshBeanFactory() {
	// 关闭旧的 BeanFactory(如果有)，创建新的BeanFactory， 加载bean定义，注册bean等等
	refreshBeanFactory();
	
	// 返回刚刚创建的 BeanFactory
	return getBeanFactory();
}
// getBeanFactory
synchronized (this.beanFactoryMonitor) {
	if (this.beanFactory == null) {
		throw new IllegalStateException("BeanFactory not initialized or already closed - " +
				"call 'refresh' before accessing beans via the ApplicationContext");
	}
	return this.beanFactory;
}
```

// AbstractRefreshableApplicationContext.java 120
refreshBeanFactory
```javascript
@Override
protected final void refreshBeanFactory() throws BeansException {
	// 如果 ApplicationContext 中已经加载过 BeanFactory 了，销毁所有Bean，关闭BeanFactory
	// 注意, 应用中 BeanFactory 本来就是可以多个的，这里可不是说应用全局是否有BeanFactory，而是当前
	// ApplicationContext 是否有 BeanFactory
	if (hasBeanFactory()) {
		destroyBeans();
		closeBeanFactory();
	}
	try {
		// 初始化一个 DefaultListableBeanFactory ， 为什么用这个，一会说。
		DefaultListableBeanFactory beanFactory = createBeanFactory();
		// 用于 BeanFactory的 序列化 ， 我想部分人应该用不到
		beanFactory.setSerializationId(getId());
		
		
		// 下面这两个方法非常重要，别跟丢了，具体细节之后说
		// 设置BeanFactory 的两个配置属性；是否允许 Bean 覆盖，是否允许循环引用
		customizeBeanFactory(beanFactory);
		
		
		// 加载 Bean 到 BeanFactory 中
		loadBeanDefinitions(beanFactory);
		synchronized (this.beanFactoryMonitor) {
			this.beanFactory = beanFactory;
		}
	}
	catch (IOException ex) {
		throw new ApplicationContextException("I/O error parsing bean definition source for " + getDisplayName(), ex);
	}
}
```
>
	看到这里的时候，我觉得读者就应该站在高处看ApplicationContext 了， ApplicationContext 继承自 BeanFactory，
	但是 它不应该被 理解为BeanFactory的实现类，而是说其内部持有一个实例化的BeanFactory (DefaultListableBeanFactory).
	以后所有的BeanFactory 相关的操作其实是委托给这个实例来处理的。

我们说说为什么选择实例化 DefaultListableBeanFactory ？前面我们说了有个很重要的接口 ConfigurableListableBeanFactory，它实现了 BeanFactory 下面一层的所有三个接口，我把之前的继承图再拿过来大家再仔细看一下：

![beanFactory](https://img-blog.csdnimg.cn/20201013203056997.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzQxNzczMDI2,size_16,color_FFFFFF,t_70#pic_center)

我们可以看到 ConfigurableListableBeanFactory 只有一个实现类 DefaultListableBeanFactory，而且实现类 DefaultListableBeanFactory 还通过实现右边的 AbstractAutowireCapableBeanFactory 通吃了右路。所以结论就是，最底下这个家伙 DefaultListableBeanFactory 基本上是最牛的 BeanFactory 了，这也是为什么这边会使用这个类来实例化的原因。

在继续往下之前，我们需要先了解 BeanDefinition。我们说** BeanFactory 是 Bean 容器，那么 Bean 又是什么呢？**

这里的 BeanDefinition 就是我们所说的 Spring 的 Bean，我们自己定义的各个 Bean 其实会转换成一个个 BeanDefinition 存在于 Spring 的 BeanFactory 中。
所以，如果有人问你 Bean 是什么的时候，你要知道 Bean 在代码层面上是 BeanDefinition 的实例

>	
BeanDefinition 中保存了我们的 Bean 信息，比如这个 Bean 指向的是哪个类、是否是单例的、是否懒加载、这个 Bean 依赖了哪些 Bean 等等。	


### 2-1-BeanDefinition接口定义
我们来看下 BeanDefinition 的接口定义：

```javascript
public interface BeanDefinition extends AttributeAccessor, BeanMetadataElement {

   // 我们可以看到，默认只提供 sington 和 prototype 两种，
   // 很多读者可能知道还有 request, session, globalSession, application, websocket 这几种，
   // 不过，它们属于基于 web 的扩展。
   String SCOPE_SINGLETON = ConfigurableBeanFactory.SCOPE_SINGLETON;
   String SCOPE_PROTOTYPE = ConfigurableBeanFactory.SCOPE_PROTOTYPE;

   // 比较不重要，直接跳过吧
   int ROLE_APPLICATION = 0;
   int ROLE_SUPPORT = 1;
   int ROLE_INFRASTRUCTURE = 2;

   // 设置父 Bean，这里涉及到 bean 继承，不是 java 继承。请参见附录的详细介绍
   // 一句话就是：继承父 Bean 的配置信息而已
   void setParentName(String parentName);

   // 获取父 Bean
   String getParentName();

   // 设置 Bean 的类名称，将来是要通过反射来生成实例的
   void setBeanClassName(String beanClassName);

   // 获取 Bean 的类名称
   String getBeanClassName();


   // 设置 bean 的 scope
   void setScope(String scope);

   String getScope();

   // 设置是否懒加载
   void setLazyInit(boolean lazyInit);

   boolean isLazyInit();

   // 设置该 Bean 依赖的所有的 Bean，注意，这里的依赖不是指属性依赖(如 @Autowire 标记的)，
   // 是 depends-on="" 属性设置的值。
   void setDependsOn(String... dependsOn);

   // 返回该 Bean 的所有依赖
   String[] getDependsOn();

   // 设置该 Bean 是否可以注入到其他 Bean 中，只对根据类型注入有效，
   // 如果根据名称注入，即使这边设置了 false，也是可以的
   void setAutowireCandidate(boolean autowireCandidate);

   // 该 Bean 是否可以注入到其他 Bean 中
   boolean isAutowireCandidate();

   // 主要的。同一接口的多个实现，如果不指定名字的话，Spring 会优先选择设置 primary 为 true 的 bean
   void setPrimary(boolean primary);

   // 是否是 primary 的
   boolean isPrimary();

   // 如果该 Bean 采用工厂方法生成，指定工厂名称。对工厂不熟悉的读者，请参加附录
   // 一句话就是：有些实例不是用反射生成的，而是用工厂模式生成的
   void setFactoryBeanName(String factoryBeanName);
   // 获取工厂名称
   String getFactoryBeanName();
   // 指定工厂类中的 工厂方法名称
   void setFactoryMethodName(String factoryMethodName);
   // 获取工厂类中的 工厂方法名称
   String getFactoryMethodName();

   // 构造器参数
   ConstructorArgumentValues getConstructorArgumentValues();

   // Bean 中的属性值，后面给 bean 注入属性值的时候会说到
   MutablePropertyValues getPropertyValues();

   // 是否 singleton
   boolean isSingleton();

   // 是否 prototype
   boolean isPrototype();

   // 如果这个 Bean 是被设置为 abstract，那么不能实例化，
   // 常用于作为 父bean 用于继承，其实也很少用......
   boolean isAbstract();

   int getRole();
   String getDescription();
   String getResourceDescription();
   BeanDefinition getOriginatingBeanDefinition();
}
```

>
这个 BeanDefinition 其实已经包含很多的信息了，暂时不清楚所有的方法对应什么东西没关系，希望看完本文后读者
可以彻底搞清楚里面的所有东西。

>
这里接口虽然那么多，但是没有类似 getInstance() 这种方法来获取我们定义的类的实例，真正的我们定义的类生成的实例到哪里去了呢？别着急，这个要很后面才能讲到。

有了 BeanDefinition 的概念以后，我们再往下看 refreshBeanFactory() 方法中的剩余部分：
```javascript
customizeBeanFactory(beanFactory);
loadBeanDefinitions(beanFactory);
```

虽然只有两个方法，但路还很长啊。。。

### 2-2-customizeBeanFactory

customizeBeanFactory

customizeBeanFactory(beanFactory) 比较简单，就是配置是否允许 BeanDefinition 覆盖、是否允许循环引用。
```javascript
protected void customizeBeanFactory(DefaultListableBeanFactory beanFactory) {
	if (this.allowBeanDefinitionOverriding != null) {
		// 是否允许 Bean 定义覆盖
		beanFactory.setAllowBeanDefinitionOverriding(this.allowBeanDefinitionOverriding);
	}
	if (this.allowCircularReferences != null) {
		// 是否允许 Bean 间的循环依赖
		beanFactory.setAllowCircularReferences(this.allowCircularReferences);
	}
}
```

BeanDefinition 的覆盖问题大家也许会碰到，就是在配置文件中定义 bean 时使用了相同的 id 或 name，默认情况下，allowBeanDefinitionOverriding 属性为 null，如果在同一配置文件中重复了，会抛错，但是如果不是同一配置文件中，会发生覆盖。

循环引用也很好理解：A 依赖 B，而 B 依赖 A。或 A 依赖 B，B 依赖 C，而 C 依赖 A。

默认情况下，Spring 允许循环依赖，当然如果你在 A 的构造方法中依赖 B，在 B 的构造方法中依赖 A 是不行的。

至于这两个属性怎么配置？我在附录中进行了介绍，尤其对于覆盖问题，很多人都希望禁止出现 Bean 覆盖，可是 Spring 默认是不同文件的时候可以覆盖的。

之后的源码中还会出现这两个属性，读者有个印象就可以了。


### 2-3-loadBeanDefinitions

接下来是最重要的 loadBeanDefinitions(beanFactory) 方法了，这个方法将根据配置，加载各个 Bean，然后放到 BeanFactory 中。

读取配置的操作在 XmlBeanDefinitionReader 中，其负责加载配置、解析。

// AbstractXmlApplicationContext.java 80
```javascript
///**  我们可以看到，此方法将通过一个 XmlBeanDefinitionReader 实例来加载各个Bean 。*/
Override      
protected void loadBeanDefinitions(DefaultListableBeanFactory beanFactory) throws BeansException, IOException {
	// 给这个 BeanFactory 实例化一个 XmlBeanDefinitionReader
	XmlBeanDefinitionReader beanDefinitionReader = new XmlBeanDefinitionReader(beanFactory);

	// Configure the bean definition reader with this context's
	// resource loading environment.
	beanDefinitionReader.setEnvironment(this.getEnvironment());
	beanDefinitionReader.setResourceLoader(this);
	beanDefinitionReader.setEntityResolver(new ResourceEntityResolver(this));

	//  初始化 BeanDefinitionReader, 其实这个是提供给子类覆写的，
	//  我看了一下，没有类覆写这个方法，我们姑且当做不重要吧
	initBeanDefinitionReader(beanDefinitionReader);
	// 重点来了，，继续向下
	loadBeanDefinitions(beanDefinitionReader);
}
```
现在还在这个类中，接下来用刚刚初始化的 Reader 开始来加载 xml 配置，这块代码读者可以选择性跳过，不是很重要。也就是说，下面这个代码块，读者可以很轻松地略过。
loadBeanDefinitions
```javascript
protected void loadBeanDefinitions(XmlBeanDefinitionReader reader) throws BeansException, IOException {
   Resource[] configResources = getConfigResources();
   if (configResources != null) {
	  // 往下看
	  reader.loadBeanDefinitions(configResources);
   }
   String[] configLocations = getConfigLocations();
   if (configLocations != null) {
	  // 2
	  reader.loadBeanDefinitions(configLocations);
   }
}
```

// 上面虽然有两个分支，不过第二个分支很快通过解析路径转换为 Resource 以后也会进到这里
```
@Override
public int loadBeanDefinitions(Resource... resources) throws BeanDefinitionStoreException {
   Assert.notNull(resources, "Resource array must not be null");
   int counter = 0;
   // 注意这里是个 for 循环，也就是每个文件是一个 resource
   for (Resource resource : resources) {
	  // 继续往下看
	  counter += loadBeanDefinitions(resource);
   }
   // 最后返回 counter，表示总共加载了多少的 BeanDefinition
   return counter;
}

// XmlBeanDefinitionReader 303
@Override
public int loadBeanDefinitions(Resource resource) throws BeanDefinitionStoreException {
   return loadBeanDefinitions(new EncodedResource(resource));
}

// XmlBeanDefinitionReader 314
public int loadBeanDefinitions(EncodedResource encodedResource) throws BeanDefinitionStoreException {
   Assert.notNull(encodedResource, "EncodedResource must not be null");
   if (logger.isInfoEnabled()) {
	  logger.info("Loading XML bean definitions from " + encodedResource.getResource());
   }
   // 用一个 ThreadLocal 来存放配置文件资源
   Set<EncodedResource> currentResources = this.resourcesCurrentlyBeingLoaded.get();
   if (currentResources == null) {
	  currentResources = new HashSet<EncodedResource>(4);
	  this.resourcesCurrentlyBeingLoaded.set(currentResources);
   }
   if (!currentResources.add(encodedResource)) {
	  throw new BeanDefinitionStoreException(
			"Detected cyclic loading of " + encodedResource + " - check your import definitions!");
   }
   try {
	  InputStream inputStream = encodedResource.getResource().getInputStream();
	  try {
		 InputSource inputSource = new InputSource(inputStream);
		 if (encodedResource.getEncoding() != null) {
			inputSource.setEncoding(encodedResource.getEncoding());
		 }
		 // 核心部分是这里，往下面看
		 return doLoadBeanDefinitions(inputSource, encodedResource.getResource());
	  }
	  finally {
		 inputStream.close();
	  }
   }
   catch (IOException ex) {
	  throw new BeanDefinitionStoreException(
			"IOException parsing XML document from " + encodedResource.getResource(), ex);
   }
   finally {
	  currentResources.remove(encodedResource);
	  if (currentResources.isEmpty()) {
		 this.resourcesCurrentlyBeingLoaded.remove();
	  }
   }
}

// 还在这个文件中，第 388 行
protected int doLoadBeanDefinitions(InputSource inputSource, Resource resource)
	  throws BeanDefinitionStoreException {
   try {
	  // 这里就不看了，将 xml 文件转换为 Document 对象
	  Document doc = doLoadDocument(inputSource, resource);
	  // 继续
	  return registerBeanDefinitions(doc, resource);
   }
   catch (...
}
// 还在这个文件中，第 505 行
// 返回值：返回从当前配置文件加载了多少数量的 Bean
public int registerBeanDefinitions(Document doc, Resource resource) throws BeanDefinitionStoreException {
   BeanDefinitionDocumentReader documentReader = createBeanDefinitionDocumentReader();
   int countBefore = getRegistry().getBeanDefinitionCount();
   // 这里
   documentReader.registerBeanDefinitions(doc, createReaderContext(resource));
   return getRegistry().getBeanDefinitionCount() - countBefore;
}
// DefaultBeanDefinitionDocumentReader 90
@Override
public void registerBeanDefinitions(Document doc, XmlReaderContext readerContext) {
   this.readerContext = readerContext;
   logger.debug("Loading bean definitions");
   Element root = doc.getDocumentElement();
   // 从 xml 根节点开始解析文件
   doRegisterBeanDefinitions(root);
}         
```	

经过漫长的链路，一个配置文件终于转换为一颗 DOM 树了，注意，这里指的是其中一个配置文件，不是所有的，读者可以看到上面有个 for 循环的。下面从根节点开始解析：

#### 2-3-1-doRegisterBeanDefinitions

// DefaultBeanDefinitionDocumentReader 116
```javascript
protected void doRegisterBeanDefinitions(Element root) {
	// 我们看名字就知道，BeanDefinitionParserDelegate 必定是一个重要的类，它负责解析 Bean 定义，
   // 这里为什么要定义一个 parent? 看到后面就知道了，是递归问题，
   // 因为 <beans /> 内部是可以定义 <beans /> 的，所以这个方法的 root 其实不一定就是 xml 的根节点，也可以是嵌套在里面的 <beans /> 节点，从源码分析的角度，我们当做根节点就好了
   BeanDefinitionParserDelegate parent = this.delegate;
   this.delegate = createDelegate(getReaderContext(), root, parent);

   if (this.delegate.isDefaultNamespace(root)) {
	  // 这块说的是根节点 <beans ... profile="dev" /> 中的 profile 是否是当前环境需要的，
	  // 如果当前环境配置的 profile 不包含此 profile，那就直接 return 了，不对此 <beans /> 解析
	  // 不熟悉 profile 为何物，不熟悉怎么配置 profile 读者的请移步附录区
	  String profileSpec = root.getAttribute(PROFILE_ATTRIBUTE);
	  if (StringUtils.hasText(profileSpec)) {
		 String[] specifiedProfiles = StringUtils.tokenizeToStringArray(
			   profileSpec, BeanDefinitionParserDelegate.MULTI_VALUE_ATTRIBUTE_DELIMITERS);
		 if (!getReaderContext().getEnvironment().acceptsProfiles(specifiedProfiles)) {
			if (logger.isInfoEnabled()) {
			   logger.info("Skipped XML bean definition file due to specified profiles [" + profileSpec +
					 "] not matching: " + getReaderContext().getResource());
			}
			return;
		 }
	  }
   }

   preProcessXml(root); // 钩子
   // 往下看
   parseBeanDefinitions(root, this.delegate);
   postProcessXml(root); // 钩子

   this.delegate = parent;
}
```
preProcessXml(root) 和 postProcessXml(root) 是给子类用的钩子方法，鉴于没有被使用到，也不是我们的重点，我们直接跳过。

这里涉及到了 profile 的问题，对于不了解的读者，我在附录中对 profile 做了简单的解释，读者可以参考一下。

接下来，看核心解析方法 parseBeanDefinitions(root, this.delegate) :

// default namespace 涉及到的就四个标签 <import />、<alias />、<bean /> 和 <beans />，
// 其他的属于 custom 的
```javascript
protected void parseBeanDefinitions(Element root, BeanDefinitionParserDelegate delegate) {
   if (delegate.isDefaultNamespace(root)) {
	  NodeList nl = root.getChildNodes();
	  for (int i = 0; i < nl.getLength(); i++) {
		 Node node = nl.item(i);
		 if (node instanceof Element) {
			Element ele = (Element) node;
			if (delegate.isDefaultNamespace(ele)) {
			   // 解析 default namespace 下面的几个元素
			   parseDefaultElement(ele, delegate);
			}
			else {
			   // 解析其他 namespace 的元素
			   delegate.parseCustomElement(ele);
			}
		 }
	  }
   }
   else {
	  delegate.parseCustomElement(root);
   }
}
```

从上面的代码，我们可以看到，对于每个配置来说，分别进入到 parseDefaultElement(ele, delegate); 和 delegate.parseCustomElement(ele); 这两个分支了。

parseDefaultElement(ele, delegate) 代表解析的节点是 <import />、<alias />、<bean />、<beans /> 这几个

>

	这里的四个标签之所以是 default 的，是因为它们是处于这个 namespace 下定义的：
	http://www.springframework.org/schema/beans
	又到初学者科普时间，不熟悉 namespace 的读者请看下面贴出来的 xml，这里的第二行 xmlns 就是咯。
	<beans xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
		   xmlns="http://www.springframework.org/schema/beans"
		   xsi:schemaLocation="
				http://www.springframework.org/schema/beans
			  http://www.springframework.org/schema/beans/spring-beans.xsd"
		   default-autowire="byName">
	而对于其他的标签，将进入到 delegate.parseCustomElement(element) 这个分支。如我们经常会使用到的 <mvc />、<task />、<context />、<aop />等。
	这些属于扩展，如果需要使用上面这些 ”非 default“ 标签，那么上面的 xml 头部的地方也要引入相应的 namespace 和 .xsd 文件的路径，如下所示。同时代码中需要提供相应的 parser 来解析，如 MvcNamespaceHandler、TaskNamespaceHandler、ContextNamespaceHandler、AopNamespaceHandler 等。

	假如读者想分析 <context:property-placeholder location="classpath:xx.properties" /> 的实现原理，就应该到 ContextNamespaceHandler 中找答案。

	<beans xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
		  xmlns="http://www.springframework.org/schema/beans"
		  xmlns:context="http://www.springframework.org/schema/context"
		  xmlns:mvc="http://www.springframework.org/schema/mvc"
		  xsi:schemaLocation="
			   http://www.springframework.org/schema/beans 
			   http://www.springframework.org/schema/beans/spring-beans.xsd
			   http://www.springframework.org/schema/context
			   http://www.springframework.org/schema/context/spring-context.xsd
			   http://www.springframework.org/schema/mvc   
			   http://www.springframework.org/schema/mvc/spring-mvc.xsd  
		   "
		  default-autowire="byName">
	同理，以后你要是碰到 <dubbo /> 这种标签，那么就应该搜一搜是不是有 DubboNamespaceHandler 这个处理类。


回过神来，看看处理 default 标签的方法：


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

