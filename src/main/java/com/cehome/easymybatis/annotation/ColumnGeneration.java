package com.cehome.easymybatis.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ColumnGeneration {
    String insertGeneration()default "";
    String insertArg() default "";

    String updateGeneration()default "";
    String updateArg() default "";

}
