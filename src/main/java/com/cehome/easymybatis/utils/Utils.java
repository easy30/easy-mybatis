package com.cehome.easymybatis.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.text.MessageFormat;
import java.util.List;

/**
 * coolma 2019/10/24
 **/
public class Utils {

    public static String format(String message,Object... args){
        return MessageFormatter.format(message,args);
       /* MessageFormat fmt = new MessageFormat(source);
        return fmt.format(args);*/
    }

    public static String toString(Object[] array,String separator,String quote)
    {
        if(array==null || array.length==0) return "";
        if(quote==null) quote="";
        String s="";
        for(int nIndex=0;nIndex<array.length;nIndex++)
        {
            if(s.length()>0) s=s+separator;
            if( array[nIndex]!=null)
                s+=quote+ array[nIndex]+quote;
        }
        return s;
    }
    public static String toString(List array, String separator, String quote)
    {
        if(array==null || array.size()==0) return "";
        if(quote==null) quote="";
        String s="";
        for(int nIndex=0;nIndex<array.size();nIndex++)
        {
            if(s.length()>0) s=s+separator;
            if( array.get(nIndex)!=null)
                s+=quote+ array.get(nIndex)+quote;
        }
        return s;
    }


    public static String regularReplace(String source,String regex,String targetFormat) {
        //#{id}
        RegularReplace  rr=new RegularReplace(source,regex);

        while(rr.find())
        {
            String g=rr.group(1);
            int c=rr.groupCount()+1;
            String[] array=new String[c];
            array[0]=rr.group();
            for(int i=1;i<c;i++) array[i]=rr.group(i);
             MessageFormat messageFormat=new MessageFormat(targetFormat);
            rr.replace(messageFormat.format(array));


        }
       return rr.getResult();

    }
}
