package tech.obiteaaron.winter.embed.rpc.serializer;

import com.caucho.hessian.io.HessianFactory;
import com.caucho.hessian.io.HessianOutput;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;

@Slf4j
public class HessianWinterSerializer implements WinterSerializer {

    static final HessianFactory HESSIAN_FACTORY = new HessianFactory();

    static {
        HESSIAN_FACTORY.getSerializerFactory().setAllowNonSerializable(true);
    }

    @Override
    public String type() {
        return "hessian";
    }

    @Override
    public String serializer(Object object) {
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            HessianOutput hessianOutput = new HessianOutput(byteArrayOutputStream);
            hessianOutput.setSerializerFactory(HESSIAN_FACTORY.getSerializerFactory());
            hessianOutput.writeObject(object);
            hessianOutput.flush();
            return byteArrayOutputStream.toString();
        } catch (Exception e) {
            log.error("Serializer Hessian failed", e);
            throw e instanceof RuntimeException ? (RuntimeException) e : new RuntimeException(e);
        }
        // 这里无需close流
    }
}
