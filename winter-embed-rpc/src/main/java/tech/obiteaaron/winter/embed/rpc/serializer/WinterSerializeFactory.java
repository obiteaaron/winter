package tech.obiteaaron.winter.embed.rpc.serializer;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

public class WinterSerializeFactory {

    /**
     * 以服务端支持的序列化类型为准，按客户端支持的序列化类型进行一次筛选
     *
     * @param providerSerializerSupports 服务端支持的序列化类型
     * @param consumerSerializerSupports 客户端支持的序列化类型
     * @return 选择的序列化类型
     */
    public static String resolveSerializerType(String providerSerializerSupports, String consumerSerializerSupports, String defaultSerializer) {
        if (StringUtils.isBlank(providerSerializerSupports)) {
            return defaultSerializer;
        }

        String[] providerSerializer = StringUtils.split(providerSerializerSupports, ',');
        if (StringUtils.isBlank(consumerSerializerSupports)) {
            return providerSerializer[0];
        }
        String[] consumerSerializer = StringUtils.split(consumerSerializerSupports, ',');
        for (String serializer : consumerSerializer) {
            if (Arrays.asList(providerSerializer).contains(serializer)) {
                return serializer;
            }
        }
        // 兜底用默认的
        return defaultSerializer;
    }

    public static WinterSerializer getWinterSerializer(String type) {
        // 单例
        switch (type) {
            case "hessian":
                return SerializerHolder.hessianWinterSerializer;
            case "json":
                return SerializerHolder.jsonWinterSerializer;
            default:
                throw new UnsupportedOperationException(type);
        }
    }

    public static WinterDeserializer getWinterDeserializer(String type) {
        // 单例
        switch (type) {
            case "hessian":
                return SerializerHolder.hessianWinterDeserializer;
            case "json":
                return SerializerHolder.jsonWinterDeserializer;
            default:
                throw new UnsupportedOperationException(type);
        }
    }

    private static class SerializerHolder {
        static final HessianWinterSerializer hessianWinterSerializer = new HessianWinterSerializer();
        static final JsonWinterSerializer jsonWinterSerializer = new JsonWinterSerializer();
        static final HessianWinterDeserializer hessianWinterDeserializer = new HessianWinterDeserializer();
        static final JsonWinterDeserializer jsonWinterDeserializer = new JsonWinterDeserializer();
    }
}
