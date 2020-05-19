package com.cehome.easymybatis.provider;

import com.cehome.easymybatis.core.ColumnAnnotation;
import com.cehome.easymybatis.utils.Const;
import com.cehome.easymybatis.core.EntityAnnotation;
import com.cehome.easymybatis.core.ProviderSupport;
import com.cehome.easymybatis.utils.Utils;
import org.apache.ibatis.annotations.Param;

import java.util.Map;

/**
 * coolma 2019/10/30
 **/
@Deprecated
public class UpdateByWhereProvider<E> {

    public String build(@Param(Const.ENTITY) E entity, String where, @Param(Const.PARAMS) Object params){

                Class entityClass=entity.getClass();
        EntityAnnotation entityAnnotation = EntityAnnotation.getInstance(entityClass);

        String sql= ProviderSupport.SQL_UPDATE;
        Map<String, ColumnAnnotation> propertyColumnMap=entityAnnotation.getPropertyColumnMap();
        String set = ProviderSupport.sqlSetValues(entityClass,propertyColumnMap, Const.ENTITY);


        if(where==null) where="";
        if(where.trim().startsWith("where")){
            where=where.trim().substring(5);

        }
        if(where.length()>0) {
            where=ProviderSupport.convertSqlColumns(where,entityAnnotation);
            //convert  #{id}==> #{params.id}
            where =ProviderSupport.sqlAddParamPrefix(where, Const.PARAMS);
        }



        return Utils.format(sql,entityAnnotation.getTable(),set,where);


    }
}
