package com.github.easy30.easymybatis.utils;

import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * cglib object proxy ,  used to get the changed properties of an entity.
 *
 * @author coolma
 */
public class EntityProxyFactory {

    public static <T>T createProxy(T targetObject) {
        return  (T)new EntityMethodInterceptor().createProxy(targetObject);
    }

    public static <T>T createProxy(Class<T> targetClass) {
        return  (T)new EntityMethodInterceptor().createProxy(targetClass);
    }

    public static Set<String> getChangedProperties(Object entity) {
        EntityMethodInterceptor interceptor = getInterceptor(entity);
        if (interceptor != null) {
            return interceptor.getProperties();
        }
        return null;
    }

    public static EntityMethodInterceptor getInterceptor(Object entity) {
        if (Enhancer.isEnhanced(entity.getClass())) {
            try {
                return (EntityMethodInterceptor) entity.getClass()
                        .getDeclaredMethod("getCallback", new Class[]{Integer.TYPE})
                        .invoke(entity, new Object[]{new Integer(0)});
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        }
        return null;
    }

    public static class EntityMethodInterceptor<T> implements MethodInterceptor, java.io.Serializable {
        private T targetObject; // 代理的目标对象
        private Set<String> properties = Collections.synchronizedSet( new HashSet<String>());

        private boolean enabled = true;

        private static String fix(String name) {
            return Character.toLowerCase(name.charAt(3)) + name.substring(4);
        }

        public boolean isEnabled() {
            return enabled;
        }

        /**
         * 设置监控有效性
         *
         * @param enabled
         */
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public Set<String> getProperties() {
            return properties;
        }

        public void clearProperties() {
            properties.clear();
        }

        public T createProxy(T targetObject) {
            this.targetObject = targetObject;
            return (T)this.createProxy(targetObject.getClass());
        }

        public <T>T createProxy(Class<T> targetClass) {
            Enhancer enhancer = new Enhancer(); // 该类用于生成代理对象
            enhancer.setSuperclass(targetClass); // 设置目标类为代理对象的父类
            enhancer.setCallback(this); // 设置回调用对象为本身
            return (T)enhancer.create();
        }

        public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
            Object result = targetObject==null?proxy.invokeSuper(obj,args): proxy.invoke(targetObject, args);
            String name = method.getName();

            if (isEnabled() && name.length() > 3 && name.startsWith("set") && args.length == 1 && method.getReturnType() == Void.TYPE) {
                //System.out.println("--------"+this+" "+obj+" "+name);
                String prop = fix(name);
                properties.add(prop);

            }

            return result;
        }

    }


}