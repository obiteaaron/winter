package tech.obiteaaron.winter.embed.registercenter.model;

import lombok.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;

import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

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

    public static URL fromUrl(String urlString) {
        java.net.URL urlJava;
        try {
            urlJava = new java.net.URL(urlString);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        URL url = URL.builder()
                .path(urlJava.getPath())
                .parameterMap(getParameterMap(urlJava.getQuery()))
                .ip(urlJava.getHost())
                .port(urlJava.getPort())
                .build();
        return url;
    }

    @NotNull
    public static Map<String, String> getParameterMap(String query) {
        String[] split = StringUtils.split(query, '&');
        return Arrays.stream(split).map(item -> {
            String[] split1 = StringUtils.split(item, '=');
            String s1 = split1[0];
            if (split1.length > 1) {
                return Pair.of(s1, split1[1]);
            } else {
                return Pair.of(s1, (String) null);
            }
        }).collect(Collectors.toMap(Pair::getLeft, Pair::getValue, (a, b) -> a));
    }
}
