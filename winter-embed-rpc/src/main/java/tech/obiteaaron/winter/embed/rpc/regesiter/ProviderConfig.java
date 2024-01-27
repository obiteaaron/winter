package tech.obiteaaron.winter.embed.rpc.regesiter;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProviderConfig {

    String applicationName;

    Class<?> interfaceClass;

    String interfaceName;

    Object interfaceImpl;

    String providerSerializerSupports;

    String group;

    String version;

}
