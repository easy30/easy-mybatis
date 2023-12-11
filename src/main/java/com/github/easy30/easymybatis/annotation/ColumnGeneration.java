package com.github.easy30.easymybatis.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 自定义字段值生成器（不仅仅是主键）
 * custom column default value from java code ( base on custom Generation)
 * first, create  a spring bean implements com.cehome.easymybatis.Generation
 * then add @ColumnGeneration to Entity's field
 *
 */
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ColumnGeneration {
    //  Generation bean name for insert
    String insertGeneration() default "";
    String insertMethod() default "generate";
    // the arg param in Generation.generate(.. ,arg) method
    String insertArg() default "";

    //  Generation bean name for update
    String updateGeneration() default "";
    String updateMethod() default "generate";
    // the arg param in Generation.generate(.. ,arg) method
    String updateArg() default "";


    //  Generation bean name for insert or update
    String generation() default "";
    String method() default "generate";
    // the arg param in Generation.generate(.. ,arg) method
    String arg() default "";

}
