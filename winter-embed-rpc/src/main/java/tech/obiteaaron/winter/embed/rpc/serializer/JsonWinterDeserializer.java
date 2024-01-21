package tech.obiteaaron.winter.embed.rpc.serializer;

import com.fasterxml.jackson.databind.JavaType;
import tech.obiteaaron.winter.common.tools.json.JsonUtil;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class JsonWinterDeserializer implements WinterDeserializer {
    @Override
    public String type() {
        return "json";
    }

    @Override
    public Object deserializer(String value, Type[] types) {
        if (value == null) {
            return Arrays.stream(types).map(item -> null).collect(Collectors.toList());
        }
        if (value.startsWith("{") && value.endsWith("}")) {
            // 单值对象
            JavaType javaType = JsonUtil.getTypeFactory().constructType(types[0]);
            return JsonUtil.parseObject(value, javaType);
        }
        // 这会导致序列化两次，严重影响性能
        // 不支持抽象类，必须使用具体类型
        List<Object> arguments = JsonUtil.parseArray(value, Object.class);
        Object[] objects = new Object[types.length];
        int i = 0;
        for (Object argument : arguments) {
            if (argument == null) {
                objects[i++] = null;
                continue;
            }
            String jsonString = JsonUtil.toJsonString(argument);
            JavaType javaType = JsonUtil.getTypeFactory().constructType(types[i]);
            Object o = JsonUtil.parseObject(jsonString, javaType);
            objects[i++] = o;
        }
        return objects;
    }
}
