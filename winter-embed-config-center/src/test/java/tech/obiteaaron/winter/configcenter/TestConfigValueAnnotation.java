package tech.obiteaaron.winter.configcenter;

import lombok.EqualsAndHashCode;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
public class TestConfigValueAnnotation {

    @ConfigValue(name = "test1", group = "testGroup", description = "")
    public static String config1 = "1";

    @ConfigValue(name = "test2", group = "testGroup", description = "")
    public static int config2 = 0;

    @ConfigValue(name = "test3", group = "testGroup", description = "")
    private boolean config3 = false;

    @ConfigValue(name = "test4", group = "testGroup", description = "")
    private double config4 = 0;

    @ConfigValue(name = "test5", group = "testGroup", description = "")
    private List<String> config5;

    @ConfigValue(name = "test6", group = "testGroup", description = "")
    private Set<String> config6;

    @ConfigValue(name = "test7", group = "testGroup", description = "")
    private TestConfigClass config7 = new TestConfigClass();

    @ConfigValue(name = "test8", group = "testGroup", description = "")
    private List<TestConfigClass> config8;

    @EqualsAndHashCode
    public static class TestConfigClass {
        private String key1;
        private int key2;
    }
}
