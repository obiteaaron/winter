package tech.obiteaaron.winter.embed.rpc.regesiter;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConsumerConfig {

    String applicationName;

    Class<?> interfaceClass;

    String interfaceName;

    String consumerSerializerSupports;

    String group;

    String version;
}
