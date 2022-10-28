package com.github.easy30.easymybatis.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * query sql condition
 */
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface QueryExp {

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
