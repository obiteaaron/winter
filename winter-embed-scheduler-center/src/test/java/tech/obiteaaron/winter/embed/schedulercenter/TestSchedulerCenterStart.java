package tech.obiteaaron.winter.embed.schedulercenter;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.concurrent.TimeUnit;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = TestApplication.class)
public class TestSchedulerCenterStart {

    @Test
    public void start() throws Exception {
        TimeUnit.MINUTES.sleep(10);
    }
}
