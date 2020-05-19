package com.cehome.easymybatis.provider;

import com.cehome.easymybatis.core.EntityAnnotation;
import com.cehome.easymybatis.core.ProviderSupport;
import com.cehome.easymybatis.utils.Utils;
import org.apache.ibatis.builder.annotation.ProviderContext;

import java.util.List;

/**
 * coolma 2019/10/30
 **/
@Deprecated
public class ByIdProvider<E> {
    public String getById(ProviderContext context, Object id) {
       return doBuild(context,id, ProviderSupport.SQL_SELECT,"*");
    }
    public String deleteById(ProviderContext context, Object id) {
        return doBuild(context,id,ProviderSupport.SQL_DELETE,"");
    }
    protected String doBuild(ProviderContext context, Object id,String sql,String select) {


        EntityAnnotation entityAnnotation = EntityAnnotation.getInstanceByMapper(context.getMapperType());



        List<String> props=entityAnnotation.getIdPropertyNames();
        List<String> columns=entityAnnotation.getIdColumnNames();

        if(props.size()>1) throw new RuntimeException("multi primary keys can not call GetById");

        String where= columns.get(0)+" = #{"+props.get(0)+"}";
        //todo:dialect id

        return Utils.format(sql,select,entityAnnotation.getTable(),where);


    }
}
