### v1.1.0

发布日期：2024年01月26日  
发布内容：  

- 提供服务注册能力，依赖winter-embed-config-center
- 提供服务远程调用能力，基于HTTP(S)服务，采用VertX
- 提供基于注解的服务提供者`@WinterProvider`和服务消费者`@WinterConsumer`的定义方式，依赖Spring
- 提供多实例支持，多实例之间相互不影响
- 提供自定义组件能力，大部分组件可以自定义替换
- 支持链式调用扩展，方便增加日志、Trace、监控、限流等
- 提供自定义路由策略，可新增多个自定义路由
- 支持Hessian、JSON序列化，默认Hessian，推荐Hessian
- 默认可跟随SpringBoot自定启动，支持 SpringBoot 1.x/2.x/3.x 自动启动
