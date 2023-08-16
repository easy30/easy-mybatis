package com.github.easy30.easymybatis.test1;

import com.github.easy30.easymybatis.dialect.MysqlDialect;
//import net.sf.jsqlparser.JSQLParserException;
import org.junit.Test;

import java.util.Locale;


public class SQLParseTest {
    public static void main(String[] args)  {
        String sql = "WITH RECURSIVE tree AS (\n" +
                "    SELECT iot_equip_instance.*,\n" +
                "           display_name AS full_display_name,path_name AS full_path_name\n" +
                "    FROM iot_equip_instance where parent_id=0 or parent_id is null\n" +
                "\n" +
                "    UNION ALL\n" +
                "    SELECT t.*,\n" +
                "           CONCAT_WS('/', tree.full_display_name, t.display_name),\n" +
                "           CONCAT_WS('/', tree.full_path_name, t.path_name)\n" +
                "    FROM iot_equip_instance t\n" +
                "             JOIN tree ON t.parent_id = tree.id\n" +
                "),\n" +
                "     a as (select  1)\n" +
                " SELECT  *\n" +
                " FROM tree   order by full_display_name ";

       //   sql =  "select * from tb_user where code=' group by ' type=0 order by code";
        System.out.println( sql.matches(".*with\\s+.*as.*"));
        System.out.println(  new MysqlDialect().getCountSql(sql));

       // net.sf.jsqlparser.statement.select.Select select = (net.sf.jsqlparser.statement.select.Select)  CCJSqlParserUtil.parse(sql);


    }
    @Test
    public void withAsTest(){
        String sql = "WITH RECURSIVE tree AS (\n"
            +   "    SELECT iot_equip_instance.*,\n" ;
        System.out.println( sql.toLowerCase().matches("(?s).*with\\s+.*as.*"));
    }

}
