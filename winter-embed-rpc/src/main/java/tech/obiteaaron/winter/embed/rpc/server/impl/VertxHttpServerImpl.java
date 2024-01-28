package tech.obiteaaron.winter.embed.rpc.server.impl;

import io.vertx.core.*;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonObject;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import tech.obiteaaron.winter.common.tools.trace.Slf4jMdcUtil;
import tech.obiteaaron.winter.embed.rpc.WinterRpcBootstrap;
import tech.obiteaaron.winter.embed.rpc.server.HttpServer;

import java.nio.charset.StandardCharsets;

@Slf4j
public class VertxHttpServerImpl extends AbstractVerticle implements HttpServer {

    @Setter
    private WinterRpcBootstrap winterRpcBootstrap;

    /**
     * 需要确保Netty版本正确，否则可能会无法启动
     * 无vertx-web，自己写简单路由即可，vertx-web的性能好像不太行
     * vertx的三种工作模式：https://vertx.io/docs/vertx-core/java/#_verticle_types
     * <p>
     * Vertx的架构是一个Verticle只有一个EventLoop（Thread）在运行。
     * 如果需要多线程运行，可以部署多实例，符合Vertx架构；
     * 也可以自己用多线程执行，但返回值不好处理，因为返回值需要回到EventLoop线程才能返回。
     * 虚拟线程需要Java21支持，可通过参数开启
     *
     * @param port
     */
    @Override
    public void startHttpServer(int port, int workThreadPoolSize, boolean useVirtualThread) {
        log.info("VertxHttpServer starting");
        VertxOptions vertxOptions = new VertxOptions();
//        vertxOptions.setEventLoopPoolSize(1);
        vertxOptions.setWorkerPoolSize(workThreadPoolSize);
        Vertx vertx = Vertx.vertx(vertxOptions);

        DeploymentOptions options = new DeploymentOptions()
                .setConfig(JsonObject.of("port", port, "winterRpcBootstrap", new VertxSharableObject(winterRpcBootstrap)))
                .setThreadingModel(useVirtualThread ? ThreadingModel.VIRTUAL_THREAD : ThreadingModel.WORKER)
                // 部署多实例
                .setInstances(workThreadPoolSize);
        vertx.deployVerticle(VertxHttpServerImpl.class, options);
    }

    @Override
    public void start() throws Exception {
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
                Slf4jMdcUtil.appendMdcForTrace(traceId);
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
                Slf4jMdcUtil.clearMdcComplete();
            }
        });
    }

}
