package com.github.easymybatis.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 字段缺省值（数值或数据库函数等）
 * column default value (base on database value,function)
 * coolma 2019/10/25
 **/
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ColumnDefault {
    /**
     * for insert or update
     * @return
     */
    String value() default "";

    /**
     * for insert only
     * @return
     */
    String insertValue() default "";

    /**
     * for update only
     * @return
     */
    String updateValue() default "";

}
