package com.cehome.easymybatis.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 查询参数对象
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Query {
    // col1,col2,... for select
    String columns() default "";
    // table1 t1,table2 t2 on t1.id=t2.id ...
    String tables() default "";
    // t1.name='test' . Normally is where conditions. You can also add group by /order by/limit ...
    String[] conditions() default "";

    /**
     *  if you define order by here, do not redefine in conditions()
     * @return
     */
    String orderBy() default "";

    /**By default is AND, then the whole sql is: baseConditions() + @QueryCondition props.
     * If you use baseConditions() only, set to NONE
     * @return
     * 缺省整个sql是baseConditions()和@QueryCondition（即属性字段）拼接一起。
     * 如果仅仅只使用baseConditions()，则设置NONE
     */
    OperatorEnum queryPropertyOuterOperator() default OperatorEnum.AND;

    /**
     * default operator between multi props (@QueryCondition)
     * @return
     * 多个@QueryCondition属性条件之间的缺省操作关系
     */
    OperatorEnum queryPropertyInnerOperator() default OperatorEnum.AND;

}
