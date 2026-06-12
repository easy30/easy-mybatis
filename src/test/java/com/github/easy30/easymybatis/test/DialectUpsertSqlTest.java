package com.github.easy30.easymybatis.test;

import com.github.easy30.easymybatis.dialect.*;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * 方言 upsert SQL 生成单元测试（不连数据库）。
 * <p>直接调用各 Dialect.getUpsertSql，断言生成的 SQL 文本，覆盖 6 个方言。</p>
 * <p>入参模拟 Provider 归一化后的结果：列名已加引号、值为占位符或常量。</p>
 */
public class DialectUpsertSqlTest {

    // 模拟两列 name、age，两行数据
    private List<String> cols() {
        return Arrays.asList("name", "age");
    }

    private List<List<String>> rows() {
        return Arrays.asList(
                Arrays.asList("#{es[0].name}", "#{es[0].age}"),
                Arrays.asList("#{es[1].name}", "#{es[1].age}"));
    }

    private List<String> keys() {
        return Arrays.asList("name");
    }

    /** 全量更新 age 列（name 是冲突列被排除） */
    private LinkedHashMap<String, String> updateAge() {
        LinkedHashMap<String, String> m = new LinkedHashMap<>();
        m.put("age", null);// null 表示引用新插入值
        return m;
    }

    private LinkedHashMap<String, String> updateAgeWithFunc() {
        LinkedHashMap<String, String> m = new LinkedHashMap<>();
        m.put("age", null);
        m.put("update_time", "now()");// 字面 SQL
        return m;
    }

    @Test
    public void mysqlUpdate() {
        String sql = new MysqlDialect().getUpsertSql("`user`", cols(), rows(), keys(), updateAge(), false);
        System.out.println("MySQL: " + sql);
        Assert.assertTrue(sql.toLowerCase().contains("insert into `user`"));
        Assert.assertTrue(sql.toLowerCase().contains("on duplicate key update"));
        Assert.assertTrue("引用新值用 values()", sql.contains("age=values(age)"));
    }

    @Test
    public void mysqlIgnore() {
        String sql = new MysqlDialect().getUpsertSql("`user`", cols(), rows(), keys(), updateAge(), true);
        System.out.println("MySQL ignore: " + sql);
        Assert.assertTrue(sql.toLowerCase().contains("insert ignore into"));
        Assert.assertFalse(sql.toLowerCase().contains("on duplicate"));
    }

    @Test
    public void mysqlUpdateWithFunc() {
        String sql = new MysqlDialect().getUpsertSql("`user`", cols(), rows(), keys(), updateAgeWithFunc(), false);
        System.out.println("MySQL func: " + sql);
        Assert.assertTrue(sql.contains("update_time=now()"));
        Assert.assertTrue(sql.contains("age=values(age)"));
    }

    @Test
    public void h2InheritsMysql() {
        String sql = new H2Dialect().getUpsertSql("`user`", cols(), rows(), keys(), updateAge(), false);
        System.out.println("H2: " + sql);
        Assert.assertTrue(sql.toLowerCase().contains("on duplicate key update"));
    }

    @Test
    public void postgresqlUpdate() {
        String sql = new PostgresqlDialect().getUpsertSql("\"user\"", cols(), rows(), keys(), updateAge(), false);
        System.out.println("PG: " + sql);
        Assert.assertTrue(sql.toLowerCase().contains("on conflict (name)"));
        Assert.assertTrue(sql.toLowerCase().contains("do update set"));
        Assert.assertTrue("引用新值用 excluded", sql.contains("age=excluded.age"));
    }

    @Test
    public void postgresqlIgnore() {
        String sql = new PostgresqlDialect().getUpsertSql("\"user\"", cols(), rows(), keys(), updateAge(), true);
        System.out.println("PG ignore: " + sql);
        Assert.assertTrue(sql.toLowerCase().contains("do nothing"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void postgresqlRequiresKeyColumns() {
        // PG 必须指定冲突列，为空应抛异常
        new PostgresqlDialect().getUpsertSql("\"user\"", cols(), rows(), null, updateAge(), false);
    }

    @Test
    public void sqliteUpdate() {
        String sql = new SqliteDialect().getUpsertSql("\"user\"", cols(), rows(), keys(), updateAge(), false);
        System.out.println("SQLite: " + sql);
        Assert.assertTrue(sql.toLowerCase().contains("on conflict (name)"));
        Assert.assertTrue(sql.contains("age=excluded.age"));
    }

    @Test
    public void oracleMerge() {
        String sql = new OracleDialect().getUpsertSql("\"USER\"", cols(), rows(), keys(), updateAge(), false);
        System.out.println("Oracle: " + sql);
        String low = sql.toLowerCase();
        Assert.assertTrue(low.contains("merge into \"user\" tgt"));
        Assert.assertTrue("Oracle 用 dual", low.contains("from dual"));
        Assert.assertTrue(low.contains("when matched then update"));
        Assert.assertTrue(low.contains("when not matched then insert"));
        Assert.assertTrue("更新引用源别名", sql.contains("tgt.age=src.age"));
    }

    @Test
    public void oracleMergeIgnore() {
        String sql = new OracleDialect().getUpsertSql("\"USER\"", cols(), rows(), keys(), updateAge(), true);
        System.out.println("Oracle ignore: " + sql);
        String low = sql.toLowerCase();
        Assert.assertFalse("忽略时不应有 when matched", low.contains("when matched then update"));
        Assert.assertTrue(low.contains("when not matched then insert"));
    }

    @Test
    public void sqlserverMerge() {
        String sql = new SqlserverDialect().getUpsertSql("[user]", cols(), rows(), keys(), updateAge(), false);
        System.out.println("SQLServer: " + sql);
        String low = sql.toLowerCase();
        Assert.assertTrue(low.contains("merge into [user] tgt"));
        Assert.assertTrue("SQLServer 用 values 子句", low.contains("values "));
        Assert.assertTrue(low.contains("when matched then update"));
        Assert.assertTrue(low.contains("when not matched then insert"));
        Assert.assertTrue("语句以分号结尾", sql.trim().endsWith(";"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void sqlserverRequiresKeyColumns() {
        new SqlserverDialect().getUpsertSql("[user]", cols(), rows(), null, updateAge(), false);
    }
}
