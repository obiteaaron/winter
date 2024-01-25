package tech.obiteaaron.winter.embed.rpc.executing;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InvokeContext {

    private String applicationName;

    private String traceId;

    private String serviceName;
    /**
     * queryById(java.lang.String)
     */
    private String methodSignature;

    private String serializerType;

    private Object[] arguments;

    private Object result;

}