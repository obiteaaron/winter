package tech.obiteaaron.winter.embed.rpc.server.impl;

import io.vertx.core.*;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonObject;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import tech.obiteaaron.winter.common.tools.trace.Slf4jMdcUtils;
import tech.obiteaaron.winter.embed.rpc.WinterRpcBootstrap;
import tech.obiteaaron.winter.embed.rpc.server.HttpServer;

import java.nio.charset.StandardCharsets;

@Slf4j
public class VertxHttpServerImpl implements HttpServer {

    @Setter
    private WinterRpcBootstrap winterRpcBootstrap;

    /**
     * 需要确保Netty版本正确，否则可能会无法启动
     * 无vertx-web，自己写简单路由即可，vertx-web的性能好像不太行
     * vertx的两种工作模式：https://vertx.io/docs/vertx-core/java/#_verticle_types
     * <p>
     * Vertx的架构是一个Verticle只有一个EventLoop（Thread）在运行。默认都在EventLoop线程下执行，如果需要用Worker线程，需要配置参数。
     * https://vertxchina.github.io/vertx-translation-chinese/start/FAQ.html
     * 一个vert.x实例/进程内有多个Eventloop和Worker线程，每个线程会部署多个Verticle对象并对应执行Verticle内的Handler，每个Verticle内有多个Handler，普通Verticle会跟Eventloop绑定，而Worker Verticle对象则会被Worker线程所共享，会依次顺序访问，但不会并发同时访问
     * <p>
     * 如果需要多线程运行，必须部署多个实例，无论采用EventLoop模式还是Worker模式，都必须部署多个实例才能实现多线程，才符合Vertx架构设计。
     * 配置线程模式，io.vertx.core.DeploymentOptions#setThreadingModel(io.vertx.core.ThreadingModel)为Worker
     * 配置多实例，io.vertx.core.DeploymentOptions#setInstances(int)
     * 实测发现，其他参数都相同的情况下，用Worker会比用EventLoop快一些，但还不确定为什么？
     * <p>
     * 也可以自己用多线程执行，但返回值不好处理，因为返回值需要回到EventLoop线程才能返回。
     * 虚拟线程需要Java21支持，可通过参数开启
     *
     * @param port
     */
    @Override
    public void startHttpServer(int port, int workThreadPoolSize, boolean useVirtualThread) {
        log.info("VertxHttpServer starting");
        VertxOptions vertxOptions = new VertxOptions();
        vertxOptions.setEventLoopPoolSize(workThreadPoolSize);
        vertxOptions.setWorkerPoolSize(workThreadPoolSize);
        Vertx vertx = Vertx.vertx(vertxOptions);

        DeploymentOptions options = new DeploymentOptions()
                .setConfig(JsonObject.of("port", port, "winterRpcBootstrap", new VertxSharableObject(winterRpcBootstrap)))
                .setThreadingModel(useVirtualThread ? ThreadingModel.VIRTUAL_THREAD : ThreadingModel.WORKER)
                // 部署多实例
                .setInstances(workThreadPoolSize);
        vertx.deployVerticle(VertxHttpVerticle.class, options);
    }

    public static class VertxHttpVerticle extends AbstractVerticle {

        @Setter
        private WinterRpcBootstrap winterRpcBootstrap;

        @Override
        public void start() {
            JsonObject config = config();
            Integer port = config.getInteger("port");
            VertxSharableObject vertxSharableObject = (VertxSharableObject) config.getValue("winterRpcBootstrap");
            this.setWinterRpcBootstrap(vertxSharableObject.getWinterRpcBootstrap());
            HttpServerOptions httpServerOptions = new HttpServerOptions();
            // 直接用默认值
            vertx.createHttpServer(httpServerOptions)
                    .exceptionHandler(t -> log.error("executing exception, please check netty version or business code.", t))
                    .invalidRequestHandler(request -> log.error("invalidRequestHandler"))
                    .requestHandler(this::requestHandler)
                    .listen(port, asyncResult -> log.info("VertxHttpServer started {}", asyncResult.succeeded() ? "success" : "failed"));
            log.info("VertxHttpServer start finish, port {}", port);
        }

        private void requestHandler(HttpServerRequest httpServerRequest) {
            String traceId = httpServerRequest.getParam("traceId");
            httpServerRequest.bodyHandler(body -> {
                try {
                    Slf4jMdcUtils.appendMdcForTrace(traceId);
                    String result = winterRpcBootstrap.getProviderDispatcher().dispatch(httpServerRequest, body.toString(StandardCharsets.UTF_8));
                    httpServerRequest.response()
                            .putHeader("content-type", "text/plain")
                            .end(result);

                } catch (Throwable t) {
                    log.error("VertxHttpServer Request Exception", t);
                    httpServerRequest.response()
                            .putHeader("content-type", "text/plain")
                            .end("FAILED:" + t.toString());
                } finally {
                    Slf4jMdcUtils.clearMdcComplete();
                }
            });
        }
    }
}
