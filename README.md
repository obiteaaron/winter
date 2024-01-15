## 说明
提供一些嵌入式、包含基本功能的中间件，基于Mysql/Redis，避免引入额外中间件带来更大的维护成本，为单体应用和小集群提供基础中间件解决方案。

winter-embed-config-center：嵌入式配置中心，基于Mysql提供秒级配置变更推送能力。

winter-register-center-embed：嵌入式注册中心，提供集群的IP发现，服务注册、发现，基于Mysql/Redis实现。

winter-scheduler-center-embed：嵌入式调度中心，提供集群内的执行单机任务、执行单机常驻任务、执行MapReduce任务的能力，依赖注册中心。

winter-workflow-engine-embed：嵌入式流程引擎，基于SmartEngine扩展，实现基于内存的BPMN流程执行引擎。

winter-strategy-embed：嵌入式策略，实现基本的策略模式，可用于实现系统的扩展和插件功能。

winter-embed-web-manager: 嵌入式web管理工具，可管理配置项、调度任务、线程池、BPMN流程等

### 架构设计图：

![架构设计图](doc/img.png)
