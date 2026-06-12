# Options & FAQ

Per-call options let you tweak a single `Mapper` call without changing the entity or writing SQL. Every CRUD method takes an optional trailing varargs option.

[← Back to README](../README.md)

## Option types

| Option | Used by |
|--------|---------|
| `UpdateOption` | `insert`, `insertList`, `upsertList`, `update`, `save`, `updateByParams`, `updateByCondition` |
| `SelectOption` | `get`, `getByParams`, `getValue*`, `listByParams`, `listByIds`, `pageByParams` |
| `DeleteOption` | `deleteById`, `deleteByParams`, `deleteByCondition` |

All are created with a static `create()` and use a fluent builder style. They all share a common base (`table`, `ignoreQueryAnnotation`, `queryEmptyStringParam`).

## UpdateOption

```java
UpdateOption.create()
    .table("user_2024")                       // override the target table
    .ignoreColumns("realName", "age")         // skip these columns on write
    .withNullColumns(true)                    // write nulls instead of skipping null props
    .upsertIgnore()                           // upsertList: do nothing on conflict
    .columnAndValues("realName", "'fixed'")   // raw-SQL column value(s)
    .columnAndValueParams("k", v);            // named params for columnAndValues placeholders
```

| Method | Effect |
|--------|--------|
| `table(String)` | Write to a different table name |
| `ignoreColumns(String...)` | Exclude columns (property or column names) from the statement |
| `withNullColumns(boolean)` | Include null-valued properties as explicit `NULL` (default: skip them) |
| `upsertIgnore()` | In `upsertList`, switch to conflict-ignore (do nothing) mode |
| `columnAndValues(String...)` | Pairs of column, raw-SQL-value — e.g. `("createTime", "now()", "n", "#{x}")` |
| `columnAndValueParams(Object...)` | Name/value pairs binding placeholders used in `columnAndValues` |

### Examples

```java
// patch but blank out realName explicitly
User patch = new User();
patch.setId(100L);
patch.setName("keep");
userMapper.update(patch, UpdateOption.create().withNullColumns(true));

// update everything except realName
userMapper.update(patch, UpdateOption.create().ignoreColumns("realName"));

// set a column from a SQL expression
userMapper.update(patch, UpdateOption.create().columnAndValues("updateTime", "now()"));

// parameterized raw value
userMapper.update(patch, UpdateOption.create()
        .columnAndValues("realName", "#{rn}")
        .columnAndValueParams("rn", "dynamic"));

// route to a sharded table
userMapper.insert(u, UpdateOption.create().table("user_2024"));
```

## SelectOption

```java
SelectOption.create()
    .table("user_2024")
    .ignoreQueryAnnotation(true)        // ignore class-level @Query on the params object
    .queryEmptyStringParam(true)        // treat "" as a real condition
    .foreignColumnThreshold(-1);        // -1 = no limit on foreign-column expansion
```

```java
// query against an archive table
userMapper.listByParams(q, null, null, SelectOption.create().table("user_archive"));
```

## DeleteOption

```java
DeleteOption.create().table("user_2024");
```

`DeleteOption` exposes `table(...)` plus the shared base options.

---

## FAQ

### How are null properties handled on write?

By default, a null property is **skipped** (not written), which is what makes `update` a partial patch. Set `withNullColumns(true)` to force nulls to be written as `NULL`.

### How do I write a column from a SQL function (e.g. `now()`)?

Two ways:

1. On the entity, declaratively: `@ColumnDefault("now()")` (insert + update) or `@ColumnDefault(insertValue="now()")` (insert only). See [Entity Mapping](entity-mapping.md).
2. Per call: `UpdateOption.create().columnAndValues("updateTime", "now()")`.

### Querying for an empty string `""`

By default `queryEmptyStringParam` is `false`, so an empty-string property is ignored (treated like null) in params queries. Enable it globally or per call:

```java
// global
EasyConfiguration.setQueryEmptyStringParam(true);

// per call: params {name:""} -> ... where name = ''
mapper.listByParams(params, null, null,
        SelectOption.create().queryEmptyStringParam(true));
```

### Do column arguments take property names or column names?

Both. `ignoreColumns`, `selectColumns`, upsert `keyColumns`/`updateColumns`, and `{prop}` references in condition fragments all accept either the Java property (`realName`) or the database column (`real_name`).

### Does `save()` return the generated id on insert?

No. `save()` is exposed via the update provider, so its insert branch does not back-fill an auto-increment key. Call `insert()` directly when you need the generated id. See [CRUD](crud.md).

### Which databases are supported for upsert?

MySQL, H2, PostgreSQL, SQLite, Oracle (12c+) and SQL Server (2016+). MySQL/H2 can detect the conflict via any unique/primary key; the others require explicit `keyColumns`. See [Upsert](upsert.md).

### How do I do a complex query easy-mybatis can't express?

Use native MyBatis: a `@Select` annotation on a method, or an XML statement. Both coexist with the generated methods on the same mapper. See [Queries & Paging → Dropping to native MyBatis](queries.md#dropping-to-native-mybatis).

### What MyBatis / Spring versions are required?

MyBatis `>= 3.5.1`; mybatis-spring `1.3.x` for Spring 3.2.2+, `2.0.x` for Spring 5.0+. Java 8+.

## Related

- [Entity Mapping & Annotations](entity-mapping.md)
- [CRUD & Batch Operations](crud.md)
- [Upsert](upsert.md)
- [Queries & Paging](queries.md)
