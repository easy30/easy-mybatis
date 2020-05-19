package com.cehome.easymybatis.annotation;

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
    String value() default "";
    String insertValue() default "";
    String updateValue() default "";

}
