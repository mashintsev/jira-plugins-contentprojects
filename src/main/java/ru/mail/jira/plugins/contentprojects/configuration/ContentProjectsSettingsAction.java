package ru.mail.jira.plugins.contentprojects.configuration;

import com.atlassian.jira.permission.ProjectPermissions;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import org.apache.commons.lang3.StringUtils;

public class ContentProjectsSettingsAction extends JiraWebActionSupport {
    private final CounterManager counterManager;
    private final PermissionManager permissionManager;
    private final PluginData pluginData;
    private final ProjectManager projectManager;

    private boolean saved;
    private Project project;
    private String[] ratingIds;
    private String[] ratingPasswords;
    private String apiUrl;
    private String newsApiUrl;

    public ContentProjectsSettingsAction(CounterManager counterManager, PermissionManager permissionManager, PluginData pluginData, ProjectManager projectManager) {
        this.counterManager = counterManager;
        this.permissionManager = permissionManager;
        this.pluginData = pluginData;
        this.projectManager = projectManager;
    }

    private boolean isUserAllowed() {
        return permissionManager.hasPermission(ProjectPermissions.ADMINISTER_PROJECTS, project, getLoggedInApplicationUser());
    }

    @Override
    public String doDefault() throws Exception {
        if (!isUserAllowed())
            return NONE;

        if (project == null)
            return NONE;

        Counter[] counters = counterManager.getCounters();
        ratingIds = new String[counters.length];
        ratingPasswords = new String[counters.length];
        for (int i = 0; i < counters.length; i++) {
            CounterConfig counterConfig = counterManager.getCounterConfig(counters[i], project);
            if (counterConfig != null) {
                ratingIds[i] = counterConfig.getRatingId() == null ? null : String.valueOf(counterConfig.getRatingId());
                ratingPasswords[i] = counterConfig.getRatingPassword();
            }
        }

        apiUrl = pluginData.getApiUrl(project);
        newsApiUrl = pluginData.getNewsApiUrl(project);

        return INPUT;
    }

    private Integer getInteger(String s) throws NumberFormatException {
        if (StringUtils.isEmpty(s))
            return null;
        return Integer.valueOf(s);
    }

    @Override
    protected void doValidation() {
        for (int i = 0; i < ratingIds.length; i++)
            try {
                getInteger(ratingIds[i]);
            } catch (IllegalArgumentException e) {
                addError(String.format("counter_%d", i), getText("ru.mail.jira.plugins.contentprojects.configuration.settings.illegalCounterId"));
            }
    }

    @RequiresXsrfCheck
    @Override
    public String doExecute() throws Exception {
        if (!isUserAllowed())
            return NONE;

        saved = true;

        if (project == null)
            return NONE;

        Counter[] counters = counterManager.getCounters();
        if (ratingIds == null || ratingIds.length != counters.length || ratingPasswords == null || ratingPasswords.length != counters.length)
            return NONE;
        for (int i = 0; i < counters.length; i++)
            counterManager.setCounterConfig(counters[i], project, getInteger(ratingIds[i]), ratingPasswords[i]);

        pluginData.setApiUrl(project, apiUrl);
        pluginData.setNewsApiUrl(project, newsApiUrl);

        return INPUT;
    }

    @SuppressWarnings("UnusedDeclaration")
    public <T> T getArrayElement(T[] a, int i) {
        return a[i];
    }

    @SuppressWarnings("UnusedDeclaration")
    public boolean isSaved() {
        return saved;
    }

    @SuppressWarnings("UnusedDeclaration")
    public String getProjectKey() {
        return project.getKey();
    }

    @SuppressWarnings("UnusedDeclaration")
    public void setProjectKey(String projectKey) {
        this.project = projectManager.getProjectByCurrentKey(projectKey);
    }

    @SuppressWarnings("UnusedDeclaration")
    public Counter[] getCounters() {
        return counterManager.getCounters();
    }

    @SuppressWarnings("UnusedDeclaration")
    public String[] getRatingIds() {
        return ratingIds;
    }

    @SuppressWarnings("UnusedDeclaration")
    public void setRatingIds(String[] ratingIds) {
        this.ratingIds = ratingIds;
    }

    @SuppressWarnings("UnusedDeclaration")
    public String[] getRatingPasswords() {
        return ratingPasswords;
    }

    @SuppressWarnings("UnusedDeclaration")
    public void setRatingPasswords(String[] ratingPasswords) {
        this.ratingPasswords = ratingPasswords;
    }

    @SuppressWarnings("UnusedDeclaration")
    public String getApiUrl() {
        return apiUrl;
    }

    @SuppressWarnings("UnusedDeclaration")
    public void setApiUrl(String apiUrl) {
        this.apiUrl = apiUrl;
    }

    @SuppressWarnings("UnusedDeclaration")
    public String getNewsApiUrl() {
        return newsApiUrl;
    }

    @SuppressWarnings("UnusedDeclaration")
    public void setNewsApiUrl(String newsApiUrl) {
        this.newsApiUrl = newsApiUrl;
    }
}
