package tech.obiteaaron.winter.embed.rpc.serializer;

import com.fasterxml.jackson.databind.JavaType;
import tech.obiteaaron.winter.common.tools.json.JsonUtil;
import tech.obiteaaron.winter.embed.rpc.executing.InvokeContext;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class JsonWinterDeserializer implements WinterDeserializer {
    @Override
    public String type() {
        return "json";
    }

    @Override
    public Object deserializer(String value, boolean isArray, String[] types, String[] invocationParameterTypes) {
        if (isArray) {
            // 按参数的 Object[] 反序列化
            // 这会导致序列化两次，严重影响性能
            // 不支持抽象类，必须使用具体类型
            if (value == null) {
                return Arrays.stream(types).map(item -> null).collect(Collectors.toList());
            }
            List<Object> arguments = JsonUtil.parseArray(value, Object.class);
            Object[] objects = new Object[types.length];
            int i = 0;
            for (Object argument : arguments) {
                if (argument == null) {
                    objects[i++] = null;
                    continue;
                }
                String jsonString = JsonUtil.toJsonString(argument);
                JavaType javaType = JsonUtil.getTypeFactory().constructFromCanonical(types[i]);
                Object o = JsonUtil.parseObject(jsonString, javaType);
                objects[i++] = o;
            }
            return objects;
        } else {
            if (value == null) {
                return null;
            }
            // 按返回值的 Object 反序列化
            // 单值对象
            JavaType javaType = JsonUtil.getTypeFactory().constructFromCanonical(types[0]);
            if (InvokeContext.class.getCanonicalName().equals(types[0])) {
                InvokeContext invokeContext = JsonUtil.parseObject(value, javaType);

                // 特殊处理，二次序列化参数类型
                Object[] arguments = invokeContext.getArguments();
                if (arguments != null) {
                    String[] argumentTypes = invocationParameterTypes;
                    Object deserializer = this.deserializer(JsonUtil.toJsonString(arguments), true, argumentTypes, null);
                    invokeContext.setArguments((Object[]) deserializer);
                }
                return invokeContext;
            } else {
                return JsonUtil.parseObject(value, javaType);
            }
        }
    }
}
