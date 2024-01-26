package com.github.easy30.easymybatis.core;

import com.github.easy30.easymybatis.Mapper;
import com.github.easy30.easymybatis.SelectOption;
import com.github.easy30.easymybatis.annotation.ForeignColumn;
import com.github.easy30.easymybatis.utils.ObjectSupport;
import com.github.easy30.easymybatis.utils.Utils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.session.SqlSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.util.CollectionUtils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * coolma 2024/1/25
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

public class ExtendInterceptor implements Interceptor {

    private static Logger logger = LoggerFactory.getLogger(ExtendInterceptor.class);
    private SqlSession sqlSession;
    private Set<Class> ignoreForeignClassSet=Collections.synchronizedSet(new HashSet<>());

    public ExtendInterceptor(SqlSession sqlSession) {
        this.sqlSession = sqlSession;
    }

    public static <T, K> Map<K, T> listToMap(List<T> list, Function<T, K> key) {
        if (list == null) return null;
        //filter过滤null值.  toMap底层用了map.merge(),value=null 会抛异常.
        //如果不正确指定Collectors.toMap方法的第三个参数（key冲突处理函数），那么在key重复的情况下该方法会报出【Duplicate Key】的错误导致Stream流异常终止，使用时要格外注意这一点。
        return list.stream().filter(Objects::nonNull).collect(Collectors.toMap(key, Function.identity(), (oldValue, newValue) -> newValue, LinkedHashMap::new));
    }

    public static Method getFirstMethod(Class clazz, String methodName, Class... classTypes) {
        for (Method m : clazz.getMethods()) {
            if (m.getName().equals(methodName)) {
                for (Class c : classTypes) {
                    if (m.getParameterTypes().length > 0 && c.equals(m.getParameterTypes()[0])) {
                        return m;
                    }
                }
            }
        }
        return null;
    }

    public static void main(String[] args) throws Exception {
        PropertyDescriptor[] propertyDescriptors = BeanUtils.getPropertyDescriptors(HashMap.class);

        Object os = Array.newInstance(Integer.TYPE, 3);
        Array.set(os, 0, 11);
        //Object[] os=(Object[]) o;

        System.out.println(Array.get(os, 0));
       /* //Class clazz=A.class;
        A[] aa=new A[3];
        aa[0]=new A();
        aa[0].setA(2);
        aa[1]=new A();
        aa[2]=new A();
        Method method = ExtendInterceptor.class.getMethod("test", A.class);

        method.invoke(null,aa[0]);
        method = ExtendInterceptor.class.getMethod("test2" ,String[].class );
        System.out.println(aa.getClass().equals(A[].class));
        method.invoke(null,new String[2]);*/
        //ExtendInterceptor.class.getMethod("test", Class[].class).invoke(null,aa);

    }

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        final Object[] args = invocation.getArgs();
        MappedStatement statement = (MappedStatement) args[0];
        // -- do with select
        if (statement.getSqlCommandType() == SqlCommandType.SELECT) {
            List list = (List) invocation.proceed();
            SelectOption selectOption = getOption(args, 1);
            if (selectOption == null || !selectOption.isIgnoreForeignColumn()) {
                setForeignColumns(list);
            }
            return list;
        } else { //update
            return invocation.proceed();
        }


    }

    private SelectOption getOption(Object[] args, int index) {
        if (index >= args.length) return null;
        Object arg = args[index];
        if (arg != null && arg instanceof Map) {
            Map parameterObject = (Map) arg;
            for (Object value : parameterObject.values()) {
                if (value != null) {
                    if (value instanceof SelectOption[]) {
                        return MapperOptionSupport.merge((SelectOption[]) value);
                    } else if (value instanceof SelectOption) {
                        return (SelectOption) value;
                    }

                }
            }

        }
        return null;
    }

    public void setForeignColumns(List<Object> list) throws  Exception {
        if (CollectionUtils.isEmpty(list)) return;
        Class clazz = list.stream().filter(e -> e != null).map(e -> e.getClass()).findAny().orElse(null);
        //must not be null
        if (clazz == null || clazz.isAssignableFrom(Map.class) || clazz.isAssignableFrom(Collection.class)) return;
        if(ignoreForeignClassSet.contains(clazz)) return;
        PropertyDescriptor[] propertyDescriptors = BeanUtils.getPropertyDescriptors(clazz);
        Map<Class, Map> foreignMaps = new HashMap<>();
        boolean find=false;
        for (PropertyDescriptor pd : propertyDescriptors) {
            ForeignColumn foreignColumn = ObjectSupport.getAnnotation(ForeignColumn.class, clazz, pd);
            if (foreignColumn == null || StringUtils.isBlank(foreignColumn.keyProp())
                    || foreignColumn.foreignClass() == null || StringUtils.isBlank(foreignColumn.foreignExp())) continue;
            find=true;
            Class foreignClass = foreignColumn.foreignClass();
            String exp = foreignColumn.foreignExp();
            Map foreignMap = foreignMaps.get(foreignClass);
            if (foreignMap == null) {
                //获取非空的ids
                List<Object> ids = list.stream().map(e -> ObjectSupport.getProperty(e, foreignColumn.keyProp()))
                        .filter(id -> id != null).collect(Collectors.toList());
                if (CollectionUtils.isEmpty(ids)) {
                    foreignMap = new HashMap();
                }
                //存在ids则去查询外键表,并放入Map
                else {
                    if (foreignColumn.foreignExpType().equals(ForeignColumn.ForeignExpType.ENTITY_PROP)) {
                        //查询记录
                        EntityAnnotation foreignAnno = EntityAnnotation.getInstanceOnly(foreignClass);
                        if(foreignAnno==null) throw new RuntimeException(foreignClass+" is not a valid entity class");
                        Object mapperObject = sqlSession.getMapper(foreignAnno.getMapperClass());
                        if(!(mapperObject instanceof Mapper)) throw new RuntimeException(mapperObject.getClass()+" is not a Mapper");
                        Mapper foreignMapper = (Mapper) mapperObject;
                        List foreignList = foreignMapper.listByIds(ids.toArray(new Object[0]), null);
                        String foreignIdName = foreignAnno.getIdPropertyNames().get(0);
                        foreignMap = listToMap(foreignList, e -> ObjectSupport.getProperty(e, foreignIdName));
                    } else if (foreignColumn.foreignExpType().equals(ForeignColumn.ForeignExpType.CLASS_METHOD)) {
                        foreignMap = getFromMethodInvoke( foreignClass, exp, foreignColumn.foreignExpType(),ids);
                    }
                }
                foreignMaps.put(foreignClass, foreignMap);

            }
            if (foreignMap.isEmpty()) continue;

            //设置字段值
            for (Object r : list) {
                pd.getWriteMethod().invoke(r, foreignMap.get(foreignColumn.foreignExp()));

            }

        }
        if(!find){
            ignoreForeignClassSet.add(clazz);
        }

    }

    private Map getFromMethodInvoke( Class foreignClass, String exp, ForeignColumn.ForeignExpType foreignExpType,List<Object> ids) throws Exception {
        Map foreignMap  = new HashMap();
        Class idClass = ids.get(0).getClass();
        Class idArrayClass = Array.newInstance(idClass, 0).getClass();
        Method method = getFirstMethod(foreignClass, exp, idArrayClass, List.class, idClass);
        if (method == null) throw new RuntimeException("can not find method  " + exp + " in class " + foreignClass);
        Class argClass = method.getParameterTypes()[0];
        Object instance=null;
        if(foreignExpType.equals(ForeignColumn.ForeignExpType.INSTANCE_METHOD)) {
            instance=foreignClass.newInstance();
        }
        //A (loop invoke id)
        if (argClass.equals(idClass)) {
            for (Object id : ids) {
                Object value = method.invoke(instance, id);
                foreignMap.put(id, value);
            }
        }
        //A[] or List<A>
        else {
            Object arg = ids;
            //convert ids to  A[]
            if (argClass.equals(idArrayClass)) {
                arg = Array.newInstance(idClass, ids.size());
                for (int i = 0; i < ids.size(); i++) {
                    Array.set(arg, i, ids.get(i));
                }
            }
            Object value = method.invoke(instance, arg);
            //value may be array,list or map
            if (value != null) {
                Class valueClass = value.getClass();
                if (valueClass.isArray()) {
                    int len = Array.getLength(value);
                    if (len < ids.size()) throw new RuntimeException(Utils.format("Method {}#{} return incorrect size: expect {} but {}",
                            foreignClass, exp, ids.size(), len));
                    for (int i = 0; i < ids.size(); i++) {
                        foreignMap.put(ids.get(i), Array.get(value, i));
                    }

                } else if (valueClass.isAssignableFrom(List.class)) {
                    List valueList = (List) value;
                    if (valueList.size() < ids.size()) throw new RuntimeException(Utils.format("Method {}#{} return incorrect size: expect {} but {}",
                            foreignClass, exp, ids.size(), valueList.size()));
                    for (int i = 0; i < ids.size(); i++) {
                        foreignMap.put(ids.get(i), valueList.get(i));
                    }
                } else if (valueClass.isAssignableFrom(Map.class)) {
                    foreignMap = (Map) value;
                } else throw new RuntimeException(Utils.format("Method {}#{} return incorrect type: ", foreignClass, exp, valueClass));
            }

        }
        return foreignMap;
    }



}
