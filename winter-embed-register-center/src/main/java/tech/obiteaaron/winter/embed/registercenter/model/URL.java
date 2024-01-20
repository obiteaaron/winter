package tech.obiteaaron.winter.embed.registercenter.model;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class URL {

    String path;

    Map<String,String> parameterMap;

    public static URL fromUrl(String consumerUrl) {
        return null;
    }
}
