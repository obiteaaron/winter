package tech.obiteaaron.winter.embed.rpc.serializer;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class JsonWinterDeserializer implements WinterDeserializer {
    @Override
    public String type() {
        return "json";
    }

    @Override
    public Object deserializer(String value) {
        String[] split = StringUtils.splitByWholeSeparator(value, JsonWinterSerializer.SPLITTER);
        String type = split[0];
        AtomicInteger index = new AtomicInteger(1);
        if ("object".equals(type)) {
            Object object = doReadOneObject(split, index);
            return object;
        } else if ("array".equals(type)) {
            List<Object> objects = new ArrayList<>();
            while (index.get() < split.length) {
                Object object = doReadOneObject(split, index);
                objects.add(object);
            }
            return objects.toArray();
        } else {
            throw new UnsupportedOperationException(type);
        }
    }

    private Object doReadOneObject(String[] split, AtomicInteger index) {
        String className = split[index.getAndIncrement()];
        String json = split[index.getAndIncrement()];
        return JsonWinterSerializer.JsonUtils.parseObject(json, className);
    }
}
