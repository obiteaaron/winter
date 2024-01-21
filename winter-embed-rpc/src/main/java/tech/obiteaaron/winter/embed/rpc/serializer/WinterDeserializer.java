package tech.obiteaaron.winter.embed.rpc.serializer;

import java.lang.reflect.Type;

public interface WinterDeserializer {

    String type();

    Object deserializer(String value, Type[] types);
}
