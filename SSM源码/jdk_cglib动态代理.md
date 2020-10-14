@[TOC](SpringAOP 分析之JDK-Cglib动态代理分析)


本文是 对 jdk cglib动态代理的分析和案例
当然也会说一下 静态代理的概念和案例



目录
- [1. 代理模式](#1-代理模式)
- [2. 静态代理](#2-静态代理)
- [3. 动态代理](#3-动态代理)	
	- [3.1 JDK动态代理](#3-1-JDK动态代理)	
	- [3.2 CGLIB动态代理](#3-2-CGLIB动态代理)	
	- [3.3 JDK和CGLIB对比](#3-3-JDK和CGLIB对比)	
- [4. 静态代理和动态代理的区别](#4-静态代理和动态代理的区别)	
- [5. 总结](#5-总结)	


- [附录](#附录)
- [AOP](#AOP)



# 1-代理模式

其实代理这个东西不难，就是一个Proxy，所谓代理模式，其实就是一种比较好的设计模式。简单的来说就是**我们使用代理对象来代替真实对象的访问**，
这样就可以在不修改原目标对象的前提下，**提供额外的功能或者动作，扩展目标对象的动作或者功能**，来更好的提供服务。

主要作用: 扩展目标对象的动作，比如在目标对象执行一定动作之前或者之后做一些额外自定义的动作。

代理模式有两种： 静态代理和动态代理两种。 我们先来看 静态代理模式噢。

# 2-静态代理

静态代理中，要求我们在Proxy代理类中自己实现方法的增强，(很容易修改源码，不方便，非常不灵活)。
实际上从 JVM 这个层面来看，静态代理发生在编译时，就将接口实现类，代理类等等都编译成了class文件。

那么我们来在实操之前分析一下怎样写一个案例：
>1. 实现一个接口和实现类
>2. 创建一个代理类 同样实现这个接口，加上自己的增强动作，那么此时会组合该接口。
>3. 创建一个 代理工厂，来返回目标对象的代理

案例如下： 对操作用户的独享进行增强
**1. userService:**
```javascript
public interface UserService {
    public void addUser();
    public void editUser();
}
```

**2. userServiceImpl:**
```javascript
public class UserServiceImpl  implements UserService{
    @Override
    public void addUser() {
        System.out.println("添加一个 新用户");
    }
    @Override
    public void editUser() {
        System.out.println("编辑用户");
    }
}
```

**3. UserServiceProxy:** 
```javascript
public class UserServiceProxy  implements UserService{
    private UserService userService;
    public UserServiceProxy(UserService userService) {
        this.userService = userService;
    }
    @Override
    public void addUser() {
        System.out.println("代理类进入，addUser");
        userService.addUser();
        System.out.println("代理类出去 addUser");
    }
    @Override
    public void editUser() {
        System.out.println("代理类进入，editUser");
        userService.editUser();
        System.out.println("代理类出去 editUser");
    }
}
```
**4. ProxyFactory**
```javascript
public class ProxyFactory {
    public static UserServiceProxy getProxy() {
        return new UserServiceProxy(new UserServiceImpl());
    }
}
```

**5. 使用测试**
```javascript
public class StaticProxyTest {
    @Test
    public void test() {
        UserServiceProxy proxy = ProxyFactory.getProxy();
        proxy.addUser();
        proxy.editUser();
    }
}
```
输出： 
```
代理类进入，addUser
添加一个 新用户
代理类出去 addUser
代理类进入，editUser
编辑用户
代理类出去 editUser
```
看见了吗，，我们实现了手动的增强操作 用户的对象哦 

接下里我们来看 动态代理 

# 3-动态代理

对于动态代理，怎么说呢，其实就是比静态代理更加灵活，不用指定对那个方法的代理，而是代理目标类的所有方法，我们可可以在运行时动态的增强我们调用的方法。

**其实从 JVM角度来说，动态代理是运行时动态生成的class字节码，跟着JVM加载到JVM中。**

**动态代理机制还是要学习的，因为在一些框架内部使用了动态代理，掌握它，可以更好地去理解框架运行原理。**

首先我们先看 jdk自带的动态代理机制
## 3-1-JDK动态代理

* 1. 在JDK动态代理机制中， **InvocationHandler** 接口和 **Proxy**是核心。

Proxy 是 newProxyInstance() 最为核心，这个方法是生成一个代理对象。
来看源码： 

```javascript
public static Object newProxyInstance(ClassLoader loader,
									  Class<?>[] interfaces,
									  InvocationHandler h)
	throws IllegalArgumentException {
	......
}
```
说明： 这个方法三个参数 ：
1. **loader**: 类加载器， 用于加载代理对象。
2. **interfaces**: 被代理类实现的一些接口。 
3. **h** : 这个是实现了InvocationHandler 接口的对象，其实依靠的是invoke方法

其实还需要一个 InvocationHandler 实现类，在实现invoke 方法，来增强 真实对象。

```javascript
public interface InvocationHandler {
    /**
     * 当你使用代理对象调用方法的时候实际会调用到这个方法
     */
    public Object invoke(Object proxy, Method method, Object[] args)
        throws Throwable;
}
```
invoke() 方法有下面三个参数：

1. **proxy** :动态生成的代理类
2. **method** : 与代理类对象调用的方法相对应
3. **args** : 当前 method 方法的参数


原理： 当我们调用通过Proxy的 newProxyInstancce() 创建代理的一个方法时， 被迫转到 invoke方法中，然后 
Object obj = method.invoke(target, args); 调用真实对象的方法。

* 2. 此时 我们分析一波，怎样做或者创建的一个步骤是什么呢？

>1. 定义一个接口和实现类
>2. 定义一个ProxyExample 实现InvocationHandler 并重写 invoke方法， 我们会调用原生方法（被代理类的方法）并自定义一些处理逻辑；
>3. 在example中  组合private Object target = null; 然后写 bind方法 ，返回Proxy.newInstamce(ClassLoader loader,Class<?>[] interfaces,InvocationHandler h)方法创建代理对象；

* 3. 案例代码：
接口和实现类：
```javascript
public interface HelloWord {
    public void sayHelloWorld();
}

public class HelloWordImpl implements HelloWord{
    @Override
    public void sayHelloWorld() {
        System.out.println("您好 世界 ");
    }
}
```

自定义ProxyExample：
```javascript
public class JdkProxyExample implements InvocationHandler {
    // 真实 对象
    private Object target = null;
    
    public Object bind(Object target) {
        this.target = target;
        return Proxy.newProxyInstance(target.getClass().getClassLoader(), target.getClass().getInterfaces(), (InvocationHandler) this);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        System.out.println(" 进入代理逻辑方法");
        System.out.println("调用 真实对象之前的服务 ，，");
        Object obj = method.invoke(target, args);
        System.out.println("调用 真实对象之后的服务。。");
        return obj;
    }
}
```

测试类：
```
public class JdkProxyTest {
    public static void main(String[] args) {

        JdkProxyExample example = new JdkProxyExample();
        HelloWord proxy = (HelloWord) example.bind(new HelloWordImpl());
        proxy.sayHelloWorld();
    }
}
```

结果 ：
```
 进入代理逻辑方法
调用 真实对象之前的服务 ，，
您好 世界 
调用 真实对象之后的服务。。
```

## 3-2-CGLIB动态代理

jdk缺点： 只能代理实现了接口类，那么我们引出 Cglib（直接代理类）

>CGLIB(Code Generation Library)是一个基于ASM的字节码生成库，它允许我们在运行时对字节码进行修改和动态生成。CGLIB 通过继承方式实现代理。
很多知名的开源框架都使用到了CGLIB， 例如 Spring 中的 AOP 模块中：如果目标对象实现了接口，则默认采用 JDK 动态代理，否则采用 CGLIB 动态代理。

* 1. 在Cglib机制中，**MethodInterceptor** 接口 和 **getProxy方法中的 Enhancer**类是核心 。

此时， 你需要自定义 **MethodInterceptor** 并重写 **intercept** 方法，**intercept** 用于拦截增强被代理类的方法。

```javascript
public interface MethodInterceptor
extends Callback{
    // 拦截被代理类中的方法
    public Object intercept(Object obj, java.lang.reflect.Method method, Object[] args,
                               MethodProxy proxy) throws Throwable;
}
```
1. **obj** :被代理的对象（需要增强的对象）
2. **method**:被拦截的方法（需要增强的方法）
3. **args** :方法入参
4. **methodProxy** :用于调用原始方法

增强： 
你可以通过 Enhancer类来动态获取被代理类，当代理类调用方法的时候，实际调用的是 MethodInterceptor 中的 intercept 方法。
```javascript
public Object getProxy(Class cls) {
	Enhancer enhancer = new Enhancer();
	// 设置 增强类型
	enhancer.setSuperclass(cls);
	//定义代理逻辑对象为当前对象，要求当前对象实现 MethodInterceptor 方法
	enhancer.setCallback(this);
	// 生成并返回代理对象
	return enhancer.create();
}
```

* 2. 哈哈哈 我们来看一下怎样编写？？ 规则？？

>1. 定义一个类 base	
>2. 自定义MethodInterceptor 实现类并重写 intercept 方法，intercept 用于拦截增强被代理类的方法，和 JDK 动态代理中的 invoke 方法类似；
>3. 通过 Enhancer 类 create() 创建代理类 

* 3. 下面是代码：
注意： 我们必须加入 Maven依赖，因为cglib不是java的api哦。
```javascript
<dependency>
	<groupId>cglib</groupId>
	<artifactId>cglib</artifactId>
	<version>3.3.0</version>
</dependency>
```

base类 ：
```javascript
public class CglibBase {

    public void sayHello(String str) {
        System.out.println("你好， 张三 ");
    }
}
``` 

代理类 ：
```javascript
public class CglibProxyExample implements MethodInterceptor {

    public Object getProxy(Class cls) {
        Enhancer enhancer = new Enhancer();
        // 设置 增强类型
        enhancer.setSuperclass(cls);
        //定义代理逻辑对象为当前对象，要求当前对象实现 MethodInterceptor 方法
        enhancer.setCallback(this);
        // 生成并返回代理对象
        return enhancer.create();
    }

    @Override
    public Object intercept(Object proxy, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
        System.out.println("调用真实 对象前 ");
        // 真正调用
        Object result = methodProxy.invokeSuper(proxy, args);
        System.out.println("调用真实 对象之后");
        return result;
    }
}
```

测试 ： 
```javascript
public class CglibProxyTest {

    public static void main(String[] args) {
        CglibProxyExample example = new CglibProxyExample();
        CglibBase proxy = (CglibBase) example.getProxy(CglibBase.class);
        proxy.sayHello("张三 ");
    }
}
```

结果 ：
```javascript
调用真实 对象前 
你好， 张三 
调用真实 对象之后
```

## 3-3-JDK和CGLIB对比

1. **JDK 动态代理只能只能代理实现了接口的类，而 CGLIB 可以代理未实现任何接口的类**。 另外， CGLIB 动态代理是通过生成一个被代理类的子类来拦截被代理类的方法调用，因此不能代理声明为 final 类型的类和方法。
2. 就二者的效率来说，大部分情况都是 JDK 动态代理更优秀，随着 JDK 版本的升级，这个优势更加明显。
3.JDK的核心是实现InvocationHandler接口，使用invoke()方法进行面向切面的处理，调用相应的通知。
4. CGLIB的核心是实现MethodInterceptor接口，使用intercept()方法进行面向切面的处理，调用相应的通知。

# 4-静态代理和动态代理的区别

1. **灵活性** ：动态代理更加灵活，不需要必须实现接口，可以直接代理实现类，并且可以不需要针对每个目标类都创建一个代理类。另外，静态代理中，接口一旦新增加方法，目标对象和代理对象都要进行修改，这是非常麻烦的！
2. **JVM 层面** ：静态代理在编译时就将接口、实现类、代理类这些都变成了一个个实际的 class 文件。而动态代理是在运行时动态生成类字节码，并加载到 JVM 中的。

# 5-总结

Spring中当Bean实现接口时，Spring就会用JDK的动态代理，当Bean没有实现接口时，Spring使用CGlib来实现。

这篇文章主要写的 静态代理 和动态代理，我们更好地认识了他们和其区别，jdk 和cglib等

具体的代码在： 
[https://github.com/1367379258/MyNote/tree/main/Proxy](https://github.com/1367379258/MyNote/tree/main/Proxy)


# 附录

# AOP

>AOP(面向切面编程)，它可以用来拦截方法前后，来达到增强方法的目的。所以我理解的AOP的本质是在一系列纵向的控制流程中，把那些相同的子流程提取成一个横向的面，就像下面这张图把相同的逻辑，用户鉴权、资源释放抽取出来，横切到各个需要该场景的方法的开头、中间以及结尾。

AOP术语：

* 通知（Advice）： 何时（Before，After，Around，After还有几个变种） 做什么
* 连接点（JoinPoint）： 应用对象提供可以切入的所有功能（一般是方法，有时也是参数）
* 切点（PointCut)： 通过指定，比如指定名称，正则表达式过滤， 指定某个/些连接点， 切点描绘了 在何地 做
* 切面（Aspect）： 通知 + 切点 何时何地做什么
* 引入（Introduction）：向现有类添加新的属性或方法
* 织入（Weaving）： 就是将切面应用到目标对象的过程


