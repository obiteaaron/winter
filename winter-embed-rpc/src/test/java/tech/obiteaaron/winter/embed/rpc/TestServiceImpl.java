package tech.obiteaaron.winter.embed.rpc;

import com.google.common.collect.Lists;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@WinterProvider
public class TestServiceImpl implements TestService {

    @Override
    public User findById(String id) {
        User user = new User();
        user.setId("111");
        return user;
    }

    @Override
    public List<User> findList(User userQuery) {
        User user = new User();
        user.setId("111");
        return Lists.newArrayList(user);
    }

    @Override
    public List<Boolean> batchCreate(List<User> userList) {
        return userList.stream().map(item -> true).collect(Collectors.toList());
    }
}
