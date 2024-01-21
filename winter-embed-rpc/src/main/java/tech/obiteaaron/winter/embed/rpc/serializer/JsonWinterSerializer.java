package tech.obiteaaron.winter.embed.rpc.serializer;

import tech.obiteaaron.winter.common.tools.json.JsonUtil;

public class JsonWinterSerializer implements WinterSerializer{
    @Override
    public String type() {
        return "json";
    }

    @Override
    public String serializer(Object object) {
        return JsonUtil.toJsonString(object);
    }
}
