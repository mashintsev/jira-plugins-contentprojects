package ru.mail.jira.plugins.contentprojects.configuration;

import net.java.ao.Entity;
import net.java.ao.OneToMany;

public interface Counter extends Entity {
    String getName();
    void setName(String name);

    @OneToMany
    public CounterConfig[] getCounterConfigs();
}
