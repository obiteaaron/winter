package tech.obiteaaron.winter.embed.registercenter.model;

import lombok.*;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class URL {

    String protocol;

    String ip;

    int port;

    String path;

    Map<String, String> parameterMap;

    public static URL fromUrl(String consumerUrl) {
        return null;
    }
}
