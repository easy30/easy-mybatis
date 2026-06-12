package com.github.easy30.easymybatis.test;

import com.github.easy30.easymybatis.test1.User;
import com.github.easy30.easymybatis.test1.UserMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.junit.runner.RunWith;

import java.util.HashMap;
import java.util.Map;

/**
 * 测试基类：统一数据前缀、跑前清理、实体工厂。
 * <p>数据隔离策略：每条数据 name 以 EZTEST_类名_ 开头，断言只认自己的数据，跑完保留供查看。</p>
 * <p>清理策略：各测试类 @BeforeClass 调 cleanOldData() 删自己前缀的旧数据，单独跑也干净。</p>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:applicationContext.xml")
public abstract class TestBase {

    protected static final Logger logger = LoggerFactory.getLogger(TestBase.class);

    /** 全局统一前缀，便于一眼识别框架测试数据 */
    public static final String PREFIX = "EZTEST_";

    @Autowired
    protected UserMapper userMapper;

    /** 本测试类的数据前缀，如 EZTEST_InsertTest_ */
    protected String classPrefix() {
        return PREFIX + getClass().getSimpleName() + "_";
    }

    /** 某方法+序号的唯一 name，如 EZTEST_InsertTest_insertOne_0 */
    protected String name(String method, int seq) {
        return classPrefix() + method + "_" + seq;
    }

    /** 构造一个待插入用户（不设 id，走自增） */
    protected User newUser(String method, int seq, int age) {
        User u = new User();
        u.setName(name(method, seq));
        u.setRealName(classPrefix() + "real_" + method + "_" + seq);
        u.setAge(age);
        return u;
    }

    /** 插入一个用户并返回（含回填的 id） */
    protected User insertUser(String method, int seq, int age) {
        User u = newUser(method, seq, age);
        userMapper.insert(u);
        return u;
    }

    protected Map<String, Object> mapOf(Object... kv) {
        Map<String, Object> m = new HashMap<>();
        for (int i = 0; i < kv.length; i += 2) m.put((String) kv[i], kv[i + 1]);
        return m;
    }

    /**
     * 删除本测试类前缀的旧数据。子类在 @BeforeClass 里手动 new 一个实例调用，
     * 或用静态方法 cleanByPrefix(mapper, prefix)。这里提供实例方法供 @Before 使用亦可。
     */
    protected int cleanOldData() {
        return cleanByPrefix(userMapper, classPrefix());
    }

    /** 按 name 前缀删除：delete from user where name like 'prefix%' */
    public static int cleanByPrefix(UserMapper mapper, String prefix) {
        Map<String, Object> params = new HashMap<>();
        params.put("namePattern", prefix + "%");
        int n = mapper.deleteByCondition("name like #{namePattern}", params);
        logger.info("cleanByPrefix [{}] deleted {} rows", prefix, n);
        return n;
    }
}
