package tech.obiteaaron.winter.common.tools.id;

import org.apache.commons.lang3.StringUtils;

import java.util.UUID;

public class UuidGenerator {

    public static String generate() {
        return StringUtils.remove(UUID.randomUUID().toString(), "-");
    }
}
