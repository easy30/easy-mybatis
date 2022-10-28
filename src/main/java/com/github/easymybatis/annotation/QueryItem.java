package com.github.easymybatis.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface QueryItem {

    /**
     * mybatis sql such as "name = #{name} "
     * @return
     */
    String[] value() default "";

    /**
     *
     * @return
     */
    boolean ignore() default false;

}
