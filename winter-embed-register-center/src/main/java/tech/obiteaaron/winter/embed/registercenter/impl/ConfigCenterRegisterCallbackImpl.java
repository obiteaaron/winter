package tech.obiteaaron.winter.embed.registercenter.impl;

import tech.obiteaaron.winter.configcenter.Config;
import tech.obiteaaron.winter.configcenter.ConfigCenter;

import java.util.List;

public class ConfigCenterRegisterCallbackImpl {
    public void init() {
        List<Config> allConfigs = ConfigCenter.getAllConfigs();
    }
}
