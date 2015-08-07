package ru.mail.jira.plugins.contentprojects.configuration;

import net.java.ao.Entity;

public interface CounterConfig extends Entity {
    Counter getCounter();
    void setCounter(Counter counter);

    long getProjectId();
    void setProjectId(long projectId);

    Integer getRatingId();
    void setRatingId(Integer ratingId);

    String getRatingPassword();
    void setRatingPassword(String ratingPassword);
}
