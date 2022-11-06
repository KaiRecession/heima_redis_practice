# 依赖说明

```java
// redis java端的依赖			
<dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-redis</artifactId>
        </dependency>
  // redis数据库连接池的依赖
        <dependency>
            <groupId>org.apache.commons</groupId>
            <artifactId>commons-pool2</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <dependency>
            <groupId>mysql</groupId>
            <artifactId>mysql-connector-java</artifactId>
            <scope>runtime</scope>
            <version>5.1.47</version>
        </dependency>
  // 提供一些方便的注解
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
  // mybatis单表增删改查的简化
        <dependency>
            <groupId>com.baomidou</groupId>
            <artifactId>mybatis-plus-boot-starter</artifactId>
            <version>3.4.3</version>
        </dependency>
        <!--hutool-->
  // json处理、字符串处理等工具类
        <dependency>
            <groupId>cn.hutool</groupId>
            <artifactId>hutool-all</artifactId>
            <version>5.7.17</version>
        </dependency>
```

# 短信登录功能

### 基于session实现

dto：DTO就是数据传输对象(Data Transfer Object)的缩写。 DTO模式，是指将数据封装成普通的JavaBeans，在J2EE多个层次之间传输。 DTO类似信使，是同步系统中的Message。 该JavaBeans可以是一个数据模型Model。  

使用接口中没有的方法，写出来后直接根据提示创建接口方法在相应的接口中就行，跳到接口中后再次点击左边中的绿色小圆点（小圆点中有个I）直接跳转到接口的实现类中去

# ThreadLocal

<img src="img/%E6%88%AA%E5%B1%8F2022-10-17%2009.51.32.png" alt="截屏2022-10-17 09.51.32" style="zoom:50%;" />

每个线程都会有属于自己的本地内存，在堆（也就是上图的主内存）中的变量在被线程使用的时候会被复制一个副本线程的本地内存中，当线程修改了共享变量之后就会通过JMM管理控制写会到主内存中。

 很明显，在多线程的场景下，当有多个线程对共享变量进行修改的时候，就会出现线程安全问题，即数据不一致问题。常用的解决方法是对访问共享变量的代码加锁（synchronized或者Lock）。但是这种方式对性能的耗费比较大。在JDK1.2中引入了ThreadLocal类，来修饰共享变量，使每个线程都单独拥有一份共享变量，这样就可以做到线程之间对于共享变量的隔离问题。

**JDK8之后，每个Thread维护一个ThreadLocalMap对象，这个Map的key是ThreadLocal实例本身，value是存储的值要隔离的变量，是泛型**

一般都会将ThreadLocal声明成一个静态字段，因为线程里面有私有的Map变量去存储各个ThreadLocal的value，map的key就是ThreadLocal对象，想要获取就必须获得ThreadLoacl对象，**不同的线程拿到相同的ThreadLocal在自己线程的map中获取value也不同。**

ThreadLocal对象里面的save方法首先就会拿到当前线程，然后再往下进行，这样就顺起来了

# 拦截器

**将校验用户是否登录的操作代码提取出来，其他的各种Controller就不用再校验，拦截器将校验出来的结果通过ThreadLocal进行传递，完美这样子，看一下线程对于tomcat 的意义**

1、编写拦截器的代码，继承HandlerInterceptor，里面的三个方法返回值为boolean，并且是这个接口的default方法，就算不implement也能够使用

2、将便携好的拦截器配到spring中去。

* 新建一个配置类实现WebMvcConfigurer接口
* 覆盖方法addInterceptors，使用方法的形参对象的addInterceptor中放入new好的刚刚写的拦截器类的对象，再配置一下路径
* 最后上面加上Configuration的注解即可

拦截器里面的redis注入只能通过构造函数进行注入，因为拦截器的代码是写在一个里面，这个类是不进行spring注入的

**看代码的报错直接看第一行的报错或者看log信息**

# Redis短信登录

Tomcat的session共享比较la，建议使用redis集群来取代session机制

在发送短信验证码时的key使用电话号码取代session的code属性，在验证用户是否登录时key使用token来取代session的user属性

## 两个拦截器的作用

<img src="img/%E6%88%AA%E5%B1%8F2022-11-06%2016.45.31.png" alt="截屏2022-11-06 16.45.31" style="zoom:80%;" />

第一个拦截器只是查看当前用户的token有没有以及有没有过期，有token并且没过期就放入ThreadLocal中去方便需要用户信息的拦截器获取，并且刷新token的有效期，这个拦截器不会拦截，放行所有请求，唯一的目的就是为了刷新tocken，**因为用户访问网站的所有网页都需要刷新tocken，但是并不是所有的网页都需要token信息，所以只能这样写而不是将刷新token写到第二个拦截器中**
