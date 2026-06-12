# Upsert (insert-or-update)

`upsertList` performs a batch **insert-or-update**: rows that don't exist are inserted, rows that collide on a unique/primary key are either updated or left untouched. One API, six dialects.

[← Back to README](../README.md)

## Method

```java
int upsertList(@Param(Const.ENTITY_LIST)   List<E> entityList,
               @Param(Const.KEY_COLUMNS)    String[] keyColumns,
               @Param(Const.UPDATE_COLUMNS) String[] updateColumns,
               @Param(Const.OPTIONS)        UpdateOption... options);
```

| Parameter | Meaning |
|-----------|---------|
| `entityList` | Rows to insert-or-update |
| `keyColumns` | Conflict-detection columns (property **or** column names accepted) |
| `updateColumns` | Columns to update on conflict (property **or** column names). `null` = update all |
| `options` | Optional; `upsertIgnore()` switches to do-nothing mode |

The insert column set is computed exactly like `insertList` (all insertable columns, same value-resolution priority). The difference is the conflict clause.

## The three modes

The intent is expressed by `updateColumns` + `UpdateOption.upsertIgnore()` — no magic empty-array sentinels:

| Call | Behavior |
|------|----------|
| `upsertList(list, keys, null)` | **Full update** — on conflict, update all updatable columns except keys and insert-only columns |
| `upsertList(list, keys, new String[]{"description","nodeType"})` | **Selected-column update** — update only the listed columns |
| `upsertList(list, keys, x, UpdateOption.create().upsertIgnore())` | **Conflict ignore** — on conflict do nothing (highest priority; `updateColumns` is irrelevant) |

### Full update

```java
userMapper.upsertList(users, new String[]{"id"}, null);
```

"All updatable columns except keys and insert-only columns" means:

- `@Id` / conflict columns are not updated.
- A column with `@Column(updatable=false)` is excluded.
- An **insert-only** column — one with a `@ColumnDefault` insert value but no update value, e.g. `create_time` with `@ColumnDefault(insertValue="now()")` — is excluded, so the original creation timestamp is never overwritten.
- `update_time` (`@ColumnDefault("now()")`) **is** included and refreshed, because it has an update default.

### Selected-column update

```java
// only real_name is updated on conflict; name/age keep their stored values
userMapper.upsertList(users, new String[]{"id"}, new String[]{"realName"});
```

Property names and column names are interchangeable, so `new String[]{"real_name"}` works too. A listed column that is `@Column(updatable=false)` is silently skipped.

### Conflict ignore

```java
userMapper.upsertList(users, new String[]{"id"}, null,
        UpdateOption.create().upsertIgnore());
// existing rows are left exactly as they are
```

## Conflict columns by dialect

`keyColumns` defines what counts as a "conflict". A matching **unique or primary key must exist on those columns in the database** — the SQL relies on it.

| Dialect | `keyColumns` required? |
|---------|------------------------|
| MySQL / H2 | Optional — any unique/primary key triggers the update |
| PostgreSQL / SQLite | **Required** — used in `ON CONFLICT (...)`; an exception is thrown if missing |
| Oracle / SQL Server | **Required** — used in the `MERGE ... ON (...)` predicate; an exception is thrown if missing |

For portable code, always pass `keyColumns`.

## Generated SQL per dialect

For an entity with insert columns `(name, age)`, conflict key `name`, updating `age`:

**MySQL / H2**
```sql
insert into `user` (name, age) values (?, ?), (?, ?)
on duplicate key update age = values(age)
-- ignore mode:
insert ignore into `user` (name, age) values (?, ?), (?, ?)
```

**PostgreSQL / SQLite**
```sql
insert into "user" (name, age) values (?, ?), (?, ?)
on conflict (name) do update set age = excluded.age
-- ignore mode:
insert into "user" (name, age) values (?, ?), (?, ?)
on conflict (name) do nothing
```

**Oracle** (uses `SELECT ... FROM dual UNION ALL` because it has no `VALUES` table constructor)
```sql
merge into "USER" tgt
using (select ? name, ? age from dual union all select ? name, ? age from dual) src
on (tgt.name = src.name)
when matched then update set tgt.age = src.age
when not matched then insert (name, age) values (src.name, src.age)
-- ignore mode: the "when matched" clause is omitted
```

**SQL Server** (2016+; statement ends with a semicolon)
```sql
merge into [user] tgt
using (values (?, ?), (?, ?)) src (name, age)
on (tgt.name = src.name)
when matched then update set tgt.age = src.age
when not matched then insert (name, age) values (src.name, src.age);
```

## Worked example

```java
// table cai_knowledge_graph_node, unique key uk_graph_name(graph_id, name)

List<Node> batch = ...;   // some new, some existing by (graphId, name)

// full update on conflict
nodeMapper.upsertList(batch, new String[]{"graphId", "name"}, null);

// only refresh description and nodeType, keep everything else
nodeMapper.upsertList(batch, new String[]{"graphId", "name"},
        new String[]{"description", "nodeType"});

// dedupe insert: keep the first version, ignore later collisions
nodeMapper.upsertList(batch, new String[]{"graphId", "name"}, null,
        UpdateOption.create().upsertIgnore());
```

## Notes & caveats

- The database **must** have a unique/primary key on the conflict columns, or no row will ever be treated as a conflict.
- Update values reference the incoming row (`values(col)` / `excluded.col` / `src.col`). Therefore an updatable column must also be insertable to be updatable in an upsert — non-insertable columns aren't present in the incoming row to copy from.
- `create_time`-style insert-only columns are protected from being overwritten in full-update mode.
- Oracle/SQL Server MERGE SQL targets Oracle 12c+ / SQL Server 2016+ syntax.

## Related

- [Entity Mapping & Annotations](entity-mapping.md) — `@ColumnDefault`, `@Column(updatable=…)`
- [CRUD & Batch Operations](crud.md)
- [Options & FAQ](options-faq.md)
