package tech.obiteaaron.winter.embed.rpc.constant;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.stream.Collectors;

public class MethodUtil {
    public static String generateMethodSignature(Method method) {
        String name = method.getName();
        Class<?>[] parameterTypes = method.getParameterTypes();
        String parameterString = Arrays.stream(parameterTypes).map(Class::getName).collect(Collectors.joining(","));
        return name + "(" + parameterString + ")";
    }
}
