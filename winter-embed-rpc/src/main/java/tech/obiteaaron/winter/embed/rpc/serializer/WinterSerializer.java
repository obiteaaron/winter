package tech.obiteaaron.winter.embed.rpc.serializer;

public interface WinterSerializer {

    String type();

    String serializer(Object object);
}
