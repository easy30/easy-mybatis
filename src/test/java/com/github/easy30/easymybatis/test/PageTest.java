package com.github.easy30.easymybatis.test;

import com.github.easy30.easymybatis.Page;
import com.github.easy30.easymybatis.test1.User;
import com.github.easy30.easymybatis.test1.UserDto;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;

/**
 * pageByParams / pageBySQL 测试。
 * <p>本类用独有 age=8888 标记数据，分页只在该范围内统计。</p>
 */
public class PageTest extends TestBase {

    private static final int MARK_AGE = 8888;
    private static final int TOTAL = 5;

    @Before
    public void before() {
        cleanOldData();
        for (int i = 0; i < TOTAL; i++) insertUser("page", i, MARK_AGE);
    }

    @Test
    public void pageByParams() {
        User params = new User();
        params.setAge(MARK_AGE);
        Page<UserDto> page = new Page<>(1, 2);
        userMapper.pageByParams(params, page, "id asc", null, null);

        Assert.assertEquals("第一页 2 条", 2, page.getData().size());
        Assert.assertEquals("总数 5", TOTAL, page.getRecordCount());
    }

    @Test
    public void pageByParamsSecondPage() {
        User params = new User();
        params.setAge(MARK_AGE);
        Page<UserDto> page = new Page<>(3, 2);
        userMapper.pageByParams(params, page, "id asc", null, null);
        // 第 3 页：5 条中第 5 条，剩 1 条
        Assert.assertEquals(1, page.getData().size());
    }

    @Test
    public void pageBySQL() {
        Map<String, Object> params = mapOf("age", MARK_AGE, "namePattern", classPrefix() + "%");
        Page<UserDto> page = new Page<>(1, 3);
        userMapper.pageBySQL("age=#{age} and name like #{namePattern} order by id asc", params, page);
        Assert.assertEquals(3, page.getData().size());
        Assert.assertEquals(TOTAL, page.getRecordCount());
    }
}
