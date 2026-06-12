package com.github.easy30.easymybatis.test;

import com.github.easy30.easymybatis.test1.User;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;

/**
 * deleteById / deleteByParams / deleteByCondition 测试。
 */
public class DeleteTest extends TestBase {

    @Before
    public void before() {
        cleanOldData();
    }

    @Test
    public void deleteById() {
        User u = insertUser("deleteById", 0, 50);
        int n = userMapper.deleteById(u.getId(), null);
        Assert.assertEquals(1, n);
        Assert.assertNull(userMapper.get(u.getId(), null));
    }

    @Test
    public void deleteByParams() {
        User u = insertUser("deleteByParams", 0, 51);
        // 按 id+age 删除
        int n = userMapper.deleteByParams(u, "id,age", null);
        Assert.assertEquals(1, n);
        Assert.assertNull(userMapper.get(u.getId(), null));
    }

    @Test
    public void deleteByParamsNoMatch() {
        User u = insertUser("deleteByParamsNoMatch", 0, 52);
        User cond = new User();
        cond.setId(u.getId());
        cond.setAge(9999);// age 不匹配
        int n = userMapper.deleteByParams(cond, "id,age", null);
        Assert.assertEquals(0, n);
        Assert.assertNotNull("条件不匹配不应删除", userMapper.get(u.getId(), null));
        // 收尾
        userMapper.deleteById(u.getId(), null);
    }

    @Test
    public void deleteByCondition() {
        User u = insertUser("deleteByCond", 0, 53);
        String where = "{id}=#{id} and {name}=#{name}";
        Map<String, Object> params = mapOf("id", u.getId(), "name", u.getName());
        int n = userMapper.deleteByCondition(where, params, null);
        Assert.assertEquals(1, n);
        Assert.assertNull(userMapper.get(u.getId(), null));
    }
}
