package tech.obiteaaron.winter.common.tools.json;

import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.Test;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

public class JsonUtilsTest {

    @Test
    public void test() {
        Map<String, Object> stringObjectMap = JsonUtils.parseObject("{\"a\":11,\"x\":{\"a\":11,\"x\":11}}");
        System.out.println();
        List<Map<String, Object>> maps = JsonUtils.parseArray("[{\"a\":11,\"x\":{\"a\":11,\"x\":11}}]");
        System.out.println();
        List<Map> maps1 = JsonUtils.parseArray("[{\"a\":11,\"x\":{\"a\":11,\"x\":11}}]", Map.class);
        System.out.println();
        List<Map<String, Object>> maps2 = JsonUtils.parseObject("[{\"a\":11,\"x\":{\"a\":11,\"x\":11}}]", new TypeReference<List<Map<String, Object>>>() {
            @Override
            public Type getType() {
                return super.getType();
            }
        });
        System.out.println();
    }
}
