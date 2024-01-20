package tech.obiteaaron.winter.embed.registercenter.impl;

import lombok.extern.slf4j.Slf4j;
import tech.obiteaaron.winter.embed.registercenter.NotifyListener;
import tech.obiteaaron.winter.embed.registercenter.RegisterService;
import tech.obiteaaron.winter.embed.registercenter.model.URL;

import java.util.List;

@Slf4j
public class DefaultRegisterServiceImpl implements RegisterService {
    @Override
    public void register(URL registerUrl) {

    }

    @Override
    public void unregister(URL registerUrl) {

    }

    @Override
    public void subscribe(URL url, NotifyListener listener) {

    }

    @Override
    public void unsubscribe(URL url, NotifyListener listener) {

    }

    @Override
    public List<URL> lookup(URL url) {
        return null;
    }
}
