package com.github.easy30.easymybatis;

import org.mybatis.spring.SqlSessionFactoryBean;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.ContextRefreshedEvent;

/**
 * 使用EasySqlSessionFactoryBean 则 不再需要定义MapperFactory; 否则需要定义MapperFactory
 */
public class EasySqlSessionFactoryBean extends SqlSessionFactoryBean implements BeanPostProcessor {
    MapperFactory mapperFactory;
    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        mapperFactory = new MapperFactory();
        mapperFactory.setSqlSessionFactory(this.getObject());
        mapperFactory.afterPropertiesSet();
    }


    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        return mapperFactory.postProcessAfterInitialization(bean,beanName);
    }
}
