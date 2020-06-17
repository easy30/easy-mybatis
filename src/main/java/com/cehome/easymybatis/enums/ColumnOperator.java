package com.cehome.easymybatis.enums;

public enum ColumnOperator{
        /* not include special operator such as REGEXP  <=>
           DEFAULT will replace with "=" or "in" (array property)
        * */
        DEFAULT(""),
        EQ("="),NOT_EQ("<>"), GT(">"),GE(">="),LT("<"),LE("<="),
        BETWEEN("BETWEEN"),NOT_BETWEEN("NOT BETWEEN"),
        IN("IN"),NOT_IN("NOT IN"),LIKE("LIKE"),
        IS_NULL("IS NULL"),IS_NOT_NULL("IS NOT NULL");

        private String value;
         ColumnOperator(String value){
             this.value=value;

        }

    public String getValue() {
        return value;
    }


}