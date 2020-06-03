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
@Deprecated
public class ByEntityProvider<E> {

    public String getByEntity(@Param(Const.PARAMS) E params){
        return doBuild(params, ProviderSupport.SQL_SELECT,"*",null);

    }
    public String getValueByEntity(String column,@Param(Const.PARAMS) E params){
        Class entityClass=params.getClass();
        EntityAnnotation entityAnnotation = EntityAnnotation.getInstance(entityClass);
        Map<String, ColumnAnnotation> propertyColumnMap=entityAnnotation.getPropertyColumnMap();

        return doBuild(params,ProviderSupport.SQL_SELECT,ProviderSupport.convertColumn(column,propertyColumnMap),null);

    }
    public String listByEntity(@Param(Const.PARAMS) E params, String orderBy){
        return doBuild(params,ProviderSupport.SQL_SELECT,"*",orderBy);

    }

    public String deleteByEntity(@Param(Const.PARAMS) E params){
        return doBuild(params,ProviderSupport.SQL_DELETE,"",null);

    }


    protected String doBuild(E params,String sqlTemplate,String columns,String orderBy){
        //E entity=(E)params.get("e");

        Class entityClass=params.getClass();
        EntityAnnotation entityAnnotation = EntityAnnotation.getInstance(entityClass);



        Map<String, ColumnAnnotation> propertyColumnMap=entityAnnotation.getPropertyColumnMap();

        LineBuilder where = new LineBuilder();
        for (Map.Entry<String, ColumnAnnotation> e :propertyColumnMap.entrySet()) {
            String prop=e.getKey();
            ColumnAnnotation columnAnnotation =e.getValue();
            String fullProp= Const.PARAMS+"."+prop;
            where.append(Utils.format(Const.SQL_IF_AND,
                    fullProp, columnAnnotation.getName(),fullProp));

            if(entityAnnotation.isDialectEntity()){
                String fullDialectMap= Const.PARAMS+"."+Const.PARAM_MAP;
                where.append(Utils.format(Const.SQL_IF_AND_DIALECT,
                        fullDialectMap, fullDialectMap+"."+prop,
                        columnAnnotation.getName(),fullDialectMap+"."+prop));

            }
        }

        String order=ProviderSupport.convertPropsToColumns(orderBy,entityAnnotation);
        if(order.length()>0) where.append( " order by "+order);

        //SQL_SELECT="<script>\r\n select {} from {} <where>{}</where>\r\n</script>";
        return Utils.format(sqlTemplate,columns,entityAnnotation.getTable(),where);

    }
}
