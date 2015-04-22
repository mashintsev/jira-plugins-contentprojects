package ru.mail.jira.plugins.contentprojects.authors.customfield;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.jql.values.ClauseValuesGenerator;
import com.atlassian.jira.user.ApplicationUsers;
import ru.mail.jira.plugins.contentprojects.authors.Author;
import ru.mail.jira.plugins.contentprojects.authors.AuthorManager;

import java.util.ArrayList;
import java.util.List;

public class AuthorClauseValuesGenerator implements ClauseValuesGenerator {
    private final AuthorManager authorManager;

    public AuthorClauseValuesGenerator(AuthorManager authorManager) {
        this.authorManager = authorManager;
    }

    @Override
    public Results getPossibleValues(User searcher, String jqlClauseName, String valuePrefix, int maxNumResults) {
        List<Result> result = new ArrayList<Result>();
        for (Author author : authorManager.searchAuthors(ApplicationUsers.from(searcher), valuePrefix, maxNumResults))
            result.add(new Result(author.getSearchValue().toString(), author.getLongCaption()));
        return new Results(result);
    }
}
