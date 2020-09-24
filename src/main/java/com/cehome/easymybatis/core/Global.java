package com.cehome.easymybatis.core;

public interface Global {
    String SQL_AND = " and {}=#{{}} ";
    String SQL_AND_DIALECT =" and {} = {}";
    String SQL_UPDATE = "<script>\r\n update {} <set>{}</set> \r\n {} \r\n </script> ";
    String  SQL_SELECT = "<script>\r\n select {} from {} {}\r\n</script>";
    String SQL_DELETE = "<script>\r\n delete {} from {} {}\r\n</script>";
    int SQL_TYPE_INSERT = 1;
    int SQL_TYPE_UPDATE = 2;
    int SQL_TYPE_DELETE = 3;
    int SQL_TYPE_SELECT = 4;

    //String SQL_IF_AND = "<if test='{} != null'> and {}=#{{}} </if>";
    //String SQL_IF_AND_DIALECT = "<if test='{} != null and {} != null'> and {} = ${{}}</if>";
    String OPER_AND="AND";
    String OPER_LESS_THAN="&lt;";
    String OPER_LESS_EQUAL="&lt;=";
    String OPER_NOT_EQ="&lt;>";

}
