package tech.obiteaaron.winter.embed.rpc.server.impl;

import io.vertx.core.shareddata.Shareable;
import lombok.Getter;
import tech.obiteaaron.winter.embed.rpc.WinterRpcBootstrap;

public class VertxSharableObject implements Shareable {

    @Getter
    private WinterRpcBootstrap winterRpcBootstrap;

    public VertxSharableObject(WinterRpcBootstrap winterRpcBootstrap) {
        this.winterRpcBootstrap = winterRpcBootstrap;
    }
}
