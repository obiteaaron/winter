package tech.obiteaaron.winter.embed.rpc.spring;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeanMetadataElement;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.type.classreading.MethodMetadataReadingVisitor;
import org.springframework.util.ReflectionUtils;
import tech.obiteaaron.winter.embed.rpc.WinterConsumer;
import tech.obiteaaron.winter.embed.rpc.WinterProvider;
import tech.obiteaaron.winter.embed.rpc.WinterRpcBootstrap;
import tech.obiteaaron.winter.embed.rpc.executing.ConsumerInvocationHandler;
import tech.obiteaaron.winter.embed.rpc.regesiter.ConsumerConfig;
import tech.obiteaaron.winter.embed.rpc.regesiter.ProviderConfig;

import javax.annotation.Resource;
import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
public class WinterRpcSpringBeanFactoryPostProcessor implements BeanFactoryPostProcessor, BeanPostProcessor {

    private static final Map<Class<?>, Object> consumerProxyBeanMap = new HashMap<>();

    private static final Map<String, Pair<String, Map<String, Object>>> beanAnnotaionMap = new HashMap<>();

    @Setter
    private WinterRpcBootstrap winterRpcBootstrap;

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        String[] beanDefinitionNames = beanFactory.getBeanDefinitionNames();
        for (String beanDefinitionName : beanDefinitionNames) {
            // 不支持工厂bean
            Class<?> beanType = beanFactory.isFactoryBean(beanDefinitionName) ? null : beanFactory.getType(beanDefinitionName);
            if (beanType == null) {
                continue;
            }

            // 注册消费者代理Bean
            ReflectionUtils.doWithFields(beanType, field -> registerConsumerProxy(beanFactory, field),
                    field -> field.getAnnotation(Autowired.class) != null || field.getAnnotation(Resource.class) != null);

            // 提前识别@Bean注解，后面识别不到
            BeanDefinition beanDefinition = beanFactory.getBeanDefinition(beanDefinitionName);
            MethodMetadataReadingVisitor methodMetadataReadingVisitor = (MethodMetadataReadingVisitor) Optional.of(beanDefinition).map(BeanMetadataElement::getSource)
                    // MethodMetadataReadingVisitor 从Spring5.2开始被标记为废弃，6.x删除了，未来的版本需要升级兼容一下
                    .filter(item -> item instanceof MethodMetadataReadingVisitor)
                    .orElse(null);
            if (methodMetadataReadingVisitor != null) {
                String returnTypeName = methodMetadataReadingVisitor.getReturnTypeName();
                Map<String, Object> annotationAttributes = methodMetadataReadingVisitor.getAnnotationAttributes(WinterProvider.class.getName());
                if (returnTypeName != null && annotationAttributes != null) {
                    beanAnnotaionMap.put(beanDefinitionName, Pair.of(returnTypeName, annotationAttributes));
                }
            }
        }
    }

    private void registerConsumerProxy(ConfigurableListableBeanFactory beanFactory, Field field) {
        Class<?> aClass = field.getType();
        if (!aClass.isInterface()) {
            return;
        }
        if (consumerProxyBeanMap.containsKey(aClass)) {
            return;
        }
        WinterConsumer annotation = field.getAnnotation(WinterConsumer.class);
        if (annotation == null) {
            return;
        }
        String beanName = generateConsumerBeanName(aClass);
        ConsumerInvocationHandler consumerInvocationHandler = new ConsumerInvocationHandler(annotation, winterRpcBootstrap);
        Object proxyBean = Proxy.newProxyInstance(aClass.getClassLoader(), new Class[]{aClass}, consumerInvocationHandler);
        beanFactory.registerSingleton(beanName, proxyBean);
        consumerProxyBeanMap.put(aClass, proxyBean);
        log.info("registerConsumerBean success className = {}", beanName);
        // 订阅到注册中心
        ConsumerConfig consumerConfig = ConsumerConfig.builder()
                .interfaceClass(aClass)
                .interfaceName(aClass.getName())
                .version(annotation.version())
                .group(annotation.group())
                .build();
        winterRpcBootstrap.consumerConfig(consumerConfig);
    }

    private String generateConsumerBeanName(Class<?> aClass) {
        return aClass.getName() + ":Consumer";
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class<?> targetClass = AopUtils.getTargetClass(bean);
        WinterProvider annotation = targetClass.getAnnotation(WinterProvider.class);
        String[] providerInterfaces = null;
        String version = null, group = null;
        if (annotation != null) {
            providerInterfaces = annotation.providerInterfaces();
            version = annotation.version();
            group = annotation.group();
        } else {
            Pair<String, Map<String, Object>> annotationAttributes = beanAnnotaionMap.get(beanName);
            if (annotationAttributes == null) {
                return bean;
            }
            Map<String, Object> value = annotationAttributes.getValue();
            version = (String) value.get("version");
            group = (String) value.get("group");
            providerInterfaces = (String[]) value.get("providerInterfaces");
            if (providerInterfaces == null || providerInterfaces.length == 0) {
                providerInterfaces = new String[]{annotationAttributes.getKey()};
            }
        }
        Class<?>[] interfaces = targetClass.getInterfaces();
        if (interfaces.length == 0) {
            log.warn("WinterProvider on class need interface implement, ignored. beanName={}, className={}", beanName, targetClass.getName());
            return bean;
        }

        for (Class<?> anInterface : interfaces) {
            if (providerInterfaces != null && providerInterfaces.length > 0 && !Arrays.asList(providerInterfaces).contains(anInterface.getName())) {
                // 忽略
                continue;
            }
            // register到注册中心
            ProviderConfig providerConfig = ProviderConfig.builder()
                    .interfaceClass(anInterface)
                    .interfaceName(anInterface.getName())
                    .interfaceImpl(bean)
                    .version(version)
                    .group(group)
                    .build();
            // 延迟到 ContextRefreshedEvent 事件才注册，确保 bean 都正确初始化成功了，以确保能对外提供服务
            winterRpcBootstrap.providerConfig(providerConfig);
        }

        return bean;
    }
}
