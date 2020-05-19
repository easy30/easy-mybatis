package com.cehome.easymybatis.annotation;

import org.apache.ibatis.mapping.StatementType;

import java.lang.annotation.*;

/**
 * 实体类的主键定义
 * for Entity key define
 * then sampe as mybatis SelectKey in xml:
 * <selectKey resultType="java.lang.Integer" keyProperty="id" order="BEFORE" >//AFTER
 *       SELECT LAST_INSERT_ID()
 *     </selectKey>
 */
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
