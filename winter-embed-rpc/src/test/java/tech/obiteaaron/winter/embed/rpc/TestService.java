package tech.obiteaaron.winter.embed.rpc;

import java.util.List;

public interface TestService {

    User findById(String id);

    List<User> findList(User user);

    List<Boolean> batchCreate(List<User> userList);
}
