package com.cehome.easymybatis.codegen;

import org.apache.commons.lang3.StringUtils;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.*;
import java.math.BigDecimal;
import java.util.*;

public class RandomObjectUtil {

    public static final String allNumber = "[0-9]{1,}";
    public static void initObject(Object object,String... skipProps) {
        if(object==null) return ;
        Class clazz=object.getClass();
        Field[] fields = clazz.getDeclaredFields();
        //属性字段
        String fileName = "";
        //符合JavaBean规则的属性
        PropertyDescriptor property = null;
        Set<String> skipSet=new HashSet();
        if(skipProps!=null)
        skipSet.addAll(Arrays.asList(skipProps));
        //set方法对象
        Method method = null;
        for (int i = 0; i < fields.length; i++) {
            //属性字段
            fileName = fields[i].getName();
            if( skipSet.contains(fileName)) continue;
            //获取属性上的注解
            FormatAnnotation annotation = fields[i].getAnnotation(FormatAnnotation.class);
            try {
                property = new PropertyDescriptor(fileName, clazz);

            } catch (IntrospectionException e) {
                //没有设置set方法，或者不符合JavaBean规范
                continue;
            }
            //获取set方法对象
            method = property.getWriteMethod();
            //如果是字节类型(包含基本类型和包装类)
            if (fields[i].getType() == byte.class || fields[i].getType() == Byte.class) {
                try {
                    method.invoke(object, RandomValueUtil.getByteValue());
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
            //如果是short类型(包含基本类型和包装类)
            else if (fields[i].getType() == short.class || fields[i].getType() == Short.class) {
                try {
                    method.invoke(object, RandomValueUtil.getShortValue());
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
            //如果是char类型(包含基本类型和包装类)
            else if (fields[i].getType() == char.class || fields[i].getType() == Character.class) {
                try {
                    method.invoke(object, RandomValueUtil.getCharValue());
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
            //如果是整型(包含基本类型和包装类)
            else if (fields[i].getType() == int.class || fields[i].getType() == Integer.class) {
                try {
                    //为属性赋值
                    method.invoke(object, RandomValueUtil.getIntValue());
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
            //如果是float(包含基本类型和包装类)
            else if (fields[i].getType() == float.class || fields[i].getType() == Float.class) {
                try {
                    //为属性赋值
                    method.invoke(object, RandomValueUtil.getFloatValue());
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
            //如果是double(包含基本类型和包装类)
            else if (fields[i].getType() == double.class || fields[i].getType() == Double.class) {
                try {
                    //为属性赋值
                    method.invoke(object, RandomValueUtil.getDoubleValue());
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
            //如果是double(包含基本类型和包装类)
            else if (fields[i].getType() == long.class || fields[i].getType() == Long.class) {
                try {
                    //为属性赋值
                    method.invoke(object, RandomValueUtil.getDoubleValue());
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
            //如果是boolean(包含基本类型和包装类)
            else if (fields[i].getType() == boolean.class || fields[i].getType() == Boolean.class) {
                try {
                    //为属性赋值
                    method.invoke(object, RandomValueUtil.getBooleanValue());
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
            //如果是boolean(包含基本类型和包装类)
            else if (fields[i].getType() == String.class) {
                try {
                    //为属性赋值
                    method.invoke(object, RandomValueUtil.getRamdomString(4));
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
            //如果是日期类型
            else if (fields[i].getType() == Date.class) {
                try {
                    method.invoke(object, RandomValueUtil.getDateValue());
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
            //如果是List类型
            else if (fields[i].getType().isAssignableFrom(List.class)) {
                //获取泛型
                Type type = fields[i].getGenericType();
                //如果不是泛型，不做处理
                if (type == null) {
                    continue;
                }
                if (type instanceof ParameterizedType) {
                    ParameterizedType parameterizedType = (ParameterizedType)type;
                    Class<?> genericClazz = (Class)parameterizedType.getActualTypeArguments()[0];
                    int length = 0;
                    String listLength = "4";
                    if (annotation != null) {
                        listLength = annotation.listLength();
                    }
                    if (StringUtils.isEmpty(listLength) || !listLength.matches( allNumber)) {
                        length = 4;
                    }
                    length = Integer.parseInt(listLength);
                    List<Object> list = new ArrayList<>(length);
                    for (int j = 0; j < length; j++) {
                        list.add(getObject(genericClazz));
                    }
                    try {
                        method.invoke(object, list);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    }
                }
            }
            //如果是Map类型
            else if(fields[i].getType().isAssignableFrom(Map.class)){
                //获取泛型
                Type types = fields[i].getGenericType();
                //如果不是泛型的话则不处理
                if(types == null){
                    continue;
                }
                if(types instanceof ParameterizedType){
                    int length = 4;
                    if(annotation != null){
                        String lengthStr = annotation.mapLength();
                        if(!StringUtils.isEmpty(lengthStr) && lengthStr.matches( allNumber)){
                            length = Integer.parseInt(lengthStr);
                        }
                    }
                    ParameterizedType parameterizedType = (ParameterizedType)types;
                    Map<Object,Object> map = new HashMap();
                    for(int j=0;j<length;j++){
                        map.put(getObject((Class<?>)parameterizedType.getActualTypeArguments()[0]),
                                getObject((Class<?>)parameterizedType.getActualTypeArguments()[1]));
                    }
                    try {
                        method.invoke(object,map);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    }
                }
            }
            //这里默认处理的是自定义对象
            else {
                try {
                    Object obj = getObject(fields[i].getType());
                    method.invoke(object,obj);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        }

    }
    public static <T> T getObject(Class<T> clazz,String... skipProps) {
        T t = null;
        if (clazz == null) {
            return t;
        }
        if((t = (T)getPrimitive(clazz))!= null){
            return t;
        }
        if(clazz.equals( java.math.BigDecimal.class)){
            return (T)new BigDecimal(RandomValueUtil.getIntValue());
        }

        //需要有无参的构造器
        try {
            t = (T)clazz.newInstance();

            initObject(t,skipProps);
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return t;

    }
 
    private static Object getPrimitive(Class<?> clazz) {
        //如果是byte类型(包含基本类型和包装类)
        if (clazz == byte.class || clazz == Byte.class) {
            return RandomValueUtil.getByteValue();
        }
        //如果是short类型(包含基本类型和包装类)
        if (clazz == short.class || clazz == Short.class) {
            return RandomValueUtil.getShortValue();
        }
        //如果是char类型(包含基本类型和包装类)
        if (clazz == char.class || clazz == Character.class) {
            return RandomValueUtil.getCharValue();
        }
        //如果是整型(包含基本类型和包装类)
        if (clazz == int.class || clazz == Integer.class) {
            //为属性赋值
            return RandomValueUtil.getIntValue();
        }
        //如果是float(包含基本类型和包装类)
        if (clazz == float.class || clazz == Float.class) {
            //为属性赋值
            return RandomValueUtil.getFloatValue();
        }
        //如果是double(包含基本类型和包装类)
        if (clazz == double.class || clazz == Double.class) {
            //为属性赋值
            return RandomValueUtil.getDoubleValue();
        }
        //如果是double(包含基本类型和包装类)
        if (clazz == long.class || clazz == Long.class) {
            //为属性赋值
            return RandomValueUtil.getDoubleValue();
        }
        //如果是boolean(包含基本类型和包装类)
        if (clazz == boolean.class || clazz == Boolean.class) {
            return RandomValueUtil.getBooleanValue();
        }
        //如果是boolean(包含基本类型和包装类)
        if (clazz == String.class) {
            //为属性赋值
            return RandomValueUtil.getRamdomString(8);
        }
        //如果是日期类型
        if (clazz == Date.class) {
            return RandomValueUtil.getDateValue();
        }
        return null;
    }
}