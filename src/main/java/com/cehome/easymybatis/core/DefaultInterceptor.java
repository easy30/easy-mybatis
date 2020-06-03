package com.cehome.easymybatis.core;

import com.cehome.easymybatis.Page;
import com.cehome.easymybatis.utils.Utils;
import org.apache.ibatis.binding.MapperMethod;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.*;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * coolma 2019/11/1
 **/
@Intercepts({
        @Signature(
                type = Executor.class,
                method = "update",
                args = {MappedStatement.class, Object.class}),
        @Signature(
                type = Executor.class,
                method = "query",
                args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class, CacheKey.class, BoundSql.class}),
        @Signature(
                type = Executor.class,
                method = "query",
                args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}),
})

public class DefaultInterceptor implements Interceptor {

    private Map<String, MappedStatement> countMap = new ConcurrentHashMap();
    private DialectInstance dialectInstance;
    private static ThreadLocal<Boolean> inPage = new ThreadLocal<>();

    public DefaultInterceptor(DialectInstance dialectInstance) {
        this.dialectInstance = dialectInstance;
    }

  /*  public static MappedStatement getCurrentMappedStatement(){
        return mappedStatementHolder.get();
    }*/

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        if (inPage.get() != null) return invocation.proceed();

        final Object[] args = invocation.getArgs();

        MappedStatement statement = (MappedStatement) args[0];


        if (statement.getSqlCommandType() == SqlCommandType.SELECT) {
            Page page = getPage(args[1]);
            if (page != null) {
                try {
                    // -- avoid  recurise invoke :executor.query
                    inPage.set(true);
                    Object parameterObject = args[1];
                    RowBounds rowBounds = (RowBounds) args[2];
                    Executor executor = (Executor) invocation.getTarget();

                    BoundSql boundSql = statement.getBoundSql(parameterObject);
                    String sql = boundSql.getSql();

                    String pageSql = dialectInstance.getInstance().getPageSql(sql);
                    List<ParameterMapping> pms = dialectInstance.getInstance().getPageParameterMapping(statement.getConfiguration(), boundSql.getParameterMappings());

                    BoundSql pageBoundSql = new BoundSql(statement.getConfiguration(), pageSql, pms, parameterObject);
                    CacheKey cacheKey = executor.createCacheKey(statement, parameterObject, rowBounds, pageBoundSql);
                    //Class entityClass= EntityAnnotation.getInstanceByMapper(getMapperClass(statement.getId())).getEntityClass();
                    List list = executor.query(statement, parameterObject, rowBounds, null, cacheKey, pageBoundSql);
                    page.setData(list);


                    if (page.isQueryCount()) {
                        String countSql = dialectInstance.getInstance().getCountSql(sql);
                        BoundSql countBoundSql = new BoundSql(statement.getConfiguration(), countSql, boundSql.getParameterMappings(), parameterObject);
                        cacheKey = executor.createCacheKey(statement, parameterObject, rowBounds, countBoundSql);
                        int total = (Integer) executor.query(createMappedStatement(statement, Integer.class), parameterObject, rowBounds, null, cacheKey, countBoundSql).get(0);
                        page.setRecordCount(total);
                        page.setPageCount((total - 1) / page.getPageSize() + 1);
                    }
                    return list;
                } finally {
                    inPage.remove();
                }
            }


        }


        return invocation.proceed();

    }

    private Page getPage(Object arg) {
        Page page = null;
        if (arg instanceof MapperMethod.ParamMap) {
            MapperMethod.ParamMap parameterObject = (MapperMethod.ParamMap) arg;

            for (Object value : parameterObject.values()) {
                if (value instanceof Page) {
                    page = (Page) value;
                    break;
                }
            }

        }
        return page;
    }

    private Class getMapperClass(String id) {
        int n = id.lastIndexOf('.');
        String className = id.substring(0, n);
        String methodName = id.substring(n + 1);

        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private Method getMethod(String id) {
        int n = id.lastIndexOf('.');
        String className = id.substring(0, n);
        String methodName = id.substring(n + 1);
        Class c = null;
        try {
            c = Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        for (Method m : c.getMethods()) {
            if (m.getName().equals(methodName)) return m;
        }
        return null;
    }

    private MappedStatement createMappedStatement(final MappedStatement statement, final Class resultTypeClass) {
        String id = statement.getId() + "!count";
        MappedStatement result = countMap.get(id);
        if (result != null) return result;
        MappedStatement.Builder statementBuilder = new MappedStatement.Builder(statement.getConfiguration(),

                id, statement.getSqlSource(), statement.getSqlCommandType())
                .resource(statement.getResource())
                .fetchSize(statement.getFetchSize())
                .timeout(statement.getTimeout())
                .statementType(statement.getStatementType())

                .databaseId(statement.getDatabaseId())
                .lang(statement.getLang())
                .resultOrdered(statement.isResultOrdered())
                .resultSets(Utils.toString(statement.getResulSets(), ",", null))
                .resultMaps(new ArrayList() {
                    {
                        add(new ResultMap.Builder(statement.getConfiguration(), statement.getId(), resultTypeClass, new ArrayList()).build());
                    }
                })
                .resultSetType(statement.getResultSetType())
                .flushCacheRequired(statement.isFlushCacheRequired())
                .useCache(statement.isUseCache())
                .cache(statement.getCache());
        result = statementBuilder.build();
        countMap.put(id, result);
        return result;
    }
}
