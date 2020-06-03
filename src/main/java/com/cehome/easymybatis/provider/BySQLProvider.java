package com.cehome.easymybatis.provider;

import com.cehome.easymybatis.core.ColumnAnnotation;
import com.cehome.easymybatis.utils.Const;
import com.cehome.easymybatis.core.EntityAnnotation;
import com.cehome.easymybatis.core.ProviderSupport;
import com.cehome.easymybatis.utils.Utils;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.builder.annotation.ProviderContext;

import java.util.Map;

/**
 * coolma 2019/10/30
 **/
@Deprecated
public class BySQLProvider<E> {


    public String deleteByWhere(ProviderContext context,
                                  @Param(Const.WHERE) String where, @Param(Const.PARAMS) Object params){
        EntityAnnotation entityAnnotation = EntityAnnotation.getInstanceByMapper(context.getMapperType());
        //Map<String, ColumnAnnotation> propertyColumnMap=entityAnnotation.getPropertyColumnMap();
        String sql= ProviderSupport.SQL_DELETE;

        if(where!=null &&where.length()>0) {
            where=fixSql(where,entityAnnotation);
        }

        return Utils.format(sql,"",entityAnnotation.getTable(),where);

    }

    public String getValueByWhere(ProviderContext context,
                                  @Param(Const.COLUMN) String column,
                                  @Param(Const.WHERE) String where, @Param(Const.PARAMS) Object params){
        EntityAnnotation entityAnnotation = EntityAnnotation.getInstanceByMapper(context.getMapperType());
        Map<String, ColumnAnnotation> propertyColumnMap=entityAnnotation.getPropertyColumnMap();
        String sql=ProviderSupport.SQL_SELECT;

        column=ProviderSupport.convertColumn(column,propertyColumnMap);
        if(where!=null &&where.length()>0) {
            where=fixSql(where,entityAnnotation);

        }
        //SQL_SELECT="<script>\r\n select {} from {} <where>{}</where>\r\n</script>";
        return Utils.format(sql,column,entityAnnotation.getTable(),where);

    }
    public String listBySQL(ProviderContext context, @Param(Const.SQL) String sql, @Param(Const.PARAMS) Object params){


        EntityAnnotation entityAnnotation = EntityAnnotation.getInstanceByMapper(context.getMapperType());

        //String sql=ProviderSupport.SQL_UPDATE;
        //Map<String, ColumnAnnotation> propertyColumnMap=entityAnnotation.getPropertyColumnMap();
        //String set = ProviderSupport.getSetValues(propertyColumnMap,"e");

        if(sql!=null &&sql.length()>0) {

            sql=fixSql(sql,entityAnnotation);
            sql=ProviderSupport.sqlComplete(sql,entityAnnotation);
        }

        return sql;


    }

    protected String fixSql(String sql,  EntityAnnotation entityAnnotation){
        sql=ProviderSupport.convertSqlPropsToColumns(sql,entityAnnotation);
        //convert  #{id}==> #{params.id}
        sql =ProviderSupport.convertSqlAddParamPrefix(sql, Const.PARAMS);
        return sql;
    }
}
