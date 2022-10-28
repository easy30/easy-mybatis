package com.github.easy30.easymybatis.utils;

/**
 * coolma 2019/10/29
 **/
public class LineBuilder {
    private StringBuilder sb;
    private static String LINE="\r\n";
    public LineBuilder(){
        sb=new StringBuilder();
    }
    public LineBuilder(int capacity){
        sb=new StringBuilder(capacity);
    }

    public LineBuilder append(String string){
        sb.append(string).append(LINE);
        return this;
    }
    public LineBuilder append(Object obj){
        sb.append(obj).append(LINE);
        return this;
    }

    public int length(){
        return sb.length();
    }

    @Override
    public String toString(){
        return sb.toString();
    }

}
