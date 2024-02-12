package tech.obiteaaron.winter.embed.rpc.serializer;

import tech.obiteaaron.winter.common.tools.json.JsonUtils;

public class JsonWinterSerializer implements WinterSerializer{
    @Override
    public String type() {
        return "json";
    }

    @Override
    public String serializer(Object object) {
        return JsonUtils.toJsonString(object);
    }
}
