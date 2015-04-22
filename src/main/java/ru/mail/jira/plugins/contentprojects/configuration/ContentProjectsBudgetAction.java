package ru.mail.jira.plugins.contentprojects.configuration;

import com.atlassian.jira.issue.customfields.option.Option;
import com.atlassian.jira.permission.ProjectPermissions;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import ru.mail.jira.plugins.contentprojects.gadgets.RemainingBudgetResource;
import webwork.action.ServletActionContext;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

public class ContentProjectsBudgetAction extends JiraWebActionSupport {
    private final PermissionManager permissionManager;
    private final PluginData pluginData;
    private final ProjectManager projectManager;

    private boolean saved;
    private Project project;
    private final List<Option> options = RemainingBudgetResource.getActivePaymentMonthOptions();
    private final String[] values = new String[options.size()];
    private final Integer[] parsedValues = new Integer[options.size()];
    private final String[] costs = new String[options.size()];
    private final Integer[] parsedCosts = new Integer[options.size()];

    public ContentProjectsBudgetAction(PermissionManager permissionManager, PluginData pluginData, ProjectManager projectManager) {
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

        for (int i = 0; i < options.size(); i++) {
            Integer value = pluginData.getBudgetValue(project, options.get(i));
            values[i] = value == null ? null : value.toString();
            parsedValues[i] = value;

            Integer cost = pluginData.getBudgetCost(project, options.get(i));
            costs[i] = cost == null ? null : cost.toString();
            parsedCosts[i] = cost;
        }
        return INPUT;
    }

    @Override
    protected void doValidation() {
        if (project == null)
            addErrorMessage("Project is not specified.");
        for (int i = 0; i < parsedValues.length; i++)
            if (parsedValues[i] == null)
                addError("value_" + i, getText("issue.field.required", options.get(i).getValue()));
        for (int i = 0; i < parsedCosts.length; i++)
            if (parsedValues[i] == null)
                addError("cost_" + i, getText("issue.field.required", options.get(i).getValue()));
    }

    @RequiresXsrfCheck
    @Override
    public String doExecute() throws Exception {
        if (!isUserAllowed())
            return sendError(HttpServletResponse.SC_UNAUTHORIZED);

        saved = true;
        for (int i = 0; i < parsedValues.length; i++)
            pluginData.setBudgetValue(project, options.get(i), parsedValues[i]);
        for (int i = 0; i < parsedCosts.length; i++)
            pluginData.setBudgetCost(project, options.get(i), parsedCosts[i]);
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
    public List<Option> getOptions() {
        return options;
    }

    @SuppressWarnings("UnusedDeclaration")
    public String[] getValues() {
        return values;
    }

    @SuppressWarnings("UnusedDeclaration")
    public void setValues(String[] values) {
        for (int i = 0; i < this.values.length; i++) {
            this.values[i] = values[i];
            try {
                this.parsedValues[i] = Integer.valueOf(values[i]);
            } catch (NumberFormatException e) {
                this.parsedValues[i] = null;
            }
        }
    }

    @SuppressWarnings("UnusedDeclaration")
    public String[] getCosts() {
        return costs;
    }

    @SuppressWarnings("UnusedDeclaration")
    public void setCosts(String[] costs) {
        for (int i = 0; i < this.costs.length; i++) {
            this.costs[i] = costs[i];
            try {
                this.parsedCosts[i] = Integer.valueOf(costs[i]);
            } catch (NumberFormatException e) {
                this.parsedCosts[i] = null;
            }
        }
    }
}