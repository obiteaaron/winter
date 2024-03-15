## 说明

提供一些嵌入式、包含基本功能的中间件，基于Mysql/Redis，避免引入额外中间件带来更大的维护成本，为单体应用和小集群提供基础中间件解决方案。

本系列软件都是基于可用功能的最简化版本，只为了能够在项目中支持可配置、可编排、可扩展、可分布式部署等特性，也方便在特定情况下的多系统RPC调用。能够在满足常见的一些使用场景的情况下，降低开发成本，提升开发效率。

本系列软件分为主要模块和次要模块，主要模块属于优先支持、优先升级的核心部分，次要模块主要是为了辅助实现主要模块的功能，或提供一些基本功能以满足部分使用场景。

【Release】【主要模块】winter-embed-common-tools：提供一些高阶的常用工具，主要还是项目内使用。

【Release】【主要模块】winter-embed-config-center：嵌入式配置中心，基于Mysql提供秒级配置变更推送能力。

【Beta】【次要模块】winter-embed-register-center：嵌入式注册中心，提供集群的IP发现，服务注册、发现，基于config-center实现。

【Release】【主要模块】winter-embed-rpc：嵌入式RPC框架，提供基本的服务注册、服务远程调用功能，基于HTTP(S)提供远程调用，大部分组件可以自由替换和扩展。依赖register-center、config-center。

【Beta】【主要模块】winter-embed-scheduler-center：嵌入式调度中心，提供集群内的执行单机任务、执行单机常驻任务、执行MapReduce任务的能力。普通任务和Spring的@Scheduled基本一致，而Map、MapReduce任务依赖winter-embed-rpc模块实现多机分发子任务。

【Release】【主要模块】winter-embed-workflow-engine：嵌入式流程引擎，基于SmartEngine扩展，实现基于内存的高性能BPMN流程执行引擎。该模块直接在Fork的Github仓库里面更新，不需要嵌入当前仓库 [SmartEngine-Enhance](https://github.com/obiteaaron/SmartEngine-Enhance) 。

【Planning】winter-embed-strategy-center：嵌入式策略中心，实现基本的策略模式，可用于内外部的系统扩展，实现系统的扩展和插件功能。

【Planning】winter-embed-data-sync：数据同步工具，基于数据扫描的方式，无需binlog技术，实现从一个数据来源（如Mysql）同步到另一个数据来源（如Mysql），扫描模式通常存在一定的延迟，适合非实时业务场景，如历史库迁移备份、实时业务库到离线分析库等场景。

【Planning】【次要模块】winter-embed-web-manager: 嵌入式web管理工具，可管理配置项、调度任务、线程池、BPMN流程等

### 其他软件
规则引擎：实现业务规则动态加载和执行，可用于实现系统的扩展和动态插件功能。参考Drools、Groovy、Mvel2等都可以作为规则引擎基座。对于我需要实现如何使用的规则引擎，还需要再调研考虑。


### 架构设计图

![架构设计图](doc/img.png)
