### v1.1.0

发布日期：2024年01月20日  
发布内容：

- 支持 SpringBoot 3.x 自动配置启动
- 修改时间字段gmtCreate和gmtModified

### v1.0.0

发布日期：2022年12月11日  
发布内容：

- 提供自动拉取配置功能，见`ConfigCenterPullTask`
- 提供配置管理接口，见`ConfigManager`
- 提供注解配置（推荐），见`@ConfigValue`
- 支持自定义配置，见`ConfigCenter#registerListener`
- 默认集成SpringBoot，也可以自行集成到Spring
- 支持手动启动（非Spring），参考`ConfigCenterSpringAutoConfiguration`，见`ConfigCenter#initConfigFromBeans`
  和`ConfigCenter#start`
- 采用Mysql作为持久化存储，采用JdbcTemplate执行SQL
- 拉取配置频率为5秒的准实时配置中心
