package com.ctrip.framework.apollo.spring.annotation;

import com.ctrip.framework.apollo.build.ApolloInjector;
import com.ctrip.framework.apollo.spring.property.*;
import com.ctrip.framework.apollo.spring.util.SpringInjector;
import com.ctrip.framework.apollo.util.ConfigUtil;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.annotation.Bean;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Set;

/**
 * Spring value processor of field or method which has @Value and xml config placeholders.
 *
 * Spring Value 处理器，处理：
 *
 * 1. 带有 `@Value` 注解的 Field 和 Method
 * 2. XML 配置的 Bean 的 PlaceHolder 们
 *
 * 每个 Field、Method、XML PlaceHolder 被处理成一个 SpringValue 对象，添加到 SpringValueRegistry 中。
 *
 * 目的还是，为了 PlaceHolder 的自动更新机制。
 *
 * @author github.com/zhegexiaohuozi  seimimaster@gmail.com
 * @since 2017/12/20.
 */
public class SpringValueProcessor extends ApolloProcessor implements BeanFactoryPostProcessor {

    private static final Logger logger = LoggerFactory.getLogger(SpringValueProcessor.class);

    /**
     * SpringValueDefinition 集合
     *
     * KEY：beanName
     * VALUE：SpringValueDefinition 集合
     */
    private static Multimap<String, SpringValueDefinition> beanName2SpringValueDefinitions = LinkedListMultimap.create();

    private final ConfigUtil configUtil;
    private final PlaceholderHelper placeholderHelper;
    /**
     * SpringValueRegistry 对象
     */
    private final SpringValueRegistry springValueRegistry;

    public SpringValueProcessor() {
        configUtil = ApolloInjector.getInstance(ConfigUtil.class);
        placeholderHelper = SpringInjector.getInstance(PlaceholderHelper.class);
        springValueRegistry = SpringInjector.getInstance(SpringValueRegistry.class);
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        // 是否开启自动更新机制
        if (configUtil.isAutoUpdateInjectedSpringPropertiesEnabled()) {
            beanName2SpringValueDefinitions = SpringValueDefinitionProcessor.getBeanName2SpringValueDefinitions();
        }
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        // 是否开启自动更新机制
        if (configUtil.isAutoUpdateInjectedSpringPropertiesEnabled()) {
            // 处理 Field 和 Method
            super.postProcessBeforeInitialization(bean, beanName);
            // 处理 XML 配置的 Bean 的 PlaceHolder 们
            processBeanPropertyValues(bean, beanName);
        }
        return bean;
    }

    @Override
    protected void processField(Object bean, String beanName, Field field) {
        // register @Value on field
        Value value = field.getAnnotation(Value.class);
        if (value == null) {
            return;
        }
        // 提取 `keys` 属性们。
        Set<String> keys = placeholderHelper.extractPlaceholderKeys(value.value());
        if (keys.isEmpty()) {
            return;
        }
        // 循环 `keys` ，创建对应的 SpringValue 对象，并添加到 `springValueRegistry` 中。
        for (String key : keys) {
            SpringValue springValue = new SpringValue(key, value.value(), bean, beanName, field, false);
            springValueRegistry.register(key, springValue);
            logger.debug("Monitoring {}", springValue);
        }
    }

    @Override
    protected void processMethod(Object bean, String beanName, Method method) {
        // register @Value on method
        Value value = method.getAnnotation(Value.class);
        if (value == null) {
            return;
        }
        // 忽略 @Bean 注解的方法
        // skip Configuration bean methods
        if (method.getAnnotation(Bean.class) != null) {
            return;
        }
        // 忽略非 setting 方法
        if (method.getParameterTypes().length != 1) {
            logger.error("Ignore @Value setter {}.{}, expecting 1 parameter, actual {} parameters", bean.getClass().getName(), method.getName(), method.getParameterTypes().length);
            return;
        }
        // 提取 `keys` 属性们。
        Set<String> keys = placeholderHelper.extractPlaceholderKeys(value.value());
        if (keys.isEmpty()) {
            return;
        }
        // 循环 `keys` ，创建对应的 SpringValue 对象，并添加到 `springValueRegistry` 中。
        for (String key : keys) {
            SpringValue springValue = new SpringValue(key, value.value(), bean, beanName, method, false);
            springValueRegistry.register(key, springValue);
            logger.info("Monitoring {}", springValue);
        }
    }

    private void processBeanPropertyValues(Object bean, String beanName) {
        // 获得 SpringValueDefinition 数组
        Collection<SpringValueDefinition> propertySpringValues = beanName2SpringValueDefinitions.get(beanName);
        if (propertySpringValues == null || propertySpringValues.isEmpty()) {
            return;
        }
        // 循环 SpringValueDefinition 数组，创建对应的 SpringValue 对象，并添加到 `springValueRegistry` 中。
        for (SpringValueDefinition definition : propertySpringValues) {
            try {
                PropertyDescriptor pd = BeanUtils.getPropertyDescriptor(bean.getClass(), definition.getPropertyName());
                Method method = pd.getWriteMethod();
                if (method == null) {
                    continue;
                }
                SpringValue springValue = new SpringValue(definition.getKey(), definition.getPlaceholder(), bean, beanName, method, false);
                springValueRegistry.register(definition.getKey(), springValue);
                logger.debug("Monitoring {}", springValue);
            } catch (Throwable ex) {
                logger.error("Failed to enable auto update feature for {}.{}", bean.getClass(), definition.getPropertyName());
            }
        }

        // clear
        // 移除 Bean 对应的 SpringValueDefinition 数组
        beanName2SpringValueDefinitions.removeAll(beanName);
    }

}
