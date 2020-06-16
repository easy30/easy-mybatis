package com.cehome.easymybatis.annotation;

import com.cehome.easymybatis.enums.ColumnOperator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface QueryColumn {

    /**
     * query column/property
     * @return
     */
    String column() default "";
    /**
     *   > ,= , in ...
     *  @QueryColumn(operator=ColumnOperator.GT)  is same as @QueryItem("a>${a})
     * If @QueryItem exists, ignore @QueryColumn
     * @return
     */
    ColumnOperator operator() default ColumnOperator.EQ;
}
