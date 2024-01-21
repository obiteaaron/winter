package tech.obiteaaron.winter.embed.rpc.serializer;

import com.caucho.hessian.io.HessianInput;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;

@Slf4j
public class HessianWinterDeserializer implements WinterDeserializer {
    @Override
    public String type() {
        return "hessian";
    }

    @Override
    public Object deserializer(String value, Class<?>[] types) {
        try {
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(value.getBytes());
            HessianInput hessianOutput = new HessianInput(byteArrayInputStream);
            Object o = hessianOutput.readObject();
            return o;
        } catch (Exception e) {
            log.error("Deserializer Hessian failed", e);
            log.warn("Deserializer Hessian failed, content={}", value);
            throw e instanceof RuntimeException ? (RuntimeException) e : new RuntimeException(e);
        }
        // 这里无需close流
    }
}
