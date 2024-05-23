package tech.obiteaaron.winter.configcenter;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import tech.obiteaaron.winter.configcenter.service.ConfigManagerService;

import java.util.List;
import java.util.concurrent.TimeUnit;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = TestApplication.class)
//@Transactional
public class TestConfigManager {

    @Autowired
    private ConfigManagerService configManagerService;

    @Test
    public void test() throws InterruptedException {
        Config config = of("test_1", "testGroup_1", "1", "");
        int i = configManagerService.create(config);
        Assert.assertEquals(1, i);

        List<Config> query = configManagerService.query(config);
        Assert.assertEquals(1, query.size());
        Config config1 = query.get(0);
        config.setId(config1.getId());
        config.setContent("1_update");
        int modify = configManagerService.modify(config);
        Assert.assertEquals(1, modify);

        // 等待，看看自动拉取的逻辑
        TimeUnit.MINUTES.sleep(10);
    }

    private static Config of(String name, String group, String content, String description) {
        Config config = new Config();
        config.setName(name);
        config.setGroupName(group);
        config.setContent(content);
        config.setDescription(description);
        return config;
    }
}
