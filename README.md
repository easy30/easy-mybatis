# easy-mybatis

Java database ORM framework base on mybatis mapper, but more easy to use with few sql. 

No SQL need for insert, update and most select operations.

## Overview
### Create a table and a java entity.
```sql

CREATE TABLE `user` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(30) DEFAULT NULL,
  `age` int(11) DEFAULT NULL,
  `email` varchar(50) DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `real_name` varchar(45) DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) 

```

```java
import lombok.Data;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Data
@Table(name = "user")
public class User {
    @Id
    private Long id;
    private String name;
    private Integer age;
    private String realName;
    @ColumnDefault(insertValue = "now()")
    private Date createTime;
    @ColumnDefault("now()")
    private Date updateTime;
}

```
### insert without sql
```sql
 insert into user(name,age,real_name) values('coolma',20,'mike')

```
```java
    user = new User();
    user.setName("coolma");
    user.setAge(20);
    user.setRealName("mike");
    userMapper1.insert(user);
    Long id = user.getId(); //return id

```
### update without sql
```sql
 update user set real_name='michael' where id=100

```
```java
    user = new User();
    user.setRealName("michael");
    user.setId(100L);
    userMapper1.update(user);

```
### column default value (for insert or update)

Directly define with @ColumnDefault on the fields.

```java
    @ColumnDefault(insertValue = "now()")
        private Date createTime;
        @ColumnDefault("now()")
        private Date updateTime;

```
### query with params Object (no or few sql)

Use params object, you can execute a query without sql or with few sql. 
 
 **params Object = normal query object + sql**
 
 The params object contains the query column names and values. If a property of params is not null, then it will be used in query.
 The deault operator of property(column) is '='.
 You can use  @QueryItem to define a custom sql snippet, use @QueryColumn to define the column name and operator.

```sql
  selete * from user where age=? and create_time>=? and create_time<? order by create_time desc
```

```java
//-- define params object
public class UserParams extends User {

    @QueryItem("create_time>= #{createTimeStart} ")
    private Date createTimeStart;

    @QueryColumn(column ="createTime",operator = ColumnOperator.GT)
    private Date createTimeEnd;
}

// execute a query
UserParams params=new UserParams();
params.setAge(20);
params.setCreateTimeStart(date1);
params.setCreateTimeEnd(date2);
List<UserDto> list= userMapper.listByParams(params,"createTime desc",null);
```


## Quick instruction

### Create table
```sql

CREATE TABLE `user` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(30) DEFAULT NULL,
  `age` int(11) DEFAULT NULL,
  `email` varchar(50) DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `real_name` varchar(45) DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) 

```

### Add mybatis common config
 Add dataSource,sqlSessionFactory and sqlSessionTemplate to spring config file( for Spring boot use @Bean).
 
```xml
 <bean id="dataSource" class="org.springframework.jdbc.datasource.SimpleDriverDataSource">
    <property name="username" value="root"/>
    <property name="password" value="password"/>
    <property name="driverClass" value="com.mysql.jdbc.Driver"/>
    <property name="url" value="jdbc:mysql://192.168.0.23:3306/mybatis?useUnicode=true&amp;characterEncoding=utf8&amp;allowMultiQueries=true&amp;useSSL=false"/>
    </bean>
    <bean id="sqlSessionFactory" class="org.mybatis.spring.SqlSessionFactoryBean">
        <property name="dataSource" ref="dataSource" />
    </bean>
    <bean id="sqlSessionTemplate" class="org.mybatis.spring.SqlSessionTemplate">
        <constructor-arg name="sqlSessionFactory" ref="sqlSessionFactory" />
    </bean>

```

### maven dependency

Note :
 1) mybatis version must >=3.5.1
 
 2) mybatis-spring 1.3.x(for spirng 3.2.2+) , 2.0.x(for spring 5,0+)

```xml
 <dependency>
    <groupId>com.cehome</groupId>
    <artifactId>easy-mybatis</artifactId>
    <version>1.0</version>
 </dependency>
 
   <dependency>
         <groupId>org.mybatis</groupId>
          <artifactId>mybatis</artifactId>
          <version>3.5.2</version>
  </dependency>

   <dependency>
              <groupId>org.mybatis</groupId>
              <artifactId>mybatis-spring</artifactId>
              <version>1.3.3</version>
    </dependency>

```

### Add easy-mybatis config

Add MapperScannerConfigurer , set scan basePackage, set **markerInterface property** to **com.cehome.easymybatis.Mapper** (base Mapper interface)

Add MapperFactory(easy-mybatis core bean).


```xml

      <bean class="org.mybatis.spring.mapper.MapperScannerConfigurer">
               <property name="basePackage" value="com.cehome.easymybatis"></property>
               <property name="markerInterface" value="com.cehome.easymybatis.Mapper"></property>
               <property name="sqlSessionFactoryBeanName" value="sqlSessionFactory"></property>
          </bean>
   
       <bean   class="com.cehome.easymybatis.MapperFactory" >
           <property name="sqlSessionFactory" ref="sqlSessionFactory"></property>
     </bean>


```

### Create entity
Create User entity with @Table @Id annotation.
```java

import lombok.Data;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@Table(name = "user")
public class User {

    @Id
    private Long id;
    private String name;
    private Integer age;
    private String realName;
 
}
``` 

### Create User mapper

```java
import com.cehome.easymybatis.Mapper;
public interface UserMapper1 extends Mapper<User> {
}
```

### insert, update, delete and select

```java
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:applicationContext.xml")
public class MapperTest {
    @Autowired
    UserMapper1 userMapper1;
    @Test
    public void test() throws SQLException {
            User user = null;
            //-- insert into user(name,age,real_name) values('coolma',20,'mike')
            user = new User();
            user.setName("coolma");
            user.setAge(20);
            user.setRealName("mike");
            userMapper1.insert(user);
            Long id = user.getId(); //return id
    
            //-- update user set real_name='michael' where id=100
            user = new User();
            user.setRealName("michael");
            user.setId(100L);
            userMapper1.update(user);
    
            //-- update user set real_name='tom' where id=100 and age=20
            user = new User();
            user.setRealName("tom");
            User params = new User();
            params.setId(100L);
            params.setAge(20);
            userMapper1.updateByParams(user, params,"id","age");
    
            //-- delete from user where id=100
            userMapper1.deleteById(100L);
    
    
            //-- select one: select * from user where id=100
            user= userMapper1.getById(id,null);
    
            //-- select one: select * from user where real_name='tom'
            params=new User();
            params.setRealName("tom");
            user= userMapper1.getByParams(params,null);
    
            //-- list: select name,real_name from user where age=20
            params=new User();
            params.setAge(20);
            List<User> list=userMapper1.listByParams(params,null,"name,realName");
    
            //-- page: select name,real_name from user where age=20 order by real_name asc limit 0,20
            params=new User();
            params.setAge(20);
            Page<User> page=new Page(1,20);
            userMapper1.pageByParams(params,page,"realName asc","name,realName");
            System.out.println(page.getData());
    
    }
}

```

## Advance 

###  Column default value
@ColumnDefault - When insert or update, set cloumn default value by sql value or function. 

```java
public @interface ColumnDefault {
    String value() default "";// for insert or update
    String insertValue() default ""; //for insert only
    String updateValue() default ""; //for update only

}
```

Define annotations on properties:

createTime - Record create time. @ColumnDefault(insertValue = "now()") means set to now() when insert.

updateTime - update this column when insert or update, So set  @ColumnDefault("now()") on the field.

```java
    import com.cehome.easymybatis.DialectEntity;
    import com.cehome.easymybatis.annotation.ColumnDefault;
    import lombok.Data;
    import javax.persistence.Id;
    import javax.persistence.Table;
    import java.util.Date;
    @Data
    @Table(name = "user")
    public class User extends DialectEntity {
        @Id
        private Long id;
        private String name;
        private Integer age;
        private String realName;
        private String email;
    
        @ColumnDefault(insertValue = "now()") 
        private Date createTime;
    
        @ColumnDefault("now()")
        private Date updateTime;
    
    
    }
```

```java
class MapperTest1{
    @Autowired
    UserMapper2 userMapper2;
    @Test
    public void test() throws SQLException {
        User user=null;
        
        //-- update user set name='coolma', update_time=now() where id=100
        user=new User();
        user.setName("coolma");
        user.setId(100L);
        userMapper2.update(user);
        
        
    }
}
``` 

### Simple query
```java
//--- Mapper method
List<R> listByParams(@Param(Const.PARAMS) Object params,
                         @Param(Const.ORDER) String orderBy,
                         @Param(Const.COLUMNS) String selectColumns);
List<R> pageByParams(@Param(Const.PARAMS) Object params,
                         @Param(Const.PAGE) Page page,
                         @Param(Const.ORDER)String orderBy,
                         @Param(Const.COLUMNS) String selectColumns);
//-- define params object 

public class UserParams extends User {

    @QueryItem("create_time>= #{createTimeStart} ")
    private Date createTimeStart;

    @QueryColumn(column ="createTime",operator = ColumnOperator.GT)
    private Date createTimeEnd;
}

// execute a query
// -- selete * from user where age=? and create_time>=? and create_time<? order by create_time desc
UserParams params=new UserParams();
params.setAge(20);
params.setCreateTimeStart(date1);
params.setCreateTimeEnd(date2);
List<UserDto> list= userMapper.listByParams(params,"createTime desc",null);

```
The params object is the query conditions. If a property of params is not null, then it will be used in query.
The deault operator of expression is '='. You can use  @QueryItem to define custom sql , use @QueryColumn to define a related column and operator.

### Complex Query 

1) For executing  "select * from user where age>20 order by create_time desc", you can invoke listBySQL() with sql:

 "select * from user where age>{#age} order by create_time desc".

This sql can be reduced to  "age>{#age} order by create_time desc".

You can replace create_time with property {createTime} and result in  "age>#{age} order by {createTime} desc"


```java
class MapperTest2{
    @Test
    public void listBySQL() throws SQLException {
        User params=new User();
        params.setAge(20);
        List<User> list= userMapper2.listBySQL("age>#{age} order by {createTime} desc",params);
       
    }
}

```

2) Recommend using mybatis native annotation or xml sql for complex query .

Define findById  with @Select SQL.

Define findById2 without annotation.

```java

public interface UserMapper2 extends Mapper<User> {
    @Select("select * from user where id = #{id}")
    User findById(@Param("id") long id);

    User findById2(long id);

}

```

For findById2 without annotation, you need to define a sql in xml file.

First, set mapperLocations. 
```xml
 <bean id="sqlSessionFactory" class="org.mybatis.spring.SqlSessionFactoryBean">
        <property name="dataSource" ref="dataSource" />
        <property name="mapperLocations" value="classpath*:com/cehome/easymybatis/sqlmap/*.xml"></property>
    </bean>
```

Second, create UserMapper.xml.
```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">

<mapper namespace="com.cehome.easymybatis.test2.UserMapper2">

    <select id="findById2" parameterType="long"  resultType="com.cehome.easymybatis.test2.User">
       select * from user where id = #{id}
    </select>

</mapper>

```

Invoke method. 

```java
public class MapperTest {
    @Test
        public void findById() throws SQLException {
    
            User user= userMapper2.findById(36L);
        }
        @Test
        public void findById2() throws SQLException {
    
            User user= userMapper2.findById2(36L);
        }
}
```

