# Entity Mapping & Annotations

easy-mybatis maps a plain Java class to a table using JPA-style annotations plus a few of its own. This page covers every mapping annotation.

[← Back to README](../README.md)

## `@Table` — table name

```java
import javax.persistence.Table;

@Table(name = "user")
public class User { ... }
```

If `@Table` is omitted, the simple class name is used as the table name.

## `@Id` — primary key

```java
import javax.persistence.Id;

@Id
private Long id;
```

- For an **auto-increment** key, the generated value is filled back into the entity after `insert()`.
- For a **string/UUID** key, combine with `@ColumnGeneration` (see below).
- Composite keys are supported by annotating more than one field with `@Id`.

> Note: `save()` is exposed through MyBatis' update provider. When it takes the insert branch (id is null), an auto-increment key is **not** filled back into the entity. Use `insert()` if you need the generated id.

## Column naming — camelCase ⇄ snake_case

By default a property maps to its snake_case column:

| Property | Column |
|----------|--------|
| `name` | `name` |
| `realName` | `real_name` |
| `createTime` | `create_time` |

Override explicitly with `@Column`:

```java
import javax.persistence.Column;

@Column(name = "nick_name")
private String nick;
```

`@Column` also controls insertability/updatability:

```java
@Column(insertable = false)            // never written on insert
private String generatedCode;

@Column(updatable = false)             // never written on update/upsert-update
private Date createdAt;
```

These flags are honored everywhere, including upsert: a column with `updatable=false` that is explicitly listed in `updateColumns` is **silently skipped** (consistent with JPA semantics).

## `@ColumnDefault` — SQL default value / function

Use this when the value should come from a SQL expression (a function like `now()` or a literal), not from the Java object.

```java
public @interface ColumnDefault {
    String value()       default "";   // used on BOTH insert and update
    String insertValue() default "";   // used on insert only
    String updateValue() default "";   // used on update only
}
```

Typical audit columns:

```java
@ColumnDefault(insertValue = "now()")   // set once, on insert
private Date createTime;

@ColumnDefault("now()")                 // refreshed on insert AND update
private Date updateTime;
```

Resulting SQL:

```sql
-- insert
insert into user (name, create_time, update_time) values (?, now(), now())
-- update
update user set name = ?, update_time = now() where id = ?
```

The default is applied **only when the corresponding property is null** on the entity; an explicit value always wins.

This also drives upsert's "full update" mode: a column that has `insertValue` but no update default (i.e. insert-only, like `create_time`) is excluded from the update clause so it is never overwritten on conflict. See [docs/upsert.md](upsert.md).

## `@ColumnGeneration` — programmatic value generation

Generate a column value from Java code (commonly a UUID primary key) via a Spring bean implementing `Generation`.

```java
public @interface ColumnGeneration {
    String insertGeneration() default "";   // bean name used on insert
    String insertMethod()     default "generate";
    String insertArg()        default "";

    String updateGeneration() default "";   // bean name used on update
    String updateMethod()     default "generate";
    String updateArg()        default "";

    String generation()       default "";   // bean used on insert OR update
    String method()           default "generate";
    String arg()              default "";
}
```

### Built-in UUID generator

Register the bundled `UUIDGeneration` bean (named `uuid`) once:

```xml
<bean class="com.github.easy30.easymybatis.generation.UUIDGeneration"/>
```

Then use it on a string id:

```java
@Id
@ColumnGeneration(insertGeneration = "uuid")
private String id;
```

On insert, a UUID is generated and assigned before the row is written.

### Custom generator

```java
@Component("seqGen")
public class SeqGeneration implements Generation {
    @Override
    public Object generate(String table, Object entity, String prop, Class propType, String arg) {
        return mySequence.next();
    }
}
```

```java
@ColumnGeneration(insertGeneration = "seqGen")
private Long id;
```

## Putting it together

```java
import lombok.Data;
import javax.persistence.*;
import com.github.easy30.easymybatis.annotation.ColumnDefault;
import com.github.easy30.easymybatis.annotation.ColumnGeneration;

@Data
@Table(name = "user")
public class User {

    @Id
    @ColumnGeneration(insertGeneration = "uuid")
    private String id;

    private String name;          // -> name
    private Integer age;          // -> age
    private String realName;      // -> real_name

    @Column(name = "nick_name")
    private String nick;          // -> nick_name

    @ColumnDefault(insertValue = "now()")
    private Date createTime;      // -> create_time, insert only

    @ColumnDefault("now()")
    private Date updateTime;      // -> update_time, insert + update
}
```

## Related

- [CRUD & Batch Operations](crud.md)
- [Upsert](upsert.md)
- [Queries & Paging](queries.md)
