package com.github.easy30.easymybatis.test;

import com.github.easy30.easymybatis.UpdateOption;
import com.github.easy30.easymybatis.test1.User;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * insert / insertList / save 测试。
 */
public class InsertTest extends TestBase {

    /** 跑前清理本类旧数据 */
    @Before
    public void before() {
        cleanOldData();
    }

    @Test
    public void insertOne() {
        User u = newUser("insertOne", 0, 20);
        int n = userMapper.insert(u);
        Assert.assertEquals(1, n);
        Assert.assertNotNull("自增id应回填", u.getId());

        User db = userMapper.get(u.getId(), null);
        Assert.assertNotNull(db);
        Assert.assertEquals(u.getName(), db.getName());
        Assert.assertEquals(Integer.valueOf(20), db.getAge());
        Assert.assertNotNull("createTime 由 @ColumnDefault(insertValue) 填充", db.getCreateTime());
        Assert.assertNotNull("updateTime 由 @ColumnDefault 填充", db.getUpdateTime());
    }

    @Test
    public void insertWithExtraColumnValue() {
        // columnAndValues：用原始 SQL 值覆盖 realName
        User u = newUser("insertExtra", 0, 21);
        int n = userMapper.insert(u, UpdateOption.create()
                .columnAndValues("realName", "'fixedReal'"));
        Assert.assertEquals(1, n);
        User db = userMapper.get(u.getId(), null);
        Assert.assertEquals("fixedReal", db.getRealName());
    }

    @Test
    public void insertList() {
        List<User> users = new ArrayList<>();
        for (int i = 0; i < 3; i++) users.add(newUser("insertList", i, 30 + i));
        int count = userMapper.insertList(users);
        Assert.assertEquals(3, count);
        for (User u : users) {
            User db = userMapper.get(u.getId(), null);
            Assert.assertNotNull(db);
            Assert.assertNotNull(db.getCreateTime());
            Assert.assertNotNull(db.getUpdateTime());
        }
    }

    @Test
    public void insertListWithNullColumns() {
        User u = newUser("insertListNull", 0, 33);
        u.setRealName(null);
        List<User> users = new ArrayList<>();
        users.add(u);
        int count = userMapper.insertList(users, UpdateOption.create().withNullColumns(true));
        Assert.assertEquals(1, count);
        User db = userMapper.get(u.getId(), null);
        Assert.assertNull("withNullColumns 时 realName 应写入 null", db.getRealName());
    }

    @Test
    public void saveInsertBranch() {
        // id 为 null -> 走 insert 分支。注意：save 经 @UpdateProvider 暴露，
        // 自增 id 不会回填到实体（框架既有行为），故用 name 查回验证插入成功。
        User u = newUser("saveInsert", 0, 40);
        userMapper.save(u);
        User params = new User();
        params.setName(u.getName());
        User db = userMapper.getByParams(params, null, null, null);
        Assert.assertNotNull("saveInsert 应已落库", db);
        Assert.assertEquals(Integer.valueOf(40), db.getAge());
    }

    @Test
    public void saveUpdateBranch() {
        // id 非 null 且存在 -> 走 update
        User u = insertUser("saveUpdate", 0, 41);
        User upd = new User();
        upd.setId(u.getId());
        upd.setRealName(classPrefix() + "updated");
        userMapper.save(upd);
        User db = userMapper.get(u.getId(), null);
        Assert.assertEquals(classPrefix() + "updated", db.getRealName());
    }
}
