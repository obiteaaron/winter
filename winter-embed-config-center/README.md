## 使用方法

1. 由于没有打包到中央仓库，所以需要使用者自己打包。拉取代码并打包到你自己的Maven仓库，或者`mvn clean install`到本地（仅本地测试用）
2. 引入打包好的jar包
   ```xml
   <dependency>
      <groupId>tech.obiteaaron.winter</groupId>
      <artifactId>winter-embed-config-center</artifactId>
      <version>1.0.0-SNAPSHOT</version>
   </dependency>
   ```
3. SpringBoot工程可直接启动
4. Spring工程需要增加`tech.obiteaaron.winter.configcenter.ConfigCenterSpringAutoConfiguration`到扫描路径即可

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

### v1.1.0

发布日期：2024年01月20日  
发布内容：

- 支持 SpringBoot 3.x 自动配置启动
- 修改时间字段gmtCreate和gmtModified

