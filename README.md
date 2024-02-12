## 说明

提供一些嵌入式、包含基本功能的中间件，基于Mysql/Redis，避免引入额外中间件带来更大的维护成本，为单体应用和小集群提供基础中间件解决方案。

【Release】winter-embed-common-tools：提供一些高阶的常用工具，主要还是项目内使用。

【Release】winter-embed-config-center：嵌入式配置中心，基于Mysql提供秒级配置变更推送能力。

【Alpha】winter-register-center-embed：嵌入式注册中心，提供集群的IP发现，服务注册、发现，基于config-center实现。

【Release】winter-embed-rpc：嵌入式RPC框架，提供基本的服务注册、服务远程调用功能，基于HTTP(S)提供远程调用，大部分组件可以自由替换和扩展。

【Alpha】winter-scheduler-center-embed：嵌入式调度中心，提供集群内的执行单机任务、执行单机常驻任务、执行MapReduce任务的能力，依赖注册中心。

【Planning】winter-workflow-engine-embed：嵌入式流程引擎，基于SmartEngine扩展，实现基于内存的BPMN流程执行引擎。

【Planning】winter-strategy-embed：嵌入式策略，实现基本的策略模式，可用于实现系统的扩展和插件功能。

【Planning】winter-embed-web-manager: 嵌入式web管理工具，可管理配置项、调度任务、线程池、BPMN流程等

【Planning】winter-embed-data-sync：数据同步工具，基于数据扫描的方式，实现从一个数据来源（如Mysql）同步到另一个数据来源（如Mysql），扫描模式通常存在一定的延迟，适合非实时业务场景，如历史库迁移备份、实时业务库到离线分析库等场景。

### 架构设计图：

![架构设计图](doc/img.png)
