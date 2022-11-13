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

# 缓存

## 缓存更新策略

1、**内存淘汰：**redis自动进行，当redis内存达到咱们设定的max-memery的时候，会自动触发淘汰机制，淘汰掉一些不重要的数据(可以自己设置策略方式)。无维护成本，但是一致性很差，**感觉就没有一致性的保证**

2、**超时剔除：**当我们给redis设置了过期时间ttl之后，redis会将超时的数据进行删除，方便咱们继续使用缓存。**这个也基本没有一致性**

3、**主动更新：**我们可以手动调用方法把缓存删掉，通常用于解决缓存和数据库不一致问题

**使用主动更新加上兜底（超时剔除）来保证高一致性**

## 主动更新的策略

1、**操作数据库更新数据后，删除缓存而不是更新缓存**。假设我们每次操作数据库后，都操作缓存，但是中间如果没有人查询，那么这个更新动作实际上只有最后一次生效，中间的更新动作意义并不大

2、**先操作数据库，再删除缓存**。原因在于，如果你选择第一种方案，在两个线程并发来访问时，假设线程1更新数据库，他**先把缓存删了**，此时线程2过来，他查询缓存数据并不存在，此时他写入缓存（查到的还是旧数据），当他写入缓存后，线程1再执行更新动作时（因为是先操删缓存后操作的数据库），实际上写入的就是旧的数据，新的数据被旧数据覆盖了。这就导致了缓存更新不到最新值

<img src="img/%E6%88%AA%E5%B1%8F2022-11-06%2020.01.12.png" alt="截屏2022-11-06 20.01.12" style="zoom:80%;" />

**但其实先操作数据库后删除缓存可以保证缓存可以更新到最新值，但是当数据库更新完毕没有删除缓存时，拿到的还是旧值，会有一个间隙的数据不一致**

两部分逻辑，第一部分是将数据从数据库放入缓存

第二部分才是这个数据库更新后操作缓存的逻辑

## 将数据放入缓存的逻辑操作

1、查询redis缓存

2、存在返回成功，不存在去数据库查询

3、数据库查询不到返回失败，查询到了就放入缓存中，**同时设置过期时间作为兜底**，然后返回查询结果

## 数据库更新后操作缓存的逻辑

**拦截器的拦截地址一定要加“/”，一定要是斜杠开头**

1、获取id

2、传入的id为空，就返回失败

3、**更新数据库，删除缓存，返回ok**

4、**给这一系列操作加上事务**
