package com.cehome.easymybatis.provider;

import com.cehome.easymybatis.ColumnAnnotation;
import com.cehome.easymybatis.DialectEntity;
import com.cehome.easymybatis.EntityAnnotation;
import com.cehome.easymybatis.utils.Const;
import com.cehome.easymybatis.utils.LineBuilder;
import com.cehome.easymybatis.utils.RegularReplace;
import com.cehome.easymybatis.utils.Utils;
import org.apache.ibatis.builder.annotation.ProviderContext;

import java.util.List;
import java.util.Map;

/**
 * coolma 2019/10/30
 **/
public class ProviderSupport {
    public static String SQL_UPDATE = "<script>\r\n update {} <set>{}</set> \r\n <where>{}</where> \r\n </script> ";
    public static String SQL_SELECT="<script>\r\n select {} from {} <where>{}</where>\r\n</script>";
    public static String SQL_DELETE="<script>\r\n delete {} from {} <where>{}</where>\r\n</script>";


    public static String sqlSetValues(Object entity,String prefix) {
        LineBuilder s1 = new LineBuilder();
        if (prefix == null) prefix = "";
        if (prefix.length() > 0) prefix += ".";
        Class entityClass=entity.getClass();
        EntityAnnotation entityAnnotation = EntityAnnotation.getInstance(entityClass);

        for (Map.Entry<String, ColumnAnnotation> e : entityAnnotation.getPropertyColumnMap().entrySet()) {

            ColumnAnnotation columnAnnotation = e.getValue();
            if (!columnAnnotation.isUpdatable()) continue;
            String prop =e.getKey();
            //-- entity value
            Object value= entityAnnotation.getProperty(entity,prop);
            if(value!=null){
                s1.append(Utils.format(" {}=#{{}}, ",  columnAnnotation.getName(), prefix + prop));
            }else{
                //-- dialect value
                value=entityAnnotation.getDialectProperty(entity,prop);
                //-- default value
                if(value==null && columnAnnotation.getColumnUpdateDefault()!=null){
                    value=columnAnnotation.getColumnUpdateDefault();
                }
                if(value!=null) {
                    s1.append(Utils.format(" {}={}, ",  columnAnnotation.getName(), value));

                }
            }

        }
        return s1.toString();
    }

    public static String sqlSetValues(Class entityClass, Map<String, ColumnAnnotation> propertyColumnMap, String prefix) {
        LineBuilder s1 = new LineBuilder();
        if (prefix == null) prefix = "";
        if (prefix.length() > 0) prefix += ".";
        boolean isDialect=DialectEntity.class.isAssignableFrom(entityClass);
        LineBuilder dialectSql=new LineBuilder();
        for (Map.Entry<String, ColumnAnnotation> e : propertyColumnMap.entrySet()) {

            ColumnAnnotation columnAnnotation = e.getValue();
            if (!columnAnnotation.isUpdatable()) continue;
            String prop = prefix + e.getKey();
            //-- default value
            if (columnAnnotation.getColumnUpdateDefault() != null) {

                s1.append(Utils.format("<if test='{} == null'> {}={}, </if>", prop, columnAnnotation.getName(), columnAnnotation.getColumnUpdateDefault()));

            }
            //-- entity value
            s1.append(Utils.format("<if test='{} != null'> {}=#{{}}, </if>", prop, columnAnnotation.getName(), prop));

            if(isDialect){
                String mapProp= Const.DIALECT_MAP+"."+prop;
                s1.append(Utils.format("<if test='{} != null and {} != null'> {}=${{}}, </if>", Const.DIALECT_MAP,mapProp, columnAnnotation.getName(), mapProp));

            }

        }
        return s1.toString();
    }

    public static String genWhereById(Object entity,EntityAnnotation entityAnnotation) {

        String where="";
        List<String> props=entityAnnotation.getIdPropertyNames();
        List<String> columns=entityAnnotation.getIdColumnNames();
        for(int i=0;i<props.size();i++){
            if(i>0) where+=",";
            String prop=props.get(i);
            Object value=entityAnnotation.getProperty(entity,prop);
            if(value!=null){
                where+= columns.get(i)+" = #{"+props.get(i)+"}";
            }else{
                value=entityAnnotation.getDialectProperty(entity,prop);
                if(value==null) throw new RuntimeException("property "+prop+" can not be null");
                 where+= columns.get(i)+" = "+value;
            }


        }

        return where;


    }

    public static String sqlPropertiesToColumns(String sql, Map<String, ColumnAnnotation> propertyColumnMap) {
        //  regex= ([^#\$]\{|^\{)(\w+)\} .  "{id}" or  "  {id}" ,but not #{id} ${id}
        RegularReplace rr = new RegularReplace(sql, "([^#\\$]\\{|^\\{)(\\w+)\\}");
        //  [^#\$]\{\w+\}|^\{(\w+)\}

        while (rr.find()) {
            String prop = rr.group(2);
            ColumnAnnotation ca = propertyColumnMap.get(prop);
            String column = null;
            if (ca != null) {
                column = ca.getName();
                if (rr.group().charAt(1) == '{') column = rr.group().charAt(0) + column;
            } else {
                column = prop;
            }
            rr.replace(column);

        }

        return rr.getResult();


    }

    /**
     * convert prop to column name ,such as:  {aF} desc , bF asc, cF  ==>  a_f desc , b_f asc , c_f
     * @param properties
     * @param propertyColumnMap
     * @return
     */
    public static String propertiesToColumns(String properties, Map<String, ColumnAnnotation> propertyColumnMap){
        String order = "";
        if (properties != null && properties.length() > 0) {
            String[] orders = properties.split(",");
            for (String s : orders) {
                s = s.trim();
                if (order.length() > 0) order += " , ";
                int n = indexOfWhitespace(s);
                String prop=n==-1?s:s.substring(0, n);
                prop= trimBrace(prop);
                if(prop.startsWith("{")) prop=prop.substring(1);
                if(prop.endsWith("}")) prop=prop.substring(0,prop.length()-1);
                ColumnAnnotation ca=propertyColumnMap.get(prop);
                String column=ca!=null?ca.getName():prop;
                if (n == -1)
                    order += column + " ";
                else
                    order += column + s.substring(n);

            }

        }
        return order;
    }

    public static String propertyToColumn(String propertyOrColumn, Map<String, ColumnAnnotation> propertyColumnMap){
        propertyOrColumn= trimBrace(propertyOrColumn);
        ColumnAnnotation ca=propertyColumnMap.get(propertyOrColumn);
        return (ca!=null)?ca.getName():propertyOrColumn;
    }


    public static  String sqlAddPrefix(String sql, EntityAnnotation entityAnnotation) {
        if(sql==null) sql="";
        String sql2 =sql.length()==0?"":sql.trim().toLowerCase();
        if (sql2.startsWith("select ")) {
            return sql;
        } else if (sql2.startsWith("from ")) {
            return "select * " + sql;
        } else {
            if (entityAnnotation == null) return sql;

            String table = entityAnnotation.getTable();
            if (table == null) return sql;
            if (sql2.length()==0|| sql2.startsWith("where ") || sql2.startsWith("order ") || sql2.startsWith("group ") || sql2.startsWith("limit ")) {
                return "select * from " + table + " " + sql;
            } else {
                return "select * from " + table + " where " + sql;
            }
        }
    }

    public static String sqlParamPrefix(String sql, String prefix){
        return Utils.regularReplace(sql, "[#\\$]\\{(\\w+)\\}", "#'{'"+ prefix+".{1}'}'");
    }


    private static String trimBrace(String prop){
        if(prop==null) return null;
        if(prop.startsWith("{")) prop=prop.substring(1);
        if(prop.endsWith("}")) prop=prop.substring(0,prop.length()-1);
        return prop;
    }

    private static int indexOfWhitespace(String s) {
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) <= 32) return i;
        }
        return -1;
    }


    public static String sqlByEntity(Object params, String sqlTemplate, String columns, String orderBy, String prefix){

        Class entityClass=params.getClass();
        EntityAnnotation entityAnnotation = EntityAnnotation.getInstance(entityClass);

        Map<String, ColumnAnnotation> propertyColumnMap=entityAnnotation.getPropertyColumnMap();


        LineBuilder where = new LineBuilder();
        for (Map.Entry<String, ColumnAnnotation> e : entityAnnotation.getPropertyColumnMap().entrySet()) {
            String prop=e.getKey();
            ColumnAnnotation columnAnnotation =e.getValue();

            Object value= entityAnnotation.getProperty(params,prop);
            if(value!=null){
                String fullProp= prefix==null||prefix.length()==0?prop:prefix+"."+prop;
                where.append(Utils.format(Const.SQL_AND,  columnAnnotation.getName(),fullProp));
            }else {
                value= entityAnnotation.getDialectProperty(params,prop);
                if(value!=null){
                    where.append(Utils.format(Const.SQL_AND_DIALECT,   columnAnnotation.getName(),value));
                }

            }

        }

        String order= propertiesToColumns(orderBy,propertyColumnMap);
        if(order.length()>0) where.append( " order by "+order);

        //SQL_SELECT="<script>\r\n select {} from {} <where>{}</where>\r\n</script>";
        return Utils.format(sqlTemplate,columns,entityAnnotation.getTable(),where);

    }

    public static String sqlFixColumnsAndParams(String sql, Map<String, ColumnAnnotation> propertyColumnMap){
        sql= sqlPropertiesToColumns(sql,propertyColumnMap);
        //convert  #{id}==> #{params.id}
        sql = sqlParamPrefix(sql, Const.PARAMS);
        return sql;
    }

    public static String sqlById(ProviderContext context, Object id, String sql, String select) {
        EntityAnnotation entityAnnotation = EntityAnnotation.getInstanceByMapper(context.getMapperType());

        List<String> props=entityAnnotation.getIdPropertyNames();
        List<String> columns=entityAnnotation.getIdColumnNames();

        if(props.size()>1) throw new RuntimeException("multi primary keys can not call GetById");

        String where= columns.get(0)+" = #{"+props.get(0)+"}";

        return Utils.format(sql,select,entityAnnotation.getTable(),where);


    }
}
