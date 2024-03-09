package tech.obiteaaron.winter.embed.rpc.serializer;

public interface WinterDeserializer {

    String type();

    Object deserializer(String value);
}
