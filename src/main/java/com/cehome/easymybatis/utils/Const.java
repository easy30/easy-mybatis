package com.cehome.easymybatis.utils;

/**
 * coolma 2019/10/31
 **/
public interface Const {
    String ENTITY = "e";
    String PARAMS = "params";
    String WHERE = "w";
    String COLUMN = "c";
    String COLUMNS = "cs";
    String PAGE="page";
    String SQL = "s";
    String ID = "id";
    String ORDER="order";
    String DIALECT_MAP="dialectMap";
    String SQL_IF_AND = "<if test='{} != null'> and {}=#{{}} </if>";
    String SQL_IF_AND_DIALECT = "<if test='{} != null and {} != null'> and {} = ${{}}</if>";

    String SQL_AND = " and {}=#{{}} ";
    String SQL_AND_DIALECT =" and {} = {}";
}
