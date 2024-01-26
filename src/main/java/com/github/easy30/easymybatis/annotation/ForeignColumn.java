package com.github.easy30.easymybatis.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * after query result, set foreign column value
 *
 * example:
 * if keyProp is 100
 * (entityProp type) foreignClass=A , foreignExp=name =>  A(where id=100) -> A.getName()
 * (classMethod type)  foreignClass=B , foreignExp=getName =>  B.getName(100)
 * (instanceMethod type)   foreignClass=C , foreignExp=getName =>  new C().getName(100)
 * @return
 */
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ForeignColumn {

    /**
     * prop of current entity which is a foreign key
     * @return
     */
    String keyProp() ;

    /**
     * foreign entity class or  Util class.
     * @return
     */
    Class foreignClass() ;

    ForeignExpType foreignExpType() default  ForeignExpType.ENTITY_PROP;

    /**
     * entityClass: property of foreign entity
     *
     * @return
     */
    String foreignExp();//Meta.F.get()


    enum ForeignExpType {
        ENTITY_PROP,CLASS_METHOD,INSTANCE_METHOD
    }

}
