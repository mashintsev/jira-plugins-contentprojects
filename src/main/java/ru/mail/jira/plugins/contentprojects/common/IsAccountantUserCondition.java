package ru.mail.jira.plugins.contentprojects.common;

import com.atlassian.jira.plugin.webfragment.conditions.AbstractWebCondition;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.user.ApplicationUser;
import ru.mail.jira.plugins.commons.CommonUtils;

public class IsAccountantUserCondition extends AbstractWebCondition {
    @Override
    public boolean shouldDisplay(ApplicationUser user, JiraHelper jiraHelper) {
        return CommonUtils.isUserInGroups(user, Consts.ACCOUNTANTS_GROUPS);
    }
}
