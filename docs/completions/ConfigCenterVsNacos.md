1. Nacos是C/S架构，Winter ConfigCenter只是嵌入式的架构。
2. Nacos的实现里面用JRaft作为集群选主，集群数据同步的工具。Winter ConfigCenter 未使用同类技术，主要是面向的集群大小不一样，Winter
   ConfigCenter只是一个小型的、嵌入式的、面向小型集群的、开箱即用的配置中心，采用的是短轮询拉取的方案，和Nacos等C/S架构、长轮询拉取的配置中心方式不同。因此不需要集群选主、集群数据同步的功能，各嵌入式客户端短轮询拉取即可。
3. Nacos集群模式下，需要至少3台机器，且需要在cluster.conf中指定对应的机器和端口，虽然也可以动态调整，但仍然需要较高的维护成本。Winter
   ConfigCenter 采用直连数据库，无状态、无集群设计，比不上Nacos专业，但对小型系统完全足够，而且降低了C/S架构的维护成本。