package tech.obiteaaron.winter.embed.rpc.serializer;

import com.caucho.hessian.io.HessianInput;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Base64Utils;

import java.io.ByteArrayInputStream;

@Slf4j
public class HessianWinterDeserializer implements WinterDeserializer {
    @Override
    public String type() {
        return "hessian";
    }

    @Override
    public Object deserializer(String value, boolean isArray, String[] types, String[] invocationParameterTypes) {
        try {
            byte[] bytes = Base64Utils.decodeFromString(value);
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
            HessianInput hessianInput = new HessianInput(byteArrayInputStream);
            hessianInput.setSerializerFactory(HessianWinterSerializer.HESSIAN_FACTORY.getSerializerFactory());
            Object o = hessianInput.readObject();
            return o;
        } catch (Exception e) {
            log.error("Deserializer Hessian failed", e);
            log.warn("Deserializer Hessian failed, content={}", value);
            throw e instanceof RuntimeException ? (RuntimeException) e : new RuntimeException(e);
        }
        // 这里无需close流
    }
}
