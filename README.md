# easy-mybatis

## mybatis config


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

## mybatis common config
 Add dataSource,sqlSessionFactory and sqlSessionTemplate in spring mvc config file( for Spring boot use @Bean).
 
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

## easy-mybatis config

Add bean MapperScannerConfigurer , set **markerInterface propertiy** to **com.cehome.easymybatis.Mapper** (base Mapper interface)
Add bean MapperFactory -- easy-mybatis core bean.


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


## sample1
### Create entity

```java
import com.cehome.easymybatis.DialectEntity;
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

### Create UserMapper

```java
import com.cehome.easymybatis.Mapper;

/**
 * coolma 2019/10/23
 **/
public interface UserMapper1 extends Mapper<User> {


}
```

###