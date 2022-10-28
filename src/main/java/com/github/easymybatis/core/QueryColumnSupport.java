package com.github.easymybatis.core;

import com.github.easymybatis.MapperException;
import com.github.easymybatis.Range;
import com.github.easymybatis.enums.ColumnOperator;

/**
 * QueryColumn parse
 */
public class QueryColumnSupport {
    public static String doQueryColumn(EntityAnnotation entityAnnotation, String column, ColumnOperator operator, String prop, Object value) {
        boolean array = value.getClass().isArray();
        if (operator == null || operator.equals(ColumnOperator.DEFAULT)) {
            operator = array ? ColumnOperator.IN : ColumnOperator.EQ;
        }
        String[] operatorValue = entityAnnotation.getDialect().getColumnOperatorValue(operator);

        //-- do with range
        if(value instanceof Range){
            Range range=(Range)value;
            return doWithRange(  range, column, prop);

        }

        //-- is null  / not is null
        if(ColumnOperator.NULL.equals(operator)){
            return doWithNull( column, value,operatorValue);
        }

        String item = column + " " + operatorValue[0] + " ";
        //-- in,not in
        if (ColumnOperator.IN.equals(operator) || ColumnOperator.NOT_IN.equals(operator)) {
            if (array) {
                item += "<foreach collection=\"" + prop + "\" separator=\",\" item=\"item\" open=\" (\" close=\") \">#{item}</foreach>";

            } else {
                item += " ( ${" + prop + "} ) ";
            }
        }
        //-- between, not between
        else if (ColumnOperator.BETWEEN.equals(operator) || ColumnOperator.NOT_BETWEEN.equals(operator)) {
            if (array) {
                item += String.format(" #{%s[0]} and #{%s[1]} ", prop, prop);
            } else {
                throw new MapperException("array property need for operator " + operatorValue);
            }
        } else {
            item += " #{" + prop + "} ";
        }
        return item;
    }
    private static String doWithRange( Range range,String column,String prop){
        String  item ="";
        if(range.isInRange()) {
            if (range.getMin() != null) {

                item += column + (range.isIncludeMin() ? ">=" : ">") + String.format("#{%s.min}", prop);
            }
            if (range.getMax() != null) {
                if (item.length() > 0) item += " " + Global.OPER_AND + " ";
                item += column + (range.isIncludeMax() ? Global.OPER_LESS_EQUAL : Global.OPER_LESS_THAN) + String.format("#{%s.max}", prop);
            }
        }else{
            if (range.getMin() != null) {

                item += column + (range.isIncludeMin() ? Global.OPER_LESS_THAN : Global.OPER_LESS_EQUAL) + String.format("#{%s.min}", prop);
            }
            if (range.getMax() != null) {
                if (item.length() > 0) item += " " + Global.OPER_AND + " ";
                item += column + (range.isIncludeMax() ? ">" : ">=") + String.format("#{%s.max}", prop);
            }

        }
        return item.length()>0?" ( "+item+" ) ":item;
    }
    private static String doWithNull(String column,Object value,String[] operatorValue){
        if(! (value instanceof Boolean)){
            throw new MapperException(" boolean value need for ColumnOperator.NULL ");
        }
        boolean b=(Boolean) value;
        return  column + " " + (b?operatorValue[0]:operatorValue[1]) + " ";
    }
}
