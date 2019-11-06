package com.cehome.easymybatis.builder;

import com.cehome.easymybatis.ColumnAnnotation;
import com.cehome.easymybatis.EntityAnnotation;
import com.cehome.easymybatis.utils.LineBuilder;
import com.cehome.easymybatis.utils.Utils;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlSource;

import java.util.Map;

/**
 * coolma 2019/10/25
 **/
public class UpdateByEntityBuilder extends AbstractMethodBuilder {


    public String getMethodName(){
        return "updateByEntity";
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
                .append(" <where>{}</where> ")
                .append("</script>");

        LineBuilder s1 = new LineBuilder();


        Map<String, ColumnAnnotation> columnMap= entityAnnotation.getPropertyColumnMap();

        for (Map.Entry<String, ColumnAnnotation> e : columnMap.entrySet()) {
            String prop=e.getKey();
            ColumnAnnotation columnAnnotation =e.getValue();
            if(!columnAnnotation.isUpdatable()) continue;
            s1.append(Utils.format("<if test=\"e.{} != null\"> {}=#{e.{}}, </if>",prop, columnAnnotation.getName(),prop));
            if(columnAnnotation.getColumnUpdateDefault()!=null){

                s1.append(Utils.format("<if test=\"e.{} == null\"> {}={}, </if>",prop, columnAnnotation.getName(),columnAnnotation.getColumnUpdateDefault()));

            }

        }

        LineBuilder s2 = new LineBuilder();
        for (Map.Entry<String, ColumnAnnotation> e : columnMap.entrySet()) {
            String prop=e.getKey();
            ColumnAnnotation columnAnnotation =e.getValue();

            s2.append(Utils.format("<if test=\"w.{} != null\"> and {}=#{w.{}}  </if>",prop, columnAnnotation.getName(),prop));


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
