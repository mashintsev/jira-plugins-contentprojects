package ru.mail.jira.plugins.contentprojects.configuration;

import com.atlassian.jira.permission.ProjectPermissions;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import org.apache.commons.lang.StringUtils;
import ru.mail.jira.plugins.contentprojects.common.Consts;
import webwork.action.ServletActionContext;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class ContentProjectsSettingsAction extends JiraWebActionSupport {
    private final PermissionManager permissionManager;
    private final PluginData pluginData;
    private final ProjectManager projectManager;

    private boolean saved;
    private Project project;
    private Integer counterId;
    private String counterPassword;
    private Integer[] scrollCounterIds = new Integer[Consts.SCROLL_CF_IDS.size()];
    private String scrollCountersPassword;
    private String apiUrl;

    public ContentProjectsSettingsAction(PermissionManager permissionManager, PluginData pluginData, ProjectManager projectManager) {
        this.permissionManager = permissionManager;
        this.pluginData = pluginData;
        this.projectManager = projectManager;
    }

    private boolean isUserAllowed() {
        return permissionManager.hasPermission(ProjectPermissions.ADMINISTER_PROJECTS, project, getLoggedInApplicationUser());
    }

    private String sendError(int code) throws IOException {
        if (ServletActionContext.getResponse() != null)
            ServletActionContext.getResponse().sendError(code);
        return NONE;
    }

    @Override
    public String doDefault() throws Exception {
        if (project == null)
            return sendError(HttpServletResponse.SC_BAD_REQUEST);
        if (!isUserAllowed())
            return sendError(HttpServletResponse.SC_UNAUTHORIZED);

        counterId = pluginData.getCounterId(project);
        counterPassword = pluginData.getCounterPassword(project);
        scrollCounterIds = pluginData.getScrollCounterIds(project);
        scrollCountersPassword = pluginData.getScrollCountersPassword(project);
        apiUrl = pluginData.getApiUrl(project);
        return INPUT;
    }

    @Override
    protected void doValidation() {
        if (project == null)
            addErrorMessage("Project is not specified.");
        if (counterId == null)
            addError("counterId", getText("issue.field.required", getText("ru.mail.jira.plugins.contentprojects.configuration.settings.counterId")));
        if (StringUtils.isEmpty(counterPassword))
            addError("counterPassword", getText("issue.field.required", getText("ru.mail.jira.plugins.contentprojects.configuration.settings.counterPassword")));
    }

    @RequiresXsrfCheck
    @Override
    public String doExecute() throws Exception {
        if (!isUserAllowed())
            return sendError(HttpServletResponse.SC_UNAUTHORIZED);

        saved = true;
        pluginData.setCounterId(project, counterId);
        pluginData.setCounterPassword(project, counterPassword);
        pluginData.setScrollCounterIds(project, scrollCounterIds);
        pluginData.setScrollCountersPassword(project, scrollCountersPassword);
        pluginData.setApiUrl(project, apiUrl);
        return INPUT;
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
    public Integer getCounterId() {
        return counterId;
    }

    @SuppressWarnings("UnusedDeclaration")
    public void setCounterId(Integer counterId) {
        this.counterId = counterId;
    }

    @SuppressWarnings("UnusedDeclaration")
    public String getCounterPassword() {
        return counterPassword;
    }

    @SuppressWarnings("UnusedDeclaration")
    public void setCounterPassword(String counterPassword) {
        this.counterPassword = counterPassword;
    }

    @SuppressWarnings("UnusedDeclaration")
    public Integer[] getScrollCounterIds() {
        return scrollCounterIds;
    }

    @SuppressWarnings("UnusedDeclaration")
    public void setScrollCounterIds(Integer[] scrollCounterIds) {
        this.scrollCounterIds = scrollCounterIds;
    }

    @SuppressWarnings("UnusedDeclaration")
    public String getScrollCountersPassword() {
        return scrollCountersPassword;
    }

    @SuppressWarnings("UnusedDeclaration")
    public void setScrollCountersPassword(String scrollCountersPassword) {
        this.scrollCountersPassword = scrollCountersPassword;
    }

    @SuppressWarnings("UnusedDeclaration")
    public String getApiUrl() {
        return apiUrl;
    }

    @SuppressWarnings("UnusedDeclaration")
    public void setApiUrl(String apiUrl) {
        this.apiUrl = apiUrl;
    }
}
