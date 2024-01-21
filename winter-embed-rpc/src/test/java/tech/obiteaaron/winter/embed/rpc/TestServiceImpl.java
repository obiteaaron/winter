package tech.obiteaaron.winter.embed.rpc;

import org.springframework.stereotype.Component;

@Component
@WinterProvider
public class TestServiceImpl implements TestService {

    @Override
    public User findById(String id) {
        User user = new User();
        user.setId("111");
        return user;
    }
}
