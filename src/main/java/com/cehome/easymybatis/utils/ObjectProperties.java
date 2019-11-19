package com.cehome.easymybatis.utils;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.HashMap;
import java.util.Map;

/**
 * coolma 2019/11/19
 **/
public class ObjectProperties extends SimpleProperties {

    private Object source;
    private Map<String,PropertyDescriptor> map=new HashMap();
    public ObjectProperties(Object source){

        this.source=source;
        BeanInfo beanInfo = null;
        try
        {
            beanInfo = Introspector.getBeanInfo(source.getClass());
            for(PropertyDescriptor pd:  beanInfo.getPropertyDescriptors()){
                String prop=pd.getName();
                if (prop.equals("class")) continue;
                map.put(prop,pd);
            }

        }
        catch (IntrospectionException e)
        {
            throw new RuntimeException("Bean introspection failed: " + e.getMessage());
        }



    }

    public String[] getProperties(){
        return  map.keySet().toArray(new String[0]);
    }
    public Object getValue(String prop){
        PropertyDescriptor pd=map.get(prop);
        if(pd==null) return null;
        try {
            return pd.getReadMethod().invoke(source);
        } catch ( Exception e) {
            throw new RuntimeException(e);
        }

    }
}
