package tech.obiteaaron.winter.embed.rpc.executing;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

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