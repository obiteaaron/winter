package tech.obiteaaron.winter.embed.rpc;

import com.google.common.collect.Lists;
import org.junit.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;

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
        {
            User byId = testService.findById("1");
            User byId2 = testService2.findById("1");
            System.out.println("findById success");
        }
        {
            User user = new User();
            user.setId("111");
            List<User> list = testService.findList(user);
            System.out.println("findList success");
        }
        {
            User user = new User();
            user.setId("111");
            List<Boolean> booleans = testService.batchCreate(Lists.newArrayList(user));
            System.out.println("batchCreate success");
        }
        System.out.println("all success");
    }
}
