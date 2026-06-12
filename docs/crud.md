# CRUD & Batch Operations

Full reference for the write side of `Mapper<E, R>`: insert, update, save, delete and batch insert. Upsert has its own page: [docs/upsert.md](upsert.md).

[← Back to README](../README.md)

All write methods accept an optional trailing `UpdateOption... options` (or `DeleteOption...`). See [Options & FAQ](options-faq.md) for the full option list. Examples below omit it unless relevant.

## insert

```java
int insert(@Param(Const.ENTITY) E entity, @Param(Const.OPTIONS) UpdateOption... options);
```

Inserts one row. Only properties that resolve to a value are written; the value for each column is resolved in this priority order:

1. `UpdateOption.columnAndValues(...)` — explicit raw-SQL value
2. entity property value (non-null, or null when tracked as changed)
3. dialect value (for entities extending `DialectEntity`)
4. `@ColumnGeneration` generated value
5. `@ColumnDefault` insert default
6. `null` — only if `UpdateOption.withNullColumns(true)`

The generated key is filled back into the entity.

```java
User u = new User();
u.setName("coolma");
u.setAge(20);
userMapper.insert(u);
Long id = u.getId();   // generated
```

## insertList — bulk insert

```java
int insertList(@Param(Const.ENTITY_LIST) List<E> entityList, @Param(Const.OPTIONS) UpdateOption... options);
```

Inserts many rows in a single `insert ... values (...), (...), ...` statement. The column set is the union of insertable columns across the list.

```java
List<User> users = new ArrayList<>();
for (int i = 0; i < 3; i++) {
    User u = new User();
    u.setName("user" + i);
    u.setAge(20 + i);
    users.add(u);
}
int rows = userMapper.insertList(users);
```

To force null columns to be written explicitly:

```java
userMapper.insertList(users, UpdateOption.create().withNullColumns(true));
```

> For large batches, add `rewriteBatchedStatements=true` to your MySQL JDBC URL so the driver collapses the multi-row insert efficiently.

## update — by id

```java
int update(@Param(Const.ENTITY) E entity, @Param(Const.OPTIONS) UpdateOption... options);
```

Updates the row matching the entity's `@Id`. By default only **non-null** properties are written, so you can patch selectively:

```java
User patch = new User();
patch.setId(100L);
patch.setRealName("michael");
userMapper.update(patch);
// update user set real_name = ?, update_time = now() where id = 100
```

`update_time` appears automatically because of `@ColumnDefault("now()")`.

### Write null columns

```java
User patch = new User();
patch.setId(100L);
patch.setName("keepThis");
userMapper.update(patch, UpdateOption.create().withNullColumns(true));
// every updatable column not set is written as NULL (realName -> null, etc.)
```

### Ignore specific columns

```java
userMapper.update(patch, UpdateOption.create().ignoreColumns("realName"));
// real_name is left untouched even if set on the entity
```

### Raw-SQL column values

```java
userMapper.update(patch, UpdateOption.create().columnAndValues("realName", "'fixed'"));
// set real_name = 'fixed'
```

## save — insert or update

```java
int save(@Param(Const.ENTITY) E entity, @Param(Const.OPTIONS) UpdateOption... options);
```

If the entity's id is null → insert; otherwise → update.

```java
User u = new User();
u.setName("new");
userMapper.save(u);          // insert branch

u.setRealName("changed");
userMapper.save(u);          // update branch (id now set... but see note)
```

> When `save` takes the insert branch, the auto-increment key is **not** filled back into the entity (it is exposed via the update provider). If you need the generated id, call `insert()` directly.

## updateByParams — update with a condition object

```java
int updateByParams(@Param(Const.ENTITY) E entity,
                   @Param(Const.PARAMS) Object params,
                   @Param(Const.PARAM_NAMES) String paramNames,
                   @Param(Const.OPTIONS) UpdateOption... options);
```

`entity` holds the new values; `params` holds the condition; `paramNames` lists which properties of `params` form the `where` (comma-separated). Requiring explicit `paramNames` prevents accidental full-table updates.

```java
User values = new User();
values.setRealName("tom");

User cond = new User();
cond.setId(100L);
cond.setAge(20);

userMapper.updateByParams(values, cond, "id,age");
// update user set real_name='tom', update_time=now() where id=100 and age=20
```

Combine with options:

```java
userMapper.updateByParams(values, cond, "id,age",
        UpdateOption.create().columnAndValues("realName", "'tom4'"));
```

## updateByCondition — update with a custom where

```java
int updateByCondition(@Param(Const.ENTITY) E entity,
                      @Param(Const.CONDITION) String condition,
                      @Param(Const.PARAMS) Object params,
                      @Param(Const.OPTIONS) UpdateOption... options);
```

`condition` is a where fragment. You may reference columns directly (`real_name=#{realName}`) or by property in braces (`{realName}=#{realName}`), which is translated to the mapped column.

```java
User values = new User();
values.setRealName("byCond");

String where = "{id}=#{id} and {name}=#{name}";
Map<String, Object> params = new HashMap<>();
params.put("id", 100L);
params.put("name", "coolma");

userMapper.updateByCondition(values, where, params);
```

## deleteById

```java
int deleteById(@Param(Const.ID) Object id, @Param(Const.OPTIONS) DeleteOption... options);
```

```java
userMapper.deleteById(100L);
```

## deleteByParams — delete with a condition object

```java
int deleteByParams(@Param(Const.PARAMS) Object params,
                   @Param(Const.PARAM_NAMES) String paramNames,
                   @Param(Const.OPTIONS) DeleteOption... options);
```

```java
User cond = new User();
cond.setId(100L);
cond.setAge(20);
userMapper.deleteByParams(cond, "id,age");
// delete from user where id=100 and age=20
```

Only the properties named in `paramNames` are used, so unrelated non-null fields won't widen or narrow the delete unexpectedly.

## deleteByCondition — delete with a custom where

```java
int deleteByCondition(@Param(Const.CONDITION) String condition,
                      @Param(Const.PARAMS) Object params,
                      @Param(Const.OPTIONS) DeleteOption... options);
```

```java
String where = "{id}=#{id} and {name}=#{name}";
Map<String, Object> params = new HashMap<>();
params.put("id", 100L);
params.put("name", "coolma");
userMapper.deleteByCondition(where, params);
```

## Related

- [Upsert (insert-or-update)](upsert.md)
- [Queries & Paging](queries.md)
- [Options & FAQ](options-faq.md)
