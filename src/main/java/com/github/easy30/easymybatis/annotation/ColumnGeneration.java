package com.github.easy30.easymybatis.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 自定义字段值生成器（不仅仅是主键）
 * column default value( base on custom Generation)
 * first, create  a spring bean implements com.cehome.easymybatis.Generation
 * then add @ColumnGeneration to Entity's field
 *
 */
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ColumnGeneration {
    // com.cehome.easymybatis.Generation bean name for insert
    String insertGeneration()default "";
    // the arg param in Generation.generate(.. ,arg) method
    String insertArg() default "";

    // com.cehome.easymybatis.Generation bean name for update
    String updateGeneration()default "";
    // the arg param in Generation.generate(.. ,arg) method
    String updateArg() default "";

}
