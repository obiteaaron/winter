package tech.obiteaaron.winter.embed.rpc.server.impl;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.http.HttpServerRequest;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import tech.obiteaaron.winter.common.tools.trace.Slf4jMdcUtil;
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
     *
     * @param port
     */
    @Override
    public void startHttpServer(int port) {
        log.info("VertxHttpServer starting");
        VertxOptions vertxOptions = new VertxOptions();
        vertxOptions.setEventLoopPoolSize(1);
        vertxOptions.setWorkerPoolSize(50);
        Vertx vertx = Vertx.vertx(vertxOptions);

        HttpServerOptions httpServerOptions = new HttpServerOptions();
        // 直接用默认值
        vertx.createHttpServer(httpServerOptions)
                .exceptionHandler(t -> log.error("executing exception, please check netty version or business code."))
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
