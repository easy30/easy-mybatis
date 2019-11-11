package com.cehome.easymybatis.annotation;

import org.apache.ibatis.mapping.StatementType;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface EntitySelectKey {
  String[] statement();

  String keyProperty() default "";

  String keyColumn() default "";

  boolean before() default true;

  Class<?> resultType();

  StatementType statementType() default StatementType.PREPARED;
}
