package ru.mail.jira.plugins.contentprojects.common;

import com.atlassian.jira.plugin.webfragment.conditions.AbstractWebCondition;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.user.ApplicationUser;

public class IsContentProjectCondition extends AbstractWebCondition {
    @Override
    public boolean shouldDisplay(ApplicationUser user, JiraHelper jiraHelper) {
        Project project = jiraHelper.getProjectObject();
        return Consts.PROJECT_IDS.contains(project.getId());
    }
}
