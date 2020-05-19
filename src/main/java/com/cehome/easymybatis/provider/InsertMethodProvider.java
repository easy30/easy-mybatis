package com.cehome.easymybatis.provider;

import com.cehome.easymybatis.core.ColumnAnnotation;
import com.cehome.easymybatis.core.EntityAnnotation;
import com.cehome.easymybatis.utils.Const;
import com.cehome.easymybatis.utils.LineBuilder;
import com.cehome.easymybatis.utils.Utils;

import java.util.Map;

/**
 * coolma 2019/10/28
 **/
@Deprecated
public class InsertMethodProvider<E> {



    public String build(E entity) {
        Class entityClass=entity.getClass();
        // Class c=  Utils.getGenericInterfaces(UserMapper.class,0,0);
        // String id= namespace+".insert";
        EntityAnnotation entityAnnotation = EntityAnnotation.getInstance(entityClass);
        //StringBuilder sql=new StringBuilder();

        LineBuilder sql =new LineBuilder();
        sql.append(  "<script>")
                .append("insert into {} " )
                .append("<trim prefix=\"(\" suffix=\")\" suffixOverrides=\",\">{} </trim>")
                .append("<trim prefix=\"values (\" suffix=\")\" suffixOverrides=\",\">{}</trim>")
                .append("</script>");

        LineBuilder s1 = new LineBuilder();
        LineBuilder s2 = new LineBuilder();
        Map<String, ColumnAnnotation> columnMap= entityAnnotation.getPropertyColumnMap();
        for (Map.Entry<String, ColumnAnnotation> e : columnMap.entrySet()) {
            String prop=e.getKey();
            ColumnAnnotation columnAnnotation =e.getValue();
            if(!columnAnnotation.isInsertable()) continue;
            s1.append(Utils.format("<if test='{} != null'> {},</if>",prop, columnAnnotation.getName()));

            s2.append(Utils.format("<if test='{} != null'> #{{}},</if>",prop,prop));

            if(columnAnnotation.getColumnInsertDefault()!=null){

                s1.append(Utils.format("<if test='{} == null'> {},</if>",prop, columnAnnotation.getName()));

                s2.append(Utils.format("<if test='{} == null'> {},</if>",prop, columnAnnotation.getColumnInsertDefault()));
            }

            if(entityAnnotation.isDialectEntity()){
                s1.append(Utils.format("<if test='{} != null and {} != null'> {},</if>",
                        Const.VALUE_MAP, Const.VALUE_MAP+"."+prop, columnAnnotation.getName()));
                s2.append(Utils.format("<if test='{} != null and {} != null'> ${{}},</if>",
                        Const.VALUE_MAP, Const.VALUE_MAP+"."+prop, Const.VALUE_MAP+"."+prop));

            }

        }

        return Utils.format(sql.toString(),entityAnnotation.getTable(),s1,s2);


    }


}
