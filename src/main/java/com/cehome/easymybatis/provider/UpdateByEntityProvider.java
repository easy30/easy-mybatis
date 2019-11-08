package com.cehome.easymybatis.provider;

import com.cehome.easymybatis.core.ColumnAnnotation;
import com.cehome.easymybatis.utils.Const;
import com.cehome.easymybatis.core.EntityAnnotation;
import com.cehome.easymybatis.utils.LineBuilder;
import com.cehome.easymybatis.core.ProviderSupport;
import com.cehome.easymybatis.utils.Utils;
import org.apache.ibatis.annotations.Param;

import java.util.Map;

/**
 * coolma 2019/10/28
 **/
public class UpdateByEntityProvider<E> {

    public String build(@Param(Const.ENTITY) E entity, @Param(Const.PARAMS) E params){
        //E entity=(E)params.get("e");

        Class entityClass=entity.getClass();
        EntityAnnotation entityAnnotation = EntityAnnotation.getInstance(entityClass);

        String sql= ProviderSupport.SQL_UPDATE;

        String set = ProviderSupport.sqlSetValues(entityClass,entityAnnotation.getPropertyColumnMap(), Const.ENTITY);


        LineBuilder where = new LineBuilder();
        for (Map.Entry<String, ColumnAnnotation> e : entityAnnotation.getPropertyColumnMap().entrySet()) {
            String prop=e.getKey();
            ColumnAnnotation columnAnnotation =e.getValue();
            String fullProp= Const.PARAMS+"."+prop;
            where.append(Utils.format(Const.SQL_IF_AND,
                    fullProp, columnAnnotation.getName(),fullProp));

            if(entityAnnotation.isDialectEntity()){
                where.append(Utils.format(Const.SQL_IF_AND_DIALECT,
                        Const.PARAM_MAP, Const.PARAM_MAP+"."+prop,
                        columnAnnotation.getName(),Const.PARAM_MAP+"."+prop));

            }


        }

        return Utils.format(sql,entityAnnotation.getTable(),set,where);


    }
}
