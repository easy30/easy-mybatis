# easy-mybatis

A lightweight ORM layer on top of [MyBatis](https://mybatis.org/) that lets you do **insert / update / delete / upsert and most queries with no hand-written SQL**.

You define a plain Java entity, extend a base `Mapper` interface, and get a full set of CRUD, batch, paging and conditional-query methods for free. When you do need raw SQL, MyBatis annotations and XML still work exactly as usual.

```java
@Table(name = "user")
@Data
public class User {
    @Id private Long id;
    private String name;
    private Integer age;
    private String realName;
    @ColumnDefault(insertValue = "now()") private Date createTime;
    @ColumnDefault("now()")                private Date updateTime;
}

public interface UserMapper extends Mapper<User, User> {}
```

```java
User u = new User();
u.setName("coolma");
u.setAge(20);
userMapper.insert(u);          // insert into user(name, age, create_time, update_time) values (...)
Long id = u.getId();           // generated key filled back

u.setRealName("mike");
userMapper.update(u);          // update user set ... where id = ?

User one = userMapper.get(id, null);   // select * from user where id = ?
userMapper.deleteById(id);             // delete from user where id = ?
```

---

## Features

- **Zero-SQL CRUD** — `insert`, `update`, `save`, `deleteById`, `get` and friends generated from the entity mapping.
- **Batch operations** — `insertList` for bulk insert, `upsertList` for insert-or-update.
- **Cross-database upsert** — one API, six dialects (MySQL, H2, PostgreSQL, SQLite, Oracle, SQL Server).
- **Parameter-object queries** — build `where` clauses from a POJO; non-null properties become conditions. Customize with `@QueryColumn` / `@QueryExp`.
- **Paging** — `pageByParams` / `pageBySQL` with automatic count and dialect-specific limit/offset.
- **Column defaults & generators** — `@ColumnDefault` for SQL defaults (e.g. `now()`), `@ColumnGeneration` for UUID/custom key generation.
- **`@Transient` support** — properties annotated with `@Transient` are excluded from INSERT, UPDATE **and** WHERE query conditions (consistent across all SQL generation).
- **Per-call options** — `UpdateOption` / `SelectOption` / `DeleteOption` to tweak a single call (ignore columns, null handling, table override, …).
- **Escape hatch** — drop down to MyBatis `@Select` annotations or XML whenever you want full control.

## Requirements

- Java 8+
- MyBatis `>= 3.5.1`
- mybatis-spring `1.3.x` (Spring 3.2.2+) or `2.0.x` (Spring 5.0+)

## Installation

```xml
<dependency>
    <groupId>com.github.easy30</groupId>
    <artifactId>easy-mybatis</artifactId>
    <version>2.0.3-SNAPSHOT</version>
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

---

## Quick Start

### 1. Configure MyBatis + easy-mybatis

The only easy-mybatis-specific piece is the `EasyConfiguration` bean wired into the `SqlSessionFactory`, plus a `MapperScannerConfigurer` whose `markerInterface` is the easy-mybatis `Mapper`.

```xml
<bean id="dataSource" class="org.springframework.jdbc.datasource.SimpleDriverDataSource">
    <property name="username" value="root"/>
    <property name="password" value="password"/>
    <property name="driverClass" value="com.mysql.cj.jdbc.Driver"/>
    <property name="url" value="jdbc:mysql://localhost:3306/test?useUnicode=true&amp;characterEncoding=utf8&amp;useSSL=false"/>
</bean>

<!-- easy-mybatis configuration; the dialect is auto-detected from the connection -->
<bean id="configuration" class="com.github.easy30.easymybatis.EasyConfiguration"/>

<bean id="sqlSessionFactory" class="org.mybatis.spring.SqlSessionFactoryBean">
    <property name="dataSource" ref="dataSource"/>
    <property name="configuration" ref="configuration"/>
    <!-- optional: location of hand-written mapper XML -->
    <property name="mapperLocations" value="classpath*:com/example/sqlmap/*.xml"/>
</bean>

<bean id="sqlSessionTemplate" class="org.mybatis.spring.SqlSessionTemplate">
    <constructor-arg name="sqlSessionFactory" ref="sqlSessionFactory"/>
</bean>

<!-- scan mappers; markerInterface MUST be the easy-mybatis Mapper -->
<bean class="org.mybatis.spring.mapper.MapperScannerConfigurer">
    <property name="basePackage" value="com.example"/>
    <property name="markerInterface" value="com.github.easy30.easymybatis.Mapper"/>
    <property name="sqlSessionFactoryBeanName" value="sqlSessionFactory"/>
</bean>

<!-- enables UUID id generation via @ColumnGeneration(insertGeneration="uuid") -->
<bean class="com.github.easy30.easymybatis.generation.UUIDGeneration"/>
```

> **Spring Boot:** declare the same beans with `@Bean` methods in a `@Configuration` class. No XML required.

### 2. Define an entity

```java
import lombok.Data;
import javax.persistence.Id;
import javax.persistence.Table;
import com.github.easy30.easymybatis.annotation.ColumnDefault;

@Data
@Table(name = "user")
public class User {
    @Id
    private Long id;                 // auto-increment key, filled back after insert
    private String name;
    private Integer age;
    private String realName;         // maps to real_name (camelCase -> snake_case)

    @ColumnDefault(insertValue = "now()")
    private Date createTime;          // set on insert only

    @ColumnDefault("now()")
    private Date updateTime;          // set on insert and update
}
```

### 3. Declare a mapper

`Mapper<E, R>` — `E` is the entity, `R` is the return/DTO type (often the same as `E`, or a subclass).

```java
import com.github.easy30.easymybatis.Mapper;

public interface UserMapper extends Mapper<User, User> {}
```

### 4. Use it

```java
// insert
User user = new User();
user.setName("coolma");
user.setAge(20);
user.setRealName("mike");
userMapper.insert(user);
Long id = user.getId();

// update by id (only non-null fields are written)
User patch = new User();
patch.setId(id);
patch.setRealName("michael");
userMapper.update(patch);

// update by params: update user set real_name='tom' where id=? and age=?
User values = new User();
values.setRealName("tom");
User cond = new User();
cond.setId(id);
cond.setAge(20);
userMapper.updateByParams(values, cond, "id,age");

// select one by id
User found = userMapper.get(id, null);

// select one by params
User q = new User();
q.setRealName("tom");
User byParams = userMapper.getByParams(q, null, null);

// list selected columns by params
User listQ = new User();
listQ.setAge(20);
List<User> list = userMapper.listByParams(listQ, null, "name,realName");

// paging
Page<User> page = new Page<>(1, 20);
userMapper.pageByParams(listQ, page, "realName asc", "name,realName");
System.out.println(page.getData() + " of " + page.getRecordCount());

// delete
userMapper.deleteById(id);
```

---

## Documentation

| Guide | Contents |
|-------|----------|
| [Entity Mapping & Annotations](docs/entity-mapping.md) | `@Table`, `@Id`, `@Column`, `@ColumnDefault`, `@ColumnGeneration`, naming rules |
| [CRUD & Batch Operations](docs/crud.md) | Full `Mapper` method reference: insert / update / save / delete, `insertList`, options |
| [Upsert (insert-or-update)](docs/upsert.md) | `upsertList` semantics, three update modes, per-dialect SQL |
| [Queries & Paging](docs/queries.md) | Parameter objects, `@QueryColumn` / `@QueryExp`, operators, `Range`, `listBySQL`, paging |
| [Options & FAQ](docs/options-faq.md) | `UpdateOption` / `SelectOption` / `DeleteOption`, empty-string handling, raw SQL escape hatch |

## Mapper method cheat sheet

| Method | Purpose |
|--------|---------|
| `insert(e, opts…)` | Insert one row; generated key filled back |
| `insertList(list, opts…)` | Bulk insert |
| `upsertList(list, keyColumns, updateColumns, opts…)` | Bulk insert-or-update |
| `update(e, opts…)` | Update by id (non-null fields) |
| `save(e, opts…)` | Insert if id is null, else update |
| `updateByParams(e, params, paramNames, opts…)` | Update with a condition object |
| `updateByCondition(e, condition, params, opts…)` | Update with a custom `where` |
| `deleteById(id, opts…)` | Delete by id |
| `deleteByParams(params, paramNames, opts…)` | Delete by a condition object |
| `deleteByCondition(condition, params, opts…)` | Delete with a custom `where` |
| `get(id, columns, opts…)` | Select one by id |
| `getByParams(params, orderBy, columns, opts…)` | Select first match by params |
| `getValue(id, column, opts…)` | Select a single column value by id |
| `getValueByParams / getValueByCondition / getValueBySQL` | Select a single scalar |
| `listByParams(params, orderBy, columns, opts…)` | List by params |
| `listByIds(ids, columns, opts…)` | List by id array |
| `listBySQL(sql, params)` | List with a (partial) SQL fragment |
| `pageByParams(params, page, orderBy, columns, opts…)` | Paged list by params |
| `pageBySQL(sql, params, page)` | Paged list with SQL |

See [docs/crud.md](docs/crud.md), [docs/upsert.md](docs/upsert.md) and [docs/queries.md](docs/queries.md) for full signatures and examples.

## License

See the repository for license details.
