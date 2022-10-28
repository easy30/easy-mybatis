package com.github.easy30.easymybatis;

/**
 *
 */
public class PageContext {
    protected static final ThreadLocal<Page> LOCAL_PAGE = new ThreadLocal();
    public static <E> Page<E> set(int pageIndex, int pageSize, boolean queryCount) {
        Page<E> page = new Page<>(pageIndex, pageSize);
         page.setQueryCount(queryCount);
        return page;
    }
    protected static void set(Page page) {
        LOCAL_PAGE.set(page);
    }

    public static <T> Page<T> get() {
        return (Page)LOCAL_PAGE.get();
    }

    public static void remove() {
        LOCAL_PAGE.remove();
    }
}
