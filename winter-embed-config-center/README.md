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
