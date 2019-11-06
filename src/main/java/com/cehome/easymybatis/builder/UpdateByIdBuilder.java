package com.cehome.easymybatis.builder;

import com.cehome.easymybatis.EntityAnnotation;
import com.cehome.easymybatis.ColumnAnnotation;
import com.cehome.easymybatis.utils.LineBuilder;
import com.cehome.easymybatis.utils.Utils;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlSource;

import java.util.List;
import java.util.Map;

/**
 * coolma 2019/10/25
 **/
public class UpdateByIdBuilder extends AbstractMethodBuilder {


    public String getMethodName(){
        return "updateById";
    }
    private String build(Class entityClass)   {
        // Class c=  Utils.getGenericInterfaces(UserMapper.class,0,0);
        // String id= namespace+".insert";
        EntityAnnotation entityAnnotation = EntityAnnotation.getInstance(entityClass);
        //StringBuilder sql=new StringBuilder();

        LineBuilder sql =new LineBuilder();
        sql.append(  "<script>")
                .append("update " + entityAnnotation.getTable())
                .append("<set>{}</set>")
                .append(" where ")
                .append("{}")
                .append("</script>");

        LineBuilder s1 = new LineBuilder();

        Map<String, ColumnAnnotation> columnMap= entityAnnotation.getPropertyColumnMap();

        for (Map.Entry<String, ColumnAnnotation> e : columnMap.entrySet()) {
            String prop=e.getKey();
            ColumnAnnotation columnAnnotation =e.getValue();
            if(!columnAnnotation.isUpdatable()) continue;
            s1.append(Utils.format("<if test=\"{} != null\"> {}=#{{}}, </if>",prop, columnAnnotation.getName(),prop));
            if(columnAnnotation.getColumnUpdateDefault()!=null){

                s1.append(Utils.format("<if test=\"{} == null\"> {}={},</if>",prop, columnAnnotation.getName(),columnAnnotation.getColumnUpdateDefault()));

            }

        }

        String s2="";
        List<String> props=entityAnnotation.getIdPropertyNames();
        List<String> columns=entityAnnotation.getIdColumnNames();

        for(int i=0;i<props.size();i++){
            if(i>0) s2+=",";
            s2+= columns.get(i)+" = #{"+props.get(i)+"}";
        }

        return Utils.format(sql.toString(),s1,s2);


    }

    @Override
    public MappedStatement doAdd(Class<?> mapperClass, Class<?> entityClass, EntityAnnotation entityAnnotation) {
        String  sql=build(entityClass);
        SqlSource sqlSource = getLanguageDriver(mapperClass).createSqlSource(configuration, sql, entityClass);
        return this.addUpdateMappedStatement(mapperClass, entityClass, getMethodName(), sqlSource);
    }
}
