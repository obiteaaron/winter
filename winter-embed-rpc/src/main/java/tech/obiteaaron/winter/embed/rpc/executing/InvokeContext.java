package tech.obiteaaron.winter.embed.rpc.executing;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@Accessors(chain = true)
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

    /**
     * 异步请求ID，当作为异步请求时，此处传本次请求地唯一ID，用于服务端记录结果、查询结果
     */
    private String asyncRequestId;
    /**
     * @see tech.obiteaaron.winter.embed.rpc.async.AsyncActionEnum
     */
    private String asyncAction;
    private int executeTimeout;
    /**
     * 自定义扩展信息
     */
    private Map<String, String> extInfo;

    public InvokeContext addExt(String key, String value) {
        if (extInfo == null) {
            extInfo = new HashMap<>();
        }
        if (StringUtils.isBlank(key)) {
            return this;
        }
        extInfo.put(key, value);
        return this;
    }
}