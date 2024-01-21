package tech.obiteaaron.winter.embed.rpc;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = TestApplication.class)
public class TestStart {

    @Autowired
    private TestClient testClient;

    @Test
    public void invokeService() throws InterruptedException {
        testClient.invokeService();
        System.out.println();
    }
}
