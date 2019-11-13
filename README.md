# easy-mybatis

## Quick start

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

### Add easy-mybatis config

Add bean MapperScannerConfigurer , set **markerInterface propertiy** to **com.cehome.easymybatis.Mapper** (base Mapper interface)
Add bean MapperFactory(easy-mybatis core bean). Config scan basePackage.


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

/**
 * coolma 2019/10/23
 **/
public interface UserMapper1 extends Mapper<User> {


}
```

### insert, update and delete operations

```java
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:applicationContext.xml")
public class MapperTest1 {
    @Autowired
    UserMapper1 userMapper1;
    @Test
    public void insert() throws SQLException {
        User user = null;
        //-- insert into user(name,age,real_name) values('coolma',20,'mike')
        user = new User();
        user.setName("coolma");
        user.setAge(20);
        user.setRealName("mike");
        userMapper1.insert(user);
        Long id = user.getId(); //return 100

        //-- update user set real_name='michael' where id=100
        user = new User();
        user.setRealName("michael");
        user.setId(100L);
        userMapper1.update(user);

        //-- update user set real_name='tom where id=100 and age=20
        user = new User();
        user.setRealName("tom");
        User params = new User();
        params.setId(100L);
        params.setAge(20);
        userMapper1.updateByEntity(user, params);

        //-- delete from user where id=100
        userMapper1.deleteById(100L);

    }
}

```