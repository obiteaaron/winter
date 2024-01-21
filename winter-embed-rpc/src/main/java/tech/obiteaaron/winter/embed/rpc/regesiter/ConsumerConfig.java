package tech.obiteaaron.winter.embed.rpc.regesiter;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConsumerConfig {

    Class<?> interfaceClass;

    String interfaceName;

    String consumerSerializerSupports;
}
