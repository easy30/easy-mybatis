package com.github.easy30.easymybatis;

import com.github.easy30.easymybatis.utils.EntityProxyFactory;

public class MybatisUtils {
    /**
     * create cglib proxy ,for dynamic update entity
     * @param targetObject
     * @param <T>
     * @return
     */
    public static <T>T createProxy(T targetObject) {
        return EntityProxyFactory.createProxy(targetObject);
    }
    /**
     * create cglib proxy ,for dynamic update entity
     * @param targetClass
     * @param <T>
     * @return
     */
    public static <T>T createProxy(Class<T> targetClass) {
        return EntityProxyFactory.createProxy(targetClass);
    }

    public static void main(String[] args) {
        Page page=  createProxy(new Page());
        page.setPageCount(100);
        System.out.println(page.getPageCount() );
        System.out.println(EntityProxyFactory.getChangedProperties(page));
        page=  createProxy(Page.class);
        page.setPageCount(101);
        System.out.println(page.getPageCount() );
        System.out.println(EntityProxyFactory.getChangedProperties(page));

    }
}
