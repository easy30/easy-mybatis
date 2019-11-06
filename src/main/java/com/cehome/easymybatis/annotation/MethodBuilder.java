package com.cehome.easymybatis.annotation;

import com.cehome.easymybatis.builder.AbstractMethodBuilder;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface MethodBuilder {

    Class<? extends AbstractMethodBuilder> value() ;

}
