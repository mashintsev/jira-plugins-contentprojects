package ru.mail.jira.plugins.contentprojects.authors;

import com.atlassian.jira.avatar.Avatar;
import com.atlassian.jira.avatar.AvatarService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;

public class UserAuthor extends Author {
    private final ApplicationUser user;

    public UserAuthor(ApplicationUser user) {
        this.user = user;
    }

    @Override
    public String getDbValue() {
        return user.getKey();
    }

    @Override
    public String getSearchValue() {
        return user.getName();
    }

    @Override
    public String getShortCaption() {
        return user.getDisplayName();
    }

    @Override
    public String getLongCaption() {
        return String.format("%s - %s (%s)", user.getDisplayName(), user.getEmailAddress(), user.getUsername());
    }

    @Override
    public String getAvatarUrl(boolean small) {
        AvatarService avatarService = ComponentAccessor.getAvatarService();
        JiraAuthenticationContext jiraAuthenticationContext = ComponentAccessor.getJiraAuthenticationContext();
        return avatarService.getAvatarURL(jiraAuthenticationContext.getUser(), user, small ? Avatar.Size.SMALL : Avatar.Size.NORMAL).toString();
    }

    public ApplicationUser getUser() {
        return user;
    }
}
