package tech.obiteaaron.winter.common.tools.json;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * JSON解析工具，整个工程全部都采用这个工具进行解析，方便统一替换具体实现类
 */
public class JsonUtils {

    private static final ObjectMapper objectMapper;

    static {
        objectMapper = new ObjectMapper();
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public static <T> T parseObject(String jsonString, Class<T> clazz) {
        try {
            if (StringUtils.isBlank(jsonString)) {
                return null;
            }
            return objectMapper.readValue(jsonString, clazz);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> List<T> parseArray(String jsonString, Class<T> clazz) {
        try {
            if (StringUtils.isBlank(jsonString)) {
                return null;
            }
            JavaType javaType = objectMapper.getTypeFactory().constructParametricType(ArrayList.class, clazz);
            return objectMapper.readValue(jsonString, javaType);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T parseObject(String jsonString, JavaType javaType) {
        try {
            if (StringUtils.isBlank(jsonString)) {
                return null;
            }
            return objectMapper.readValue(jsonString, javaType);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T parseObject(String jsonString, TypeReference<T> typeReference) {
        try {
            if (StringUtils.isBlank(jsonString)) {
                return null;
            }
            return objectMapper.readValue(jsonString, typeReference);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String toJsonString(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static TypeFactory getTypeFactory() {
        try {
            return objectMapper.getTypeFactory();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
