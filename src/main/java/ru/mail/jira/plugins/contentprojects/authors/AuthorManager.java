package ru.mail.jira.plugins.contentprojects.authors;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.bc.user.search.UserPickerSearchService;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.roles.ProjectRole;
import com.atlassian.jira.security.roles.ProjectRoleActors;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.ApplicationUsers;
import com.atlassian.jira.user.util.UserManager;
import org.apache.commons.lang3.StringUtils;
import ru.mail.jira.plugins.contentprojects.authors.freelancers.Freelancer;
import ru.mail.jira.plugins.contentprojects.authors.freelancers.FreelancerManager;
import ru.mail.jira.plugins.contentprojects.common.Consts;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class AuthorManager {
    private final FreelancerManager freelancerManager;
    private final ProjectManager projectManager;
    private final ProjectRoleManager projectRoleManager;
    private final UserManager userManager;
    private final UserPickerSearchService userPickerSearchService;

    public AuthorManager(FreelancerManager freelancerManager, ProjectManager projectManager, ProjectRoleManager projectRoleManager, UserManager userManager, UserPickerSearchService userPickerSearchService) {
        this.freelancerManager = freelancerManager;
        this.projectManager = projectManager;
        this.projectRoleManager = projectRoleManager;
        this.userManager = userManager;
        this.userPickerSearchService = userPickerSearchService;
    }

    public Author getAuthor(String dbValue) {
        if (StringUtils.isEmpty(dbValue))
            return null;

        if (dbValue.startsWith("&")) {
            int freelancerId = Integer.parseInt(dbValue.substring(1));
            return new FreelancerAuthor(freelancerManager.getFreelancer(freelancerId));
        } else
            return new UserAuthor(userManager.getUserByKeyEvenWhenUnknown(dbValue));
    }

    public Collection<Author> getAuthors(Collection<Long> projectIds) {
        Collection<Author> result = new ArrayList<Author>();

        ProjectRole authorsProjectRole = projectRoleManager.getProjectRole(Consts.AUTHORS_ROLE_NAME);
        for (Long projectId : projectIds) {
            ProjectRoleActors projectRoleActors = projectRoleManager.getProjectRoleActors(authorsProjectRole, projectManager.getProjectObj(projectId));
            for (ApplicationUser user : projectRoleActors.getApplicationUsers())
                result.add(new UserAuthor(user));
        }

        for (Freelancer freelancer : freelancerManager.getFreelancers())
            result.add(new FreelancerAuthor(freelancer));

        return result;

    }

    public Collection<Author> searchAuthors(ApplicationUser searcher, String term, int maxNumResults) {
        List<Author> result = new ArrayList<Author>();

        JiraServiceContext serviceContext = new JiraServiceContextImpl(searcher);
        if (userPickerSearchService.canPerformAjaxSearch(serviceContext))
            for (User user : userPickerSearchService.findUsersAllowEmptyQuery(serviceContext, term))
                result.add(new UserAuthor(ApplicationUsers.from(user)));

        if (result.size() < maxNumResults)
            for (Freelancer freelancer : freelancerManager.searchFreelancers("%" + term + "%"))
                result.add(new FreelancerAuthor(freelancer));

        if (result.size() > maxNumResults)
            result = result.subList(0, maxNumResults);
        return result;
    }
}
