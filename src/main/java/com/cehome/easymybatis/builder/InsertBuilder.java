package com.cehome.easymybatis.builder;

import com.cehome.easymybatis.core.EntityAnnotation;
import com.cehome.easymybatis.core.ColumnAnnotation;
import com.cehome.easymybatis.utils.LineBuilder;
import com.cehome.easymybatis.utils.Utils;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlSource;

import java.util.ArrayList;
import java.util.Map;

/**
 * coolma 2019/10/25
 **/
public class InsertBuilder extends AbstractMethodBuilder {



    public String getMethodName(){
        return "insert";
    }
    private String build(Class entityClass)   {
        // Class c=  Utils.getGenericInterfaces(UserMapper.class,0,0);
       // String id= namespace+".insert";
        EntityAnnotation entityAnnotation = EntityAnnotation.getInstance(entityClass);
        //StringBuilder sql=new StringBuilder();

        LineBuilder sql =new LineBuilder();
        sql.append(  "<script>")
                .append("insert into " + entityAnnotation.getTable())
                        .append("<trim prefix=\"(\" suffix=\")\" suffixOverrides=\",\">{} </trim>")
                                .append("<trim prefix=\"values (\" suffix=\")\" suffixOverrides=\",\">{}</trim>")
                                        .append("</script>");

        LineBuilder s1 = new LineBuilder();
        LineBuilder s2 = new LineBuilder();
        Map<String, ColumnAnnotation> columnMap= entityAnnotation.getPropertyColumnMap();
        ArrayList<Object> arrayList = new ArrayList<Object>();
        for (Map.Entry<String, ColumnAnnotation> e : columnMap.entrySet()) {
            String prop=e.getKey();
            ColumnAnnotation columnAnnotation =e.getValue();
            if(!columnAnnotation.isInsertable()) continue;
            s1.append(Utils.format("<if test=\"{} != null\"> {},</if>",prop, columnAnnotation.getName()));

            s2.append(Utils.format("<if test=\"{} != null\"> #{{}},</if>",prop,prop));

            if(columnAnnotation.getColumnInsertDefault()!=null){

                s1.append(Utils.format("<if test=\"{} == null\"> {},</if>",prop, columnAnnotation.getName()));

                s2.append(Utils.format("<if test=\"{} == null\"> {},</if>",prop, columnAnnotation.getColumnInsertDefault()));
            }

        }

        return Utils.format(sql.toString(),s1,s2);


    }



    @Override
    public MappedStatement doAdd(Class<?> mapperClass, Class<?> entityClass, EntityAnnotation entityAnnotation) {


        String  sql=build(entityClass);

        String keyColumn=Utils.toString(entityAnnotation.getIdColumnNames(),",",null);
        String keyProperty=Utils.toString(entityAnnotation.getIdPropertyNames(),",",null);
        SqlSource sqlSource = getLanguageDriver(mapperClass).createSqlSource(configuration, sql, entityClass);
        return this.addInsertMappedStatement(mapperClass, entityClass, getMethodName(), sqlSource, getKeyGenerator(null), keyProperty, keyColumn);
    }
}
