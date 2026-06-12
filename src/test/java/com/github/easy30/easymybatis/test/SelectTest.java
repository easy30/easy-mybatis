package com.github.easy30.easymybatis.test;

import com.github.easy30.easymybatis.test1.User;
import com.github.easy30.easymybatis.test1.UserDto;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * get / getByParams / getValue* / listByParams / listByIds / listBySQL 测试。
 * <p>本类用独有 age=7777 标记数据，list 类断言只在该 age 范围内统计，避免与其它测试互相影响。</p>
 */
public class SelectTest extends TestBase {

    /** 本类专用 age，避免与别的测试数据混淆 */
    private static final int MARK_AGE = 7777;

    private List<User> seeded;

    @Before
    public void before() {
        cleanOldData();
        // 预置 3 条数据
        seeded = new ArrayList<>();
        for (int i = 0; i < 3; i++) seeded.add(insertUser("seed", i, MARK_AGE));
    }

    @Test
    public void getById() {
        User u = seeded.get(0);
        User db = userMapper.get(u.getId(), null);
        Assert.assertNotNull(db);
        Assert.assertEquals(u.getName(), db.getName());
    }

    @Test
    public void getByIdWithColumns() {
        User u = seeded.get(0);
        // 只查 name 列
        UserDto db = userMapper.get(u.getId(), "name");
        Assert.assertNotNull(db);
        Assert.assertEquals(u.getName(), db.getName());
        Assert.assertNull("未查询 age 列应为 null", db.getAge());
    }

    @Test
    public void getByParams() {
        User u = seeded.get(1);
        User params = new User();
        params.setId(u.getId());
        UserDto db = userMapper.getByParams(params, "id desc", null, null);
        Assert.assertNotNull(db);
        Assert.assertEquals(u.getId(), db.getId());
    }

    @Test
    public void getValueById() {
        User u = seeded.get(0);
        String nm = userMapper.getValue(u.getId(), "name");
        Assert.assertEquals(u.getName(), nm);
    }

    @Test
    public void getValueByParams() {
        User u = seeded.get(0);
        User params = new User();
        params.setId(u.getId());
        Object v = userMapper.getValueByParams(params, null, "name", null);
        Assert.assertEquals(u.getName(), v);
    }

    @Test
    public void getValueByCondition() {
        User u = seeded.get(0);
        User params = new User();
        params.setId(u.getId());
        Object v = userMapper.getValueByCondition("{id}=#{id}", params, "name", null);
        Assert.assertEquals(u.getName(), v);
    }

    @Test
    public void getCountByParams() {
        // count(*) where age=MARK_AGE and name like classPrefix%
        User params = new User();
        params.setAge(MARK_AGE);
        long count = userMapper.getValueByParams(params, null, "count(*)", null);
        Assert.assertTrue("至少 3 条", count >= 3);
    }

    @Test
    public void getValueBySQL() {
        User u = seeded.get(0);
        Map<String, Object> params = mapOf("id", u.getId());
        Object v = userMapper.getValueBySQL("select name from user where id=#{id}", params);
        Assert.assertEquals(u.getName(), v);
    }

    @Test
    public void listByParams() {
        User params = new User();
        params.setAge(MARK_AGE);
        List<UserDto> list = userMapper.listByParams(params, "id asc", null, null);
        // 过滤出本类前缀数据
        long mine = list.stream().filter(x -> x.getName().startsWith(classPrefix())).count();
        Assert.assertEquals(3, mine);
    }

    @Test
    public void listByIds() {
        Object[] ids = seeded.stream().map(User::getId).toArray();
        List<UserDto> list = userMapper.listByIds(ids, null, null);
        Assert.assertEquals(3, list.size());
    }

    @Test
    public void listBySQL() {
        Map<String, Object> params = mapOf("age", MARK_AGE, "namePattern", classPrefix() + "%");
        List<UserDto> list = userMapper.listBySQL(
                "age=#{age} and name like #{namePattern} order by id asc", params);
        Assert.assertEquals(3, list.size());
    }
}
