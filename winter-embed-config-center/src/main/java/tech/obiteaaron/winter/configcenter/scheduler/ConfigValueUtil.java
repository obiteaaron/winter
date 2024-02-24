package tech.obiteaaron.winter.configcenter.scheduler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.lang.reflect.Type;

class ConfigValueUtil {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    static {
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    static Object parseValue(String content, Class<?> type, Type genericType) {
        if (content == null) {
            return null;
        }
        if (String.class.isAssignableFrom(type)) {
            return content;
        }
        if (type.isPrimitive()) {
            if (boolean.class.equals(type)) {
                return Boolean.valueOf(content);
            }
            if (int.class.equals(type)) {
                return Integer.valueOf(content);
            }
            if (long.class.equals(type)) {
                return Long.valueOf(content);
            }
            if (double.class.equals(type)) {
                return Double.valueOf(content);
            }
        }
        try {
            JavaType javaType = objectMapper.getTypeFactory().constructType(genericType);
            @SuppressWarnings("UnnecessaryLocalVariable")
            Object readValue = objectMapper.readValue(content, javaType);
            return readValue;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    static String stringify(Object value, Class<?> type, Type genericType) {
        if (value == null) {
            return null;
        }
        if (String.class.isAssignableFrom(type)) {
            return (String) value;
        }
        if (type.isPrimitive()) {
            if (boolean.class.equals(type)) {
                return Boolean.toString((Boolean) value);
            }
            if (int.class.equals(type)) {
                return Integer.toString((Integer) value);
            }
            if (long.class.equals(type)) {
                return Long.toString((Long) value);
            }
            if (double.class.equals(type)) {
                return Double.toString((Double) value);
            }
        }
        try {
            @SuppressWarnings("UnnecessaryLocalVariable")
            String writeValueAsString = objectMapper.writeValueAsString(value);
            return writeValueAsString;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
