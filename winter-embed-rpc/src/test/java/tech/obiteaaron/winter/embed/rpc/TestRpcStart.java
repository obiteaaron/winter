package tech.obiteaaron.winter.embed.rpc;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import tech.obiteaaron.winter.common.tools.http.CommonOkHttpClient;
import tech.obiteaaron.winter.common.tools.http.OkHttpClientFactory;

import java.util.concurrent.TimeUnit;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = TestApplication.class)
public class TestRpcStart {

    @Autowired
    private TestClient testClient;

    @Test
    public void invokeService() throws InterruptedException {
        TimeUnit.SECONDS.sleep(5);
        testClient.invokeService();
        System.out.println();
        TimeUnit.MINUTES.sleep(10);
    }

    @Test
    public void invokeServiceByHttp() throws InterruptedException {
        TimeUnit.SECONDS.sleep(5);
        CommonOkHttpClient commonOkHttpClient = OkHttpClientFactory.commonOkHttpClient();
        String s = commonOkHttpClient.doPost("http://127.0.0.1:8080/tech.obiteaaron.winter.embed.rpc.TestService?serializerType=json&methodSignature=findById(java.lang.String)",
                "{\"serviceName\":\"tech.obiteaaron.winter.embed.rpc.TestService\",\"methodSignature\":\"findById(java.lang.String)\",\"arguments\":[null]}");
        System.out.println(s);
        Assert.assertEquals("{\"id\":\"111\"}", s);
    }
}
