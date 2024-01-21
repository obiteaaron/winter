package tech.obiteaaron.winter.embed.rpc;

import org.junit.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class TestClient {

    @Autowired
    @Qualifier("tech.obiteaaron.winter.embed.rpc.TestService:Consumer")
    @WinterConsumer
    private TestService testService;

    @Autowired
    @Qualifier("testServiceImpl")
    private TestService testService2;

    public void invokeService() throws InterruptedException {
        Assert.assertNotEquals(testService2, testService);
        User byId = testService.findById("1");
        User byId2 = testService2.findById("1");
        System.out.println();
    }
}
