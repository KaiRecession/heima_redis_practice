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