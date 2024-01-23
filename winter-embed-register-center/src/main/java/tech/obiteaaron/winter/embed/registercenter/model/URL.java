package tech.obiteaaron.winter.embed.registercenter.model;

import lombok.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;
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
                .protocol(urlJava.getProtocol())
                .path(urlJava.getPath())
                .parameterMap(getParameterMap(urlJava.getQuery()))
                .ip(urlJava.getHost())
                .port(urlJava.getPort())
                .build();
        return url;
    }

    @NotNull
    public static Map<String, String> getParameterMap(String query) {
        if (StringUtils.isBlank(query)) {
            return new HashMap<>();
        }
        String[] split = StringUtils.split(query, '&');
        return Arrays.stream(split).map(item -> {
            String[] split1 = StringUtils.split(item, '=');
            String s1 = split1[0];
            if (split1.length > 1) {
                try {
                    return Pair.of(s1, URLDecoder.decode(split1[1], "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException(e);
                }
            } else {
                return Pair.of(s1, (String) null);
            }
        }).collect(Collectors.toMap(Pair::getLeft, Pair::getValue, (a, b) -> a));
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(protocol);
        stringBuilder.append("://");
        stringBuilder.append(ip);
        stringBuilder.append(':');
        stringBuilder.append(port);
        stringBuilder.append('/');
        stringBuilder.append(path);
        if (parameterMap != null) {
            stringBuilder.append("?");
            StringJoiner stringJoiner = new StringJoiner("&");
            parameterMap.forEach((k, v) -> {
                try {
                    stringJoiner.add(k + "=" + URLEncoder.encode(v, "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException(e);
                }
            });
            stringBuilder.append(stringJoiner.toString());
        }
        return stringBuilder.toString();
    }
}
