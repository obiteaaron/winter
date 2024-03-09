package tech.obiteaaron.winter.embed.rpc.serializer;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import org.apache.commons.lang3.StringUtils;

import java.util.StringJoiner;

public class JsonWinterSerializer implements WinterSerializer {

    static final String SPLITTER = "$WRSJ$";

    @Override
    public String type() {
        return "json";
    }

    @Override
    public String serializer(Object object) {
        StringJoiner stringJoiner = new StringJoiner(SPLITTER);
        serializer0(object, stringJoiner);
        System.out.println(stringJoiner.toString());
        return stringJoiner.toString();
    }

    public void serializer0(Object object, StringJoiner stringJoiner) {
        // 此处需要做文章，虽然是用JSON工具，但需要能够传递一些必要的信息过去，以方便反序列化
        // 整个对象序列化分为多个JSON字符串，多个JSON字符串之间用分隔符拼接。
        // 对象还是数组|最外层单个对象的全名称（数组的话，则一个一个来）|JSON字符串值|
        if (object instanceof Object[]) {
            stringJoiner.add("array");
            for (Object o : ((Object[]) object)) {
                serializer0(o, stringJoiner);
            }
        } else {
            stringJoiner.add("object");
            doSerializerOneObject(object, stringJoiner);
        }
    }

    private void doSerializerOneObject(Object object, StringJoiner stringJoiner) {
        String name = object.getClass().getName();
        stringJoiner.add(name);
        String jsonString = JsonUtils.toJsonString(object);
        stringJoiner.add(jsonString);
    }


    /**
     * JSON解析工具，整个工程全部都采用这个工具进行解析，方便统一替换具体实现类
     */
    static class JsonUtils {

        private static final ObjectMapper objectMapper;

        static {
            objectMapper = new ObjectMapper();
            objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
//            objectMapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_CONCRETE_AND_ARRAYS);
            objectMapper.activateDefaultTyping(LaissezFaireSubTypeValidator.instance, ObjectMapper.DefaultTyping.NON_CONCRETE_AND_ARRAYS);
//        objectMapper.activateDefaultTyping(BasicPolymorphicTypeValidator.builder()
//                        .allowIfBaseType(Object.class)
//                .build(), ObjectMapper.DefaultTyping.NON_CONCRETE_AND_ARRAYS);
        }

        public static Object parseObject(String jsonString, String className) {
            try {
                if (StringUtils.isBlank(jsonString)) {
                    return null;
                }
                JavaType javaType = objectMapper.getTypeFactory().constructType(Class.forName(className));
                return objectMapper.readValue(jsonString, javaType);
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
    }

}
