package com.github.easy30.easymybatis.test;

import com.github.easy30.easymybatis.UpdateOption;
import com.github.easy30.easymybatis.test1.User;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;

/**
 * update / updateByParams / updateByCondition 测试。
 */
public class UpdateTest extends TestBase {

    @Before
    public void before() {
        cleanOldData();
    }

    @Test
    public void updateById() {
        User u = insertUser("updateById", 0, 25);
        User upd = new User();
        upd.setId(u.getId());
        upd.setRealName(classPrefix() + "newReal");
        int n = userMapper.update(upd);
        Assert.assertEquals(1, n);

        User db = userMapper.get(u.getId(), null);
        Assert.assertEquals(classPrefix() + "newReal", db.getRealName());
        // 未设置的字段不应被改动（name 保持原值）
        Assert.assertEquals(u.getName(), db.getName());
    }

    @Test
    public void updateWithNullColumns() {
        User u = insertUser("updateNull", 0, 26);
        Assert.assertNotNull(userMapper.get(u.getId(), null).getRealName());

        // withNullColumns：把未赋值字段也置为 null
        User upd = new User();
        upd.setId(u.getId());
        upd.setName(u.getName());// 保留 name 否则也会被置 null
        int n = userMapper.update(upd, UpdateOption.create().withNullColumns(true));
        Assert.assertEquals(1, n);
        User db = userMapper.get(u.getId(), null);
        Assert.assertNull("realName 应被置为 null", db.getRealName());
    }

    @Test
    public void updateIgnoreColumns() {
        User u = insertUser("updateIgnore", 0, 27);
        User upd = new User();
        upd.setId(u.getId());
        upd.setName(classPrefix() + "changedName");
        upd.setRealName(classPrefix() + "shouldIgnore");
        // 忽略 realName，只更新 name
        int n = userMapper.update(upd, UpdateOption.create().ignoreColumns("realName"));
        Assert.assertEquals(1, n);
        User db = userMapper.get(u.getId(), null);
        Assert.assertEquals(classPrefix() + "changedName", db.getName());
        Assert.assertEquals("realName 被忽略，保持原值", u.getRealName(), db.getRealName());
    }

    @Test
    public void updateByParams() {
        User u = insertUser("updateByParams", 0, 28);
        User upd = new User();
        upd.setRealName(classPrefix() + "byParams");
        // 条件：id+age 都匹配才更新
        int n = userMapper.updateByParams(upd, u, "id,age", null);
        Assert.assertEquals(1, n);
        User db = userMapper.get(u.getId(), null);
        Assert.assertEquals(classPrefix() + "byParams", db.getRealName());
    }

    @Test
    public void updateByParamsWithColumnAndValues() {
        User u = insertUser("updateByParamsCV", 0, 29);
        User upd = new User();
        upd.setName(classPrefix() + "cvName");
        int n = userMapper.updateByParams(upd, u, "id,age",
                UpdateOption.create().columnAndValues("realName", "'cvReal'"));
        Assert.assertEquals(1, n);
        User db = userMapper.get(u.getId(), null);
        Assert.assertEquals(classPrefix() + "cvName", db.getName());
        Assert.assertEquals("cvReal", db.getRealName());
    }

    @Test
    public void updateByCondition() {
        User u = insertUser("updateByCond", 0, 31);
        User upd = new User();
        upd.setRealName(classPrefix() + "byCond");
        String where = "{id}=#{id} and {name}=#{name}";
        Map<String, Object> params = mapOf("id", u.getId(), "name", u.getName());
        int n = userMapper.updateByCondition(upd, where, params, null);
        Assert.assertEquals(1, n);
        User db = userMapper.get(u.getId(), null);
        Assert.assertEquals(classPrefix() + "byCond", db.getRealName());
    }
}
