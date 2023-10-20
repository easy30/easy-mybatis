package com.github.easy30.easymybatis.annotation;

import com.github.easy30.easymybatis.enums.ColumnOperator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface QueryColumn {

    /**
     * query column/property
     * if not set, use property as default
     * @return
     */
    String column() default "";
    /**
     *  operator such as  > ,= , in ...
     *  DEFAULT will replace with "="(for single propertiy) or "in" (for array property) 
     *  @QueryColumn(operator=ColumnOperator.GT)  is same as @QueryItem("a>${a})
     * If @QueryItem exists, ignore @QueryColumn
     * @return
     */
    ColumnOperator operator() default ColumnOperator.DEFAULT;

    /**
     * for more than one tables;
     * @return
     */
    String table() default "";
}
