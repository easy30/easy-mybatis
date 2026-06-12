package com.github.easy30.easymybatis.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * 统一测试入口：一次性跑完所有用例。
 * <p>各测试类在 @Before 里清理自己前缀(EZTEST_类名_)的旧数据，跑完保留本次数据供查看。</p>
 * <p>数据隔离：每条数据 name 以 EZTEST_类名_方法_序号 命名，断言只认自己的数据，互不影响。</p>
 * <p>DialectUpsertSqlTest 不连库，纯校验 6 个方言的 SQL 生成。</p>
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        DialectUpsertSqlTest.class,// 不连库，最快，先跑
        InsertTest.class,
        UpdateTest.class,
        DeleteTest.class,
        SelectTest.class,
        PageTest.class,
        UpsertTest.class
})
public class AllTestsSuite {
}
