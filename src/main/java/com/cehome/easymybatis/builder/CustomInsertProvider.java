package com.cehome.easymybatis.builder;

import com.cehome.easymybatis.core.ColumnAnnotation;
import com.cehome.easymybatis.core.EntityAnnotation;
import com.cehome.easymybatis.utils.Utils;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Map;

/**
 * coolma 2019/10/28
 **/
public class CustomInsertProvider<E> {

    public String getSql(E e) {

        return  build(e.getClass());

    }

    public String build(Class c) {
        // Class c=  Utils.getGenericInterfaces(UserMapper.class,0,0);
        // String id= namespace+".insert";
        EntityAnnotation entityAnnotation = EntityAnnotation.getInstance(c);
        //StringBuilder sql=new StringBuilder();
        String sql =
                "<script>"
                        + "insert into " + entityAnnotation.getTable()
                        + "<trim prefix=\"(\" suffix=\")\" suffixOverrides=\",\">{0} </trim>"
                        + "<trim prefix=\"values (\" suffix=\")\" suffixOverrides=\",\">{1}</trim>"
                        + "</script>";

        String s1 = "";
        String s2 = "";
        Map<String, ColumnAnnotation> columnMap = entityAnnotation.getPropertyColumnMap();
        ArrayList<Object> arrayList = new ArrayList<Object>();
        for (Map.Entry<String, ColumnAnnotation> e : columnMap.entrySet()) {
            String prop = e.getKey();
            ColumnAnnotation columnAnnotation = e.getValue();
            if (!columnAnnotation.isInsertable()) continue;
            s1 += Utils.format("<if test=\"{0} != null\"> {1},</if>", prop, columnAnnotation.getName());

            s2 += Utils.format("<if test=\"{0} != null\"> #'{'{0}'}',</if>", prop);

            if (columnAnnotation.getColumnInsertDefault() != null) {

                s1 += Utils.format("<if test=\"{0} == null\"> {1},</if>", prop, columnAnnotation.getName());

                s2 += Utils.format("<if test=\"{0} == null\"> {1},</if>", prop, columnAnnotation.getColumnInsertDefault());
            }


        }

        MessageFormat fmt = new MessageFormat(sql);
        Object[] args = {s1, s2};
        sql = fmt.format(args);

        return sql;


    }
}
