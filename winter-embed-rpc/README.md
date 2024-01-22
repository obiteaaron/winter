1. 参考DUBBO服务定义用URL定义服务资源
2. 参考Nacos，用naming的方式注册服务，serviceName --> ip:port --> 服务详细元数据的形式，注册到注册中心上（从配置中心扩展出来）
3. RPC默认用IP调用，支持内网集群负载均衡调用
4. RPC支持公网、域名调用，可在Consumer上直接定义公网IP、域名、域名解析器类名等等，RPC框架会直接调用，而不通过内网的方式调用
5. 多网卡场景下，可以通过IP前缀方式指定需要使用的IP地址
6. 可直接使用Provider注册的内存模型进行调用，方便直接集成到SpringMVC里面使用。

URL格式  
`winterrpc://ip:port/serviceName?methods=a,b,c&其他自定义参数`  
示例：  
`winterrpc://10.0.0.1:7788/tech.obiteaaron.winter.embed.rpc.UserService?methods=findById(java.lang.String),findByOpenId(java.lang.String)&type=provider&其他自定义参数`

RPC vs Dubbo 
1. Dubbo主要解决的是集群范围内的、跨应用之间的、内网的RPC调用。Winter Rpc 解决的是本应用内部的RPC调用（可用来做MapReduce任务），基于公网HTTP的、跨应用之间的RPC调用。
2. Dubbo 2.x 的HTTP模式暴露服务，是为了方便其他微服务应用基于HTTP调用本应用。但并未提供基于HTTP的客户端应用。非HTTP的模式下，只支持私有的DUBBO协议，无法方便地测试和连接其他系统。也难以在DUBBO上层再包装一层处理其他业务逻辑。
3. Dubbo 3.x 提供了TLS支持，方便在非可信环境下保证数据安全，而Winter Rpc 在内网使用不支持加密HTTP协议，在公网建议通过HTTPS协议进行传输。 
4. Dubbo 3.x 支持使用token令牌验证，Winter Rpc 同样提供了简单的全局令牌验证机制。 
5. Dubbo 3.x 的TRIPLE协议，兼容GRPC协议，支持HTTP2调用，也扩展了HTTP1调用，也支持JSON格式的数据传输。也就是支持了CURL和浏览器访问。同时也支持和其他的GRPC客户端、服务端进行交互。这一点很好用，只需要自己实现一个嵌入式的注册中心即可（因为Dubbo 3.x 没有Redis注册中心了）。但如果你的系统本身就在用DUBBO，和其他应用集群存在互调用。再新建一个新的DUBBO实例做外部集成时，多DUBBO实例的定义（2.7.x和3.0.x不支持，3.1以上新支持，新旧版本差异较大），不确定是否会影响到原本系统里面支持的DUBBO服务（比如SPI扩展会都使用到）。以及新旧版本不能并存，也是一个需要考虑的问题。
6. 基于第5点，如果要自定义一个新的DUBBO实例，那么依然需要实现单独的注解扫描、嵌入式注册中心。如此一来，只需要再多实现一个服务URL定义，那么使用VertX暴露HTTP服务就和使用DUBBO基本一样了。当然这里未包含扩展机制、监控机制。实现起来会更简单。
7. Dubbo 提供基于Filter的SPI扩展，Winter Rpc 提供基于同等技术的Filter扩展。 
8. DUBBO 的序列化实现更抽象化一些，它完全设计了单独的序列化和反序列化对象，可以将多个对象分别写入，所以可以先写基本信息（含调用方法信息），再写参数对象，两个可以独立写，也就可以独立读，但不易直接调用。Winter Rpc为了能够直接用JSON curl调用，并且不依赖注解，采用了内置的反射方式进行JSON序列化。但还是建议生产环境使用Hessian，性能更好。
