# Queries & Paging

easy-mybatis lets you query with a **parameter object** (no SQL), a **SQL fragment** (a little SQL), or full MyBatis annotations/XML (any SQL). This page covers the first two and paging.

[← Back to README](../README.md)

## Parameter-object queries

A *params object* carries the query conditions. **Any non-null property becomes a condition**, joined with `AND`. The default operator is `=` for scalars and `IN` for arrays. No SQL needed for the common case.

```java
User q = new User();
q.setAge(20);
List<User> list = userMapper.listByParams(q, "createTime desc", null);
// select * from user where age = 20 order by create_time desc
```

The third argument is the select column list (`null` = all columns); the second is `order by`.

### Read methods

```java
// one row by id
R get(Object id, String selectColumns, SelectOption... options);

// first row by params
R getByParams(Object params, String orderBy, String selectColumns, SelectOption... options);

// list by params
List<R> listByParams(Object params, String orderBy, String selectColumns, SelectOption... options);

// list by id array
List<R> listByIds(Object[] ids, String selectColumns, SelectOption... options);

// single scalar value
<V> V getValue(Object id, String column, SelectOption... options);
<V> V getValueByParams(Object params, String orderBy, String column, SelectOption... options);
<V> V getValueByCondition(String condition, Object params, String column, SelectOption... options);
<V> V getValueBySQL(String sql, Object params);
```

`selectColumns` accepts property or column names: `"name,realName"` or `"name,real_name"`.

### Examples

```java
// select one column
String name = userMapper.getValue(100L, "name");

// count(*) where age = 20
long n = userMapper.getValueByParams(q, null, "count(*)");

// list selected columns
List<User> list = userMapper.listByParams(q, "id asc", "name,realName");

// list by ids
List<User> some = userMapper.listByIds(new Object[]{1L, 2L, 3L}, null);
```

## Customizing conditions

Define a params class (typically extending the entity) and annotate fields to change the operator or supply a custom expression.

### `@QueryColumn` — pick column + operator

```java
public @interface QueryColumn {
    String column()   default "";                       // target column/property; default = the field name
    ColumnOperator operator() default ColumnOperator.DEFAULT;  // DEFAULT = "=" (scalar) or "in" (array)
    String table()    default "";                       // for multi-table queries
}
```

```java
import com.github.easy30.easymybatis.annotation.QueryColumn;
import com.github.easy30.easymybatis.enums.ColumnOperator;

public class UserParams extends User {

    @QueryColumn(column = "createTime", operator = ColumnOperator.GE)
    private Date createTimeStart;          // create_time >= ?

    @QueryColumn(column = "createTime", operator = ColumnOperator.LE)
    private Date createTimeEnd;            // create_time <= ?

    @QueryColumn(column = "id")            // array -> id IN (...)
    private Long[] ids;

    @QueryColumn(column = "id", operator = ColumnOperator.BETWEEN)
    private Long[] idBetween;              // id BETWEEN ? AND ?

    @QueryColumn(column = "name", operator = ColumnOperator.NULL)
    private Boolean nameNull;              // true -> name IS NULL, false -> name IS NOT NULL

    @QueryColumn(column = "age")
    private Range ageRange;                // see Range below
}
```

### Operators (`ColumnOperator`)

| Operator | SQL |
|----------|-----|
| `DEFAULT` | `=` for scalars, `IN` for arrays |
| `EQ` / `NOT_EQ` | `=` / `<>` |
| `GT` / `GE` / `LT` / `LE` | `>` `>=` `<` `<=` |
| `BETWEEN` / `NOT_BETWEEN` | `BETWEEN` / `NOT BETWEEN` (array of 2) |
| `LIKE` / `NOT_LIKE` | `LIKE` / `NOT LIKE` |
| `CONTAIN` / `NOT_CONTAIN` | `LIKE` / `NOT LIKE` |
| `IN` / `NOT_IN` | `IN` / `NOT IN` |
| `NULL` | `IS NULL` / `IS NOT NULL` (driven by a Boolean) |
| `RANGE` | range expression (see `Range`) |

### `@QueryExp` — custom SQL snippet

When you need a hand-written predicate, use `@QueryExp` (the field value binds to the `#{...}` placeholder). If present, it overrides `@QueryColumn`.

```java
import com.github.easy30.easymybatis.annotation.QueryExp;

public class UserParams2 extends User {

    @QueryExp("create_time >= #{createTimeStart}")
    private Date createTimeStart;

    @QueryExp("name like CONCAT('%', #{nameSuffix})")
    private String nameSuffix;
}
```

```java
UserParams2 p = new UserParams2();
p.setAge(20);
p.setCreateTimeStart(date1);
p.setNameSuffix("mer");
List<User> list = userMapper.listByParams(p, "createTime desc", null);
// where age=20 and create_time>=? and name like CONCAT('%', ?) order by create_time desc
```

### `Range` helper

`Range` builds a bounded condition with inclusive/exclusive ends in one shot:

```java
UserParams2 p = new UserParams2();
p.setAgeRange(Range.inRange(70, 95, true, true));   // 70 <= age <= 95
long count = userMapper.getValueByParams(p, null, "count(*)");
```

### `@Query` — class-level query shape

Annotate the params class to set default select columns, base `where`, joins, group/order by, etc.

```java
import com.github.easy30.easymybatis.annotation.Query;

@Query(columns = "id,createTime", where = "1=1 and {createTime} is not null")
public class UserParams2 extends User { ... }
```

## SQL-fragment queries

When a params object isn't expressive enough but you don't want full XML, pass a (partial) SQL string. You can write a full `select`, start from `from`/`where`, or just the predicate. Reference a property as `{prop}` to have it translated to its column.

```java
List<R> listBySQL(String sql, Object params);
```

```java
User p = new User();
p.setAge(20);

// predicate only; {createTime} -> create_time
List<User> list = userMapper.listBySQL("age > #{age} order by {createTime} desc", p);

// with a like and extra named param
Map<String, Object> m = new HashMap<>();
m.put("age", 20);
m.put("namePattern", "coolma%");
List<User> list2 = userMapper.listBySQL(
        "age = #{age} and name like #{namePattern} order by id asc", m);
```

`getValueBySQL` works the same way for a single scalar:

```java
Object name = userMapper.getValueBySQL("select name from user where id = #{id}",
        Collections.singletonMap("id", 100L));
```

## Paging

```java
List<R> pageByParams(Object params, Page page, String orderBy, String selectColumns, SelectOption... options);
List<R> pageBySQL(String sql, Object params, Page page);
```

`Page` is both input (page index + size) and output (data + total count). Counting and dialect-specific limit/offset are handled for you.

```java
User q = new User();
q.setAge(20);

Page<User> page = new Page<>(1, 20);                 // page 1, 20 per page
userMapper.pageByParams(q, page, "id asc", "name,realName");

List<User> rows = page.getData();
int total = page.getRecordCount();
```

```java
// paging with SQL fragment
Map<String, Object> m = new HashMap<>();
m.put("age", 20);
m.put("namePattern", "coolma%");

Page<User> page = new Page<>(1, 10);
userMapper.pageBySQL("age = #{age} and name like #{namePattern} order by id asc", m, page);
```

## Dropping to native MyBatis

For anything complex, just use MyBatis as usual — annotations:

```java
public interface UserMapper extends Mapper<User, User> {
    @Select("select * from user where id = #{id}")
    User findById(@Param("id") long id);
}
```

…or XML (set `mapperLocations` on the `SqlSessionFactoryBean`):

```xml
<mapper namespace="com.example.UserMapper">
    <select id="findById2" parameterType="long" resultType="com.example.User">
        select * from user where id = #{id}
    </select>
</mapper>
```

```java
User getByIdWithXml(long id);   // resolved from the XML above
```

## Related

- [Entity Mapping & Annotations](entity-mapping.md)
- [CRUD & Batch Operations](crud.md)
- [Options & FAQ](options-faq.md)
