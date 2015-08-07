package ru.mail.jira.plugins.contentprojects.configuration;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.sal.api.transaction.TransactionCallback;
import net.java.ao.Query;
import org.apache.commons.lang3.StringUtils;
import ru.mail.jira.plugins.commons.RestFieldException;

public class CounterManager {
    private final ActiveObjects ao;
    private final I18nHelper i18nHelper;

    public CounterManager(ActiveObjects ao, I18nHelper i18nHelper) {
        this.ao = ao;
        this.i18nHelper = i18nHelper;
    }

    public Counter getCounter(final int id) {
        return ao.executeInTransaction(new TransactionCallback<Counter>() {
            @Override
            public Counter doInTransaction() {
                Counter counter = ao.get(Counter.class, id);
                if (counter == null)
                    throw new IllegalArgumentException(String.format("Counter is not found by id %s", id));
                return counter;
            }
        });
    }

    public Counter[] getCounters() {
        return ao.executeInTransaction(new TransactionCallback<Counter[]>() {
            @Override
            public Counter[] doInTransaction() {
                return ao.find(Counter.class, Query.select().order("NAME"));
            }
        });
    }

    private void validateCounterParams(String name) {
        if (StringUtils.isEmpty(name))
            throw new RestFieldException(i18nHelper.getText("issue.field.required", i18nHelper.getText("common.words.name")), "name");
    }

    public int createCounter(final String name) {
        validateCounterParams(name);
        return ao.executeInTransaction(new TransactionCallback<Integer>() {
            @Override
            public Integer doInTransaction() {
                Counter counter = ao.create(Counter.class);
                counter.setName(name);
                counter.save();
                return counter.getID();
            }
        });
    }

    public void updateCounter(final int id, final String name) {
        validateCounterParams(name);
        ao.executeInTransaction(new TransactionCallback<Void>() {
            @Override
            public Void doInTransaction() {
                Counter counter = getCounter(id);
                counter.setName(name);
                counter.save();
                return null;
            }
        });
    }

    public void deleteCounter(final int id) {
        ao.executeInTransaction(new TransactionCallback<Void>() {
            @Override
            public Void doInTransaction() {
                Counter counter = getCounter(id);
                ao.delete(counter.getCounterConfigs());
                ao.delete(counter);
                return null;
            }
        });
    }

    public CounterConfig getCounterConfig(final Counter counter, final Project project) {
        for (CounterConfig counterConfig : counter.getCounterConfigs())
            if (counterConfig.getProjectId() == project.getId())
                return counterConfig;
        return null;
    }

    public void setCounterConfig(final Counter counter, final Project project, final Integer ratingId, final String ratingPassword) {
        ao.executeInTransaction(new TransactionCallback<Void>() {
            @Override
            public Void doInTransaction() {
                CounterConfig counterConfig = getCounterConfig(counter, project);
                if (counterConfig == null) {
                    counterConfig = ao.create(CounterConfig.class);
                    counterConfig.setCounter(counter);
                    counterConfig.setProjectId(project.getId());
                }
                counterConfig.setRatingId(ratingId);
                counterConfig.setRatingPassword(ratingPassword);
                counterConfig.save();
                return null;
            }
        });
    }
}
