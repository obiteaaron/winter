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
        TimeUnit.SECONDS.sleep(10);
    }

    @Test
    public void invokeServiceByHttp() throws InterruptedException {
        TimeUnit.SECONDS.sleep(5);
        CommonOkHttpClient commonOkHttpClient = OkHttpClientFactory.commonOkHttpClient();
//        String s = commonOkHttpClient.doPost("http://127.0.0.1:8080/tech.obiteaaron.winter.embed.rpc.TestService?serializerType=json&methodSignature=findById(java.lang.String)",
//                "{\"serviceName\":\"tech.obiteaaron.winter.embed.rpc.TestService\",\"methodSignature\":\"findById(java.lang.String)\",\"arguments\":[null]}");
        String s = commonOkHttpClient.doPost("http://127.0.0.1:8080/tech.obiteaaron.winter.embed.rpc.TestService?serializerType=json&methodSignature=findById(java.lang.String)",
                "object$WRSJ$tech.obiteaaron.winter.embed.rpc.executing.InvokeContext$WRSJ${\"applicationName\":\"rpc-test-client\",\"traceId\":\"7dfa52434519449d9ba250ca83de9ce0\",\"serviceName\":\"tech.obiteaaron.winter.embed.rpc.TestService\",\"methodSignature\":\"findById(java.lang.String)\",\"serializerType\":\"json\",\"arguments\":[\"[Ljava.lang.Object;\",[null]],\"result\":null}");
        System.out.println(s);
        Assert.assertTrue(s.endsWith("{\"id\":\"111\"}"));
    }
}
