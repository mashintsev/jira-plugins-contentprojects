package ru.mail.jira.plugins.contentprojects.configuration;

import com.atlassian.jira.issue.customfields.option.Option;
import com.atlassian.jira.project.Project;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import ru.mail.jira.plugins.contentprojects.common.Consts;

public class PluginData {
    private static final String PLUGIN_PREFIX = "ru.mail.jira.plugins.contentprojects:";
    private static final String COUNTER_ID = PLUGIN_PREFIX + "counterId";
    private static final String COUNTER_PASSWORD = PLUGIN_PREFIX + "counterPassword";
    private static final String SCROLL_COUNTER_IDS = PLUGIN_PREFIX + "scrollCounterIds_";
    private static final String SCROLL_COUNTERS_PASSWORD = PLUGIN_PREFIX + "scrollCountersPassword";
    private static final String API_URL = PLUGIN_PREFIX + "apiUrl";
    private static final String BUDGET_VALUE = PLUGIN_PREFIX + "budgetValue_";
    private static final String BUDGET_COST = PLUGIN_PREFIX + "budgetCost_";
    private static final String ACT_CREATED = PLUGIN_PREFIX + "actGenerated_";

    private final PluginSettingsFactory pluginSettingsFactory;

    public PluginData(PluginSettingsFactory pluginSettingsFactory) {
        this.pluginSettingsFactory = pluginSettingsFactory;
    }

    private String getString(Project project, String key) {
        return (String) pluginSettingsFactory.createSettingsForKey(project.getOriginalKey()).get(key);
    }

    private Integer getInteger(Project project, String key) {
        try {
            return Integer.valueOf(getString(project, key));
        } catch (Exception e) {
            return null;
        }
    }

    private boolean getBoolean(Project project, String key) {
        try {
            return Boolean.valueOf(getString(project, key));
        } catch (Exception e) {
            return false;
        }
    }

    private void set(Project project, String key, Object value) {
        pluginSettingsFactory.createSettingsForKey(project.getOriginalKey()).put(key, value != null ? value.toString() : null);
    }

    /* Project Settings */

    public Integer getCounterId(Project project) {
        return getInteger(project, COUNTER_ID);
    }

    public void setCounterId(Project project, Integer value) {
        set(project, COUNTER_ID, value);
    }

    public String getCounterPassword(Project project) {
        return getString(project, COUNTER_PASSWORD);
    }

    public void setCounterPassword(Project project, String value) {
        set(project, COUNTER_PASSWORD, value);
    }

    public Integer[] getScrollCounterIds(Project project) {
        Integer[] result = new Integer[Consts.SCROLL_CF_IDS.size()];
        for (int i = 0; i < result.length; i++)
            result[i] = getInteger(project, SCROLL_COUNTER_IDS + i);
        return result;
    }

    public void setScrollCounterIds(Project project, Integer[] value) {
        for (int i = 0; i < value.length; i++)
            set(project, SCROLL_COUNTER_IDS + i, value[i]);
    }

    public String getScrollCountersPassword(Project project) {
        return getString(project, SCROLL_COUNTERS_PASSWORD);
    }

    public void setScrollCountersPassword(Project project, String value) {
        set(project, SCROLL_COUNTERS_PASSWORD, value);
    }

    public String getApiUrl(Project project) {
        return getString(project, API_URL);
    }

    public void setApiUrl(Project project, String value) {
        set(project, API_URL, value);
    }

    public Integer getBudgetValue(Project project, Option option) {
        return getInteger(project, BUDGET_VALUE + option.getOptionId());
    }

    public void setBudgetValue(Project project, Option option, Integer value) {
        set(project, BUDGET_VALUE + option.getOptionId(), value);
    }

    public int getBudgetCost(Project project, Option option) {
        Integer result = getInteger(project, BUDGET_COST + option.getOptionId());
        if (result == null)
            result = 0;
        return result;
    }

    public void setBudgetCost(Project project, Option option, Integer value) {
        set(project, BUDGET_COST + option.getOptionId(), value);
    }

    public boolean isActCreated(Project project, Option option) {
        return getBoolean(project, ACT_CREATED + option.getOptionId());
    }

    public void setActCreated(Project project, Option option, boolean value) {
        set(project, ACT_CREATED + option.getOptionId(), value);
    }
}
