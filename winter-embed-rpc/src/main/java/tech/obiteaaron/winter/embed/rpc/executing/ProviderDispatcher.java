package tech.obiteaaron.winter.embed.rpc.executing;

import io.vertx.core.http.HttpServerRequest;

import java.util.concurrent.ThreadPoolExecutor;

public interface ProviderDispatcher {
    String dispatch(HttpServerRequest httpServerRequest, String body);

    void setWinterRpcBootstrap(tech.obiteaaron.winter.embed.rpc.WinterRpcBootstrap winterRpcBootstrap);

    void setThreadPoolExecutor(ThreadPoolExecutor threadPoolExecutor);
}
