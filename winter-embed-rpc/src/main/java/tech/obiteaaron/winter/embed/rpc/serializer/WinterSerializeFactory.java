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
        return null;
    }

    public static WinterSerializer getWinterSerializer(String type) {
        // TODO 单例
        switch (type) {
            case "hessian":
                return new HessianWinterSerializer();
            case "json":
                return new JsonWinterSerializer();
            default:
                throw new UnsupportedOperationException(type);
        }
    }

    public static WinterDeserializer getWinterDeserializer(String type) {
        // TODO 单例
        switch (type) {
            case "hessian":
                return new HessianWinterDeserializer();
            case "json":
                return new JsonWinterDeserializer();
            default:
                throw new UnsupportedOperationException(type);
        }
    }
}
