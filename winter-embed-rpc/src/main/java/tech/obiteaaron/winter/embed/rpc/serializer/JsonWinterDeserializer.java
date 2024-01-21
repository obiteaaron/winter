package tech.obiteaaron.winter.embed.rpc.serializer;

import com.fasterxml.jackson.databind.JavaType;
import tech.obiteaaron.winter.common.tools.json.JsonUtil;

public class JsonWinterDeserializer implements WinterDeserializer {
    @Override
    public String type() {
        return "json";
    }

    @Override
    public Object deserializer(String value, Class<?>[] types) {
        JavaType javaType = JsonUtil.getTypeFactory().constructParametricType(Object[].class, types);

        return JsonUtil.parseObject(value, javaType);
    }
}
