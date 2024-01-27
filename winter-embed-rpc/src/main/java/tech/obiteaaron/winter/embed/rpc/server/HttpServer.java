package tech.obiteaaron.winter.embed.rpc.server;

public interface HttpServer {

    void startHttpServer(int port, int workThreadPoolSize);

    void setWinterRpcBootstrap(tech.obiteaaron.winter.embed.rpc.WinterRpcBootstrap winterRpcBootstrap);
}
