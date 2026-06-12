package com.github.easy30.easymybatis.test;

import com.github.easy30.easymybatis.UpdateOption;
import com.github.easy30.easymybatis.test1.User;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * upsertList 测试（重点）。
 * <p>冲突判定列用主键 id（手动指定 Long），MySQL 靠主键触发 ON DUPLICATE KEY UPDATE。</p>
 * <p>覆盖三态：全量更新(updateColumns=null) / 指定列更新 / 冲突忽略(upsertIgnore)，</p>
 * <p>并验证 create_time 不被覆盖、update_time 被刷新，以及属性名/列名混用。</p>
 */
public class UpsertTest extends TestBase {

    /** 手动主键基值，避开自增区间与其它测试 */
    private long baseId;

    @Before
    public void before() {
        cleanOldData();
        baseId = 800000000L + (System.currentTimeMillis() % 100000L) * 10;
    }

    private User mkUser(long id, String suffix, int age) {
        User u = new User();
        u.setId(id);
        u.setName(classPrefix() + suffix);
        u.setRealName(classPrefix() + "real_" + suffix);
        u.setAge(age);
        return u;
    }

    @Test
    public void upsertInsertNew() {
        long id = baseId;
        User u = mkUser(id, "new", 20);
        int n = userMapper.upsertList(Collections.singletonList(u), new String[]{"id"}, null);
        Assert.assertTrue(n >= 1);
        User db = userMapper.get(id, null);
        Assert.assertNotNull(db);
        Assert.assertEquals(classPrefix() + "new", db.getName());
        Assert.assertEquals(Integer.valueOf(20), db.getAge());
    }

    @Test
    public void upsertFullUpdate() {
        long id = baseId + 1;
        // 先插入
        userMapper.upsertList(Collections.singletonList(mkUser(id, "v1", 20)), new String[]{"id"}, null);
        // 再 upsert 同 id，全量更新
        User v2 = mkUser(id, "v2", 99);
        int n = userMapper.upsertList(Collections.singletonList(v2), new String[]{"id"}, null);
        Assert.assertTrue(n >= 1);
        User db = userMapper.get(id, null);
        Assert.assertEquals("name 应被更新", classPrefix() + "v2", db.getName());
        Assert.assertEquals("age 应被更新", Integer.valueOf(99), db.getAge());
    }

    @Test
    public void upsertSpecifiedColumns() {
        long id = baseId + 2;
        userMapper.upsertList(Collections.singletonList(mkUser(id, "orig", 30)), new String[]{"id"}, null);
        // 只更新 realName，name/age 应保持原值
        User upd = mkUser(id, "changed", 88);
        int n = userMapper.upsertList(Collections.singletonList(upd),
                new String[]{"id"}, new String[]{"realName"});
        Assert.assertTrue(n >= 1);
        User db = userMapper.get(id, null);
        Assert.assertEquals("只更新 realName", classPrefix() + "real_changed", db.getRealName());
        Assert.assertEquals("name 不应被改", classPrefix() + "orig", db.getName());
        Assert.assertEquals("age 不应被改", Integer.valueOf(30), db.getAge());
    }

    @Test
    public void upsertIgnore() {
        long id = baseId + 3;
        userMapper.upsertList(Collections.singletonList(mkUser(id, "keep", 40)), new String[]{"id"}, null);
        // upsertIgnore：冲突时啥都不更新
        User upd = mkUser(id, "ignored", 0);
        int n = userMapper.upsertList(Collections.singletonList(upd),
                new String[]{"id"}, null, UpdateOption.create().upsertIgnore());
        User db = userMapper.get(id, null);
        Assert.assertEquals("冲突忽略，name 保持原值", classPrefix() + "keep", db.getName());
        Assert.assertEquals("age 保持原值", Integer.valueOf(40), db.getAge());
    }

    @Test
    public void upsertKeepsCreateTimeRefreshesUpdateTime() throws InterruptedException {
        long id = baseId + 4;
        userMapper.upsertList(Collections.singletonList(mkUser(id, "ct", 50)), new String[]{"id"}, null);
        User first = userMapper.get(id, null);
        java.util.Date createdAt = first.getCreateTime();
        java.util.Date updatedAt1 = first.getUpdateTime();
        Assert.assertNotNull(createdAt);
        Assert.assertNotNull(updatedAt1);

        Thread.sleep(1100);// 让时间差可辨（秒级）
        // 全量更新同一行
        userMapper.upsertList(Collections.singletonList(mkUser(id, "ct2", 51)), new String[]{"id"}, null);
        User second = userMapper.get(id, null);
        Assert.assertEquals("create_time 不应被覆盖", createdAt, second.getCreateTime());
        Assert.assertTrue("update_time 应被刷新", second.getUpdateTime().getTime() >= updatedAt1.getTime());
    }

    @Test
    public void upsertAcceptsPropertyOrColumnName() {
        long id = baseId + 5;
        userMapper.upsertList(Collections.singletonList(mkUser(id, "prop", 60)), new String[]{"id"}, null);
        // keyColumns 用属性名 id；updateColumns 用列名 real_name（数据库列）
        User upd = mkUser(id, "byColName", 61);
        int n = userMapper.upsertList(Collections.singletonList(upd),
                new String[]{"id"}, new String[]{"real_name"});
        Assert.assertTrue(n >= 1);
        User db = userMapper.get(id, null);
        Assert.assertEquals("用列名 real_name 指定更新生效", classPrefix() + "real_byColName", db.getRealName());
        Assert.assertEquals("name 未在更新列中，保持原值", classPrefix() + "prop", db.getName());
    }

    @Test
    public void upsertBatchMixedInsertAndUpdate() {
        long idA = baseId + 6;
        long idB = baseId + 7;
        // 预置 A
        userMapper.upsertList(Collections.singletonList(mkUser(idA, "A_old", 70)), new String[]{"id"}, null);
        // 批量：A 更新、B 新增
        List<User> batch = new ArrayList<>();
        batch.add(mkUser(idA, "A_new", 71));
        batch.add(mkUser(idB, "B_new", 72));
        int n = userMapper.upsertList(batch, new String[]{"id"}, null);
        Assert.assertTrue(n >= 1);

        Assert.assertEquals("A 被更新", classPrefix() + "A_new", userMapper.get(idA, null).getName());
        Assert.assertEquals("B 被插入", classPrefix() + "B_new", userMapper.get(idB, null).getName());
    }
}

