package tech.obiteaaron.winter.embed.rpc.executing;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InvokeContext {

    private String serviceName;
    private String methodName;
    /**
     * queryById(java.lang.String)
     */
    private String methodSignature;

    private Object[] arguments;

}